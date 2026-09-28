package com.typely.keyboard.settings

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.preferences.core.Preferences
import com.typely.keyboard.settings.SettingsDataStore.Keys
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Centralized settings store. The keyboard reads this Flow so every
 * toggle/color change applies immediately without restarting the IME.
 */
class SettingsRepository(context: Context) {

    private val dataStore = context.settingsDataStore

    val settings: Flow<KeyboardSettings> = dataStore.data.map { prefs -> prefs.toSettings() }

    suspend fun update(transform: (KeyboardSettings) -> KeyboardSettings) {
        val next = dataStore.data.first().toSettings().let(transform)
        dataStore.updateData { prefs -> prefs.applySettings(next) }
    }

    suspend fun resetToDefaults() {
        dataStore.updateData { it.applySettings(KeyboardSettings()) }
    }

    private fun Preferences.toSettings(): KeyboardSettings {
        val default = KeyboardSettings()
        fun color(key: Preferences.Key<Int>, fallback: Color): Color =
            this[key]?.let { Color(it) } ?: fallback

        return KeyboardSettings(
            autoCapitalize = this[Keys.AUTO_CAPITALIZE] ?: default.autoCapitalize,
            autoCorrect = this[Keys.AUTO_CORRECT] ?: default.autoCorrect,
            suggestions = this[Keys.SUGGESTIONS] ?: default.suggestions,
            predictiveText = this[Keys.PREDICTIVE_TEXT] ?: default.predictiveText,
            doubleSpacePeriod = this[Keys.DOUBLE_SPACE_PERIOD] ?: default.doubleSpacePeriod,
            autoSpacingEnabled = this[Keys.AUTO_SPACING] ?: default.autoSpacingEnabled,
            emojiSuggestions = this[Keys.EMOJI_SUGGESTIONS] ?: default.emojiSuggestions,
            swipeTyping = this[Keys.SWIPE_TYPING] ?: default.swipeTyping,
            soundsEnabled = this[Keys.SOUNDS_ENABLED] ?: default.soundsEnabled,
            hapticsEnabled = this[Keys.HAPTICS_ENABLED] ?: default.hapticsEnabled,
            hapticLevel = HapticLevel.entries.getOrElse(
                this[Keys.HAPTIC_LEVEL] ?: default.hapticLevel.ordinal
            ) { default.hapticLevel },
            animationsEnabled = this[Keys.ANIMATIONS_ENABLED] ?: default.animationsEnabled,
            numberRowEnabled = this[Keys.NUMBER_ROW_ENABLED] ?: default.numberRowEnabled,
            keyboardHeight = this[Keys.KEYBOARD_HEIGHT] ?: default.keyboardHeight,
            backgroundColor = color(Keys.BACKGROUND_COLOR, default.backgroundColor),
            keyColor = color(Keys.KEY_COLOR, default.keyColor),
            pressedKeyColor = color(Keys.PRESSED_KEY_COLOR, default.pressedKeyColor),
            textColor = color(Keys.TEXT_COLOR, default.textColor),
            accentColor = color(Keys.ACCENT_COLOR, default.accentColor),
            suggestionBarColor = color(Keys.SUGGESTION_BAR_COLOR, default.suggestionBarColor),
            borderColor = color(Keys.BORDER_COLOR, default.borderColor),
            backgroundImagePath = this[Keys.BACKGROUND_IMAGE_PATH] ?: default.backgroundImagePath,
            backgroundDim = this[Keys.BACKGROUND_DIM] ?: default.backgroundDim,
            keyCornerRadius = this[Keys.KEY_CORNER_RADIUS] ?: default.keyCornerRadius,
            keySpacing = this[Keys.KEY_SPACING] ?: default.keySpacing,
            keyTransparency = this[Keys.KEY_TRANSPARENCY] ?: default.keyTransparency
        )
    }

    private fun Preferences.applySettings(s: KeyboardSettings): Preferences {
        val editor = toMutablePreferences()
        editor[Keys.AUTO_CAPITALIZE] = s.autoCapitalize
        editor[Keys.AUTO_CORRECT] = s.autoCorrect
        editor[Keys.SUGGESTIONS] = s.suggestions
        editor[Keys.PREDICTIVE_TEXT] = s.predictiveText
        editor[Keys.DOUBLE_SPACE_PERIOD] = s.doubleSpacePeriod
        editor[Keys.AUTO_SPACING] = s.autoSpacingEnabled
        editor[Keys.EMOJI_SUGGESTIONS] = s.emojiSuggestions
        editor[Keys.SWIPE_TYPING] = s.swipeTyping
        editor[Keys.SOUNDS_ENABLED] = s.soundsEnabled
        editor[Keys.HAPTICS_ENABLED] = s.hapticsEnabled
        editor[Keys.HAPTIC_LEVEL] = s.hapticLevel.ordinal
        editor[Keys.ANIMATIONS_ENABLED] = s.animationsEnabled
        editor[Keys.NUMBER_ROW_ENABLED] = s.numberRowEnabled
        editor[Keys.KEYBOARD_HEIGHT] = s.keyboardHeight
        editor[Keys.BACKGROUND_COLOR] = s.backgroundColor.toArgb()
        editor[Keys.KEY_COLOR] = s.keyColor.toArgb()
        editor[Keys.PRESSED_KEY_COLOR] = s.pressedKeyColor.toArgb()
        editor[Keys.TEXT_COLOR] = s.textColor.toArgb()
        editor[Keys.ACCENT_COLOR] = s.accentColor.toArgb()
        editor[Keys.SUGGESTION_BAR_COLOR] = s.suggestionBarColor.toArgb()
        editor[Keys.BORDER_COLOR] = s.borderColor.toArgb()
        editor[Keys.BACKGROUND_IMAGE_PATH] = s.backgroundImagePath
        editor[Keys.BACKGROUND_DIM] = s.backgroundDim
        editor[Keys.KEY_CORNER_RADIUS] = s.keyCornerRadius
        editor[Keys.KEY_SPACING] = s.keySpacing
        editor[Keys.KEY_TRANSPARENCY] = s.keyTransparency
        return editor
    }
}