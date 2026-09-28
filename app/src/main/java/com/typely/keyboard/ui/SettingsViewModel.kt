package com.typely.keyboard.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.typely.keyboard.settings.HapticLevel
import com.typely.keyboard.settings.KeyboardSettings
import com.typely.keyboard.settings.SettingsRepository
import com.typely.keyboard.theme.KeyboardTheme
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SettingsRepository(application)

    val settings: StateFlow<KeyboardSettings> = repository.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, KeyboardSettings())

    fun update(transform: (KeyboardSettings) -> KeyboardSettings) {
        viewModelScope.launch { repository.update(transform) }
    }

    fun applyTheme(theme: KeyboardTheme) {
        update { it.copy(
            backgroundColor = theme.backgroundColor,
            keyColor = theme.keyColor,
            pressedKeyColor = theme.pressedKeyColor,
            textColor = theme.textColor,
            accentColor = theme.accentColor,
            suggestionBarColor = theme.suggestionBarColor
        ) }
    }

    fun setHapticLevel(level: HapticLevel) =
        update { it.copy(hapticLevel = level, hapticsEnabled = level != HapticLevel.OFF) }

    fun resetAll() {
        viewModelScope.launch { repository.resetToDefaults() }
    }
}