package com.typely.keyboard.ime

import android.view.KeyEvent
import android.view.inputmethod.InputConnection
import com.typely.keyboard.ime.KeyAction.Delete
import com.typely.keyboard.ime.KeyAction.DeleteWord
import com.typely.keyboard.ime.KeyAction.Newline
import com.typely.keyboard.ime.KeyAction.Output
import com.typely.keyboard.ime.KeyAction.Space
import com.typely.keyboard.settings.KeyboardSettings

/**
 * Executes [KeyAction]s against the active [InputConnection].
 * All text processing stays on-device (see PRD Privacy).
 */
class KeyProcessor(
    private val getSettings: () -> KeyboardSettings
) {

    private var sentenceStart = true
    private var lastKeyWasSpace = false
    private var doubleSpaceTriggered = false

    fun onStartInput() {
        sentenceStart = true
        lastKeyWasSpace = false
        doubleSpaceTriggered = false
    }

    fun process(action: KeyAction, input: InputConnection?) {
        if (input == null) return
        when (action) {
            is Output -> commitText(action.text, input)

            Space -> {
                val settings = getSettings()
                if (settings.doubleSpacePeriod && lastKeyWasSpace && !doubleSpaceTriggered) {
                    input.deleteSurroundingText(1, 0)
                    input.commitText(". ", 1)
                    sentenceStart = true
                    doubleSpaceTriggered = true
                } else {
                    input.commitText(" ", 1)
                    doubleSpaceTriggered = false
                }
                lastKeyWasSpace = true
            }

            Delete -> input.deleteSurroundingText(1, 0)

            DeleteWord -> {
                // Delete back to the previous word boundary, on-device.
                for (i in 0 until 40) {
                    input.deleteSurroundingText(1, 0)
                    val before = input.getTextBeforeCursor(1, 0)?.toString()
                    if (before.isNullOrBlank()) break
                }
            }

            Newline -> {
                input.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                input.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
                input.commitText("\n", 1)
                sentenceStart = true
                lastKeyWasSpace = false
                doubleSpaceTriggered = false
            }

            // UI-only actions (shift / mode switches) produce no text.
            else -> Unit
        }
    }

    private fun commitText(text: String, input: InputConnection) {
        val settings = getSettings()

        // Auto-insert a space when starting a new word after a committed word.
        val prefix = input.getTextBeforeCursor(1, 0)?.toString()
        val startsWord = text.firstOrNull()?.isLetterOrDigit() == true
        val needsWordSpace = settings.autoSpacingEnabled &&
            startsWord && !prefix.isNullOrBlank() && prefix != "\n"
        val out = if (needsWordSpace) " $text" else text

        val rendered = if (
            settings.autoCapitalize && sentenceStart && out.isNotEmpty() && out.first().isLetter()
        ) {
            out.replaceFirstChar { it.uppercase() }
        } else {
            out
        }

        input.commitText(rendered, 1)
        sentenceStart = rendered.lastOrNull() in listOf('.', '?', '!')
        lastKeyWasSpace = false
        doubleSpaceTriggered = false
    }
}