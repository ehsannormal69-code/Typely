package com.typely.keyboard.ime

import com.typely.keyboard.ime.KeyAction.Delete
import com.typely.keyboard.ime.KeyAction.DeleteWord
import com.typely.keyboard.ime.KeyAction.Newline
import com.typely.keyboard.ime.KeyAction.Output
import com.typely.keyboard.ime.KeyAction.Space
import com.typely.keyboard.ime.KeyAction.SwitchToExtraSymbols
import com.typely.keyboard.ime.KeyAction.SwitchToLetters
import com.typely.keyboard.ime.KeyAction.SwitchToNumbers
import com.typely.keyboard.ime.KeyAction.SwitchToSymbols
import com.typely.keyboard.ime.KeyAction.ToggleCapsLock
import com.typely.keyboard.ime.KeyAction.ToggleShift

class KeyboardController {

    enum class Page { LETTERS, NUMBERS, SYMBOLS, EXTRA_SYMBOLS }

    var page = Page.LETTERS
        private set

    var shift = false
        private set

    var capsLock = false
        private set

    val rows: List<List<Key>> get() = when (page) {
        Page.LETTERS -> letterRows()
        Page.NUMBERS -> numberRows()
        Page.SYMBOLS -> symbolRows()
        Page.EXTRA_SYMBOLS -> extraSymbolRows()
    }

    fun process(key: Key): KeyAction? = when (key.type) {
        KeyType.CHAR -> {
            val out = if (shift || capsLock) key.primary.uppercase() else key.primary
            if (shift && !capsLock) shift = false
            Output(out)
        }

        KeyType.SHIFT -> {
            when {
                capsLock -> { capsLock = false; shift = false }
                shift -> { capsLock = true; shift = false }
                else -> shift = true
            }
            if (capsLock) ToggleCapsLock else ToggleShift
        }

        KeyType.BACKSPACE -> Delete
        KeyType.DELETE_WORD -> DeleteWord
        KeyType.ENTER -> Newline
        KeyType.SPACE -> Space
        KeyType.MODE_NUMBER -> { page = Page.NUMBERS; SwitchToNumbers }
        KeyType.MODE_SYMBOL -> { page = Page.SYMBOLS; SwitchToSymbols }
        KeyType.MODE_EXTRA_SYMBOLS -> { page = Page.EXTRA_SYMBOLS; SwitchToExtraSymbols }
        KeyType.MODE_LETTERS -> { page = Page.LETTERS; SwitchToLetters }
        else -> null
    }

    private fun letterRows(): List<List<Key>> {
        val top = listOf(
            Key(KeyType.CHAR, "q"), Key(KeyType.CHAR, "w"), Key(KeyType.CHAR, "e"),
            Key(KeyType.CHAR, "r"), Key(KeyType.CHAR, "t"), Key(KeyType.CHAR, "y"),
            Key(KeyType.CHAR, "u"), Key(KeyType.CHAR, "i"), Key(KeyType.CHAR, "o"),
            Key(KeyType.CHAR, "p")
        )
        val middle = listOf(
            Key(KeyType.CHAR, "a"), Key(KeyType.CHAR, "s"), Key(KeyType.CHAR, "d"),
            Key(KeyType.CHAR, "f"), Key(KeyType.CHAR, "g"), Key(KeyType.CHAR, "h"),
            Key(KeyType.CHAR, "j"), Key(KeyType.CHAR, "k"), Key(KeyType.CHAR, "l")
        )
        val bottom = listOf(
            Key(KeyType.SHIFT, label = shiftSymbol()),
            Key(KeyType.CHAR, "z"), Key(KeyType.CHAR, "x"), Key(KeyType.CHAR, "c"),
            Key(KeyType.CHAR, "v"), Key(KeyType.CHAR, "b"), Key(KeyType.CHAR, "n"),
            Key(KeyType.CHAR, "m"), Key(KeyType.BACKSPACE, label = "\u232B")
        )
        val function = listOf(
            Key(KeyType.MODE_NUMBER, label = "?123"),
            Key(KeyType.CHAR, ","),
            Key(KeyType.SPACE, label = "space", width = KeyWidth.SPACE),
            Key(KeyType.CHAR, "."),
            Key(KeyType.ENTER, label = "\u23CE", width = KeyWidth.WIDE)
        )
        return listOf(top, middle, bottom, function)
    }

    private fun numberRows(): List<List<Key>> {
        val top = (1..9).map { Key(KeyType.CHAR, it.toString()) } + listOf(Key(KeyType.CHAR, "0"))
        val second = "!@#%^&*()".map {
            val s = it.toString()
            Key(KeyType.CHAR, s, label = s)
        }
        val third = "-_/|:\\;'\",".split("").filter { it.isNotEmpty() }.map {
            Key(KeyType.CHAR, it, label = it)
        }
        val function = listOf(
            Key(KeyType.MODE_SYMBOL, label = "=\\<"),
            Key(KeyType.CHAR, ","),
            Key(KeyType.SPACE, label = "space", width = KeyWidth.SPACE),
            Key(KeyType.CHAR, "."),
            Key(KeyType.ENTER, label = "\u23CE", width = KeyWidth.WIDE)
        )
        return listOf(top, second, third, function)
    }

    private fun symbolRows(): List<List<Key>> {
        // Top toggle button leads to the extra-symbols page.
        val toggle = listOf(Key(KeyType.MODE_EXTRA_SYMBOLS, label = "#+="))
        val top = "[ ] { } # % ^ * + =".split(" ").map { Key(KeyType.CHAR, it) }
        val second = "_ \\ | ~ < > € £ ¥ • ÷ ×".split(" ").map { Key(KeyType.CHAR, it) }
        val function = listOf(
            Key(KeyType.MODE_NUMBER, label = "123"),
            Key(KeyType.CHAR, ","),
            Key(KeyType.SPACE, label = "space", width = KeyWidth.SPACE),
            Key(KeyType.CHAR, "."),
            Key(KeyType.ENTER, label = "\u23CE", width = KeyWidth.WIDE)
        )
        return listOf(toggle, top, second, function)
    }

    private fun extraSymbolRows(): List<List<Key>> {
        // Top button returns to the first symbols page.
        val back = listOf(Key(KeyType.MODE_SYMBOL, label = "1@\""))
        val top = "÷ ± × − – — … • °".split(" ").map { Key(KeyType.CHAR, it) }
        val second = "€ £ ¥ $ ¢ ≈ ≠ ≤ ≥ √ ∞".split(" ").map { Key(KeyType.CHAR, it) }
        val function = listOf(
            Key(KeyType.MODE_SYMBOL, label = "1@\""),
            Key(KeyType.CHAR, ","),
            Key(KeyType.SPACE, label = "space", width = KeyWidth.SPACE),
            Key(KeyType.CHAR, "."),
            Key(KeyType.ENTER, label = "\u23CE", width = KeyWidth.WIDE)
        )
        return listOf(back, top, second, function)
    }

    private fun shiftSymbol(): String = when {
        shift -> "\u2191"
        capsLock -> "\u25C9"
        else -> "\u21E7"
    }
}