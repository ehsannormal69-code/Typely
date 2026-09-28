package com.typely.keyboard.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "typely_settings"
)

object SettingsDataStore {
    object Keys {
        val AUTO_CAPITALIZE = booleanPreferencesKey("auto_capitalize")
        val AUTO_CORRECT = booleanPreferencesKey("auto_correct")
        val SUGGESTIONS = booleanPreferencesKey("suggestions")
        val PREDICTIVE_TEXT = booleanPreferencesKey("predictive_text")
        val DOUBLE_SPACE_PERIOD = booleanPreferencesKey("double_space_period")
        val AUTO_SPACING = booleanPreferencesKey("auto_spacing")
        val EMOJI_SUGGESTIONS = booleanPreferencesKey("emoji_suggestions")
        val SWIPE_TYPING = booleanPreferencesKey("swipe_typing")
        val SOUNDS_ENABLED = booleanPreferencesKey("sounds_enabled")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val HAPTIC_LEVEL = intPreferencesKey("haptic_level")
        val ANIMATIONS_ENABLED = booleanPreferencesKey("animations_enabled")
        val NUMBER_ROW_ENABLED = booleanPreferencesKey("number_row_enabled")
        val KEYBOARD_HEIGHT = intPreferencesKey("keyboard_height")
        val BACKGROUND_COLOR = intPreferencesKey("background_color")
        val KEY_COLOR = intPreferencesKey("key_color")
        val PRESSED_KEY_COLOR = intPreferencesKey("pressed_key_color")
        val TEXT_COLOR = intPreferencesKey("text_color")
        val ACCENT_COLOR = intPreferencesKey("accent_color")
        val SUGGESTION_BAR_COLOR = intPreferencesKey("suggestion_bar_color")
        val BORDER_COLOR = intPreferencesKey("border_color")
        val BACKGROUND_IMAGE_PATH = stringPreferencesKey("background_image_path")
        val BACKGROUND_DIM = floatPreferencesKey("background_dim")
        val KEY_CORNER_RADIUS = floatPreferencesKey("key_corner_radius")
        val KEY_SPACING = floatPreferencesKey("key_spacing")
        val KEY_TRANSPARENCY = intPreferencesKey("key_transparency")
    }
}