package com.typely.keyboard.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.typely.keyboard.sound.SoundKind
import com.typely.keyboard.sound.SoundManager

@Composable
fun TypelyApp(viewModel: SettingsViewModel = viewModel()) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    var started by remember { mutableStateOf(isTypelyEnabled(context)) }

    androidx.compose.material3.MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) {
            androidx.compose.material3.darkColorScheme()
        } else {
            androidx.compose.material3.lightColorScheme()
        }
    ) {
        if (!started) {
            SetupScreen(
                onEnabled = { started = true },
                onConfigure = { started = true }
            )
            return@MaterialTheme
        }

        val stack = remember { mutableStateListOf<SettingsSection>() }
        val onHome = stack.isEmpty()

        BackHandler(enabled = !onHome) {
            stack.removeAt(stack.lastIndex)
        }

        val close: () -> Unit = { if (!onHome) stack.removeAt(stack.lastIndex) }

        when {
            onHome -> SettingsHomeScreen(
                settings = settings,
                onOpen = { stack.add(it) },
                onReset = viewModel::resetAll
            )

            stack.lastOrNull() == SettingsSection.Typing -> TypingScreen(
                settings = settings,
                onUpdate = viewModel::update,
                onBack = close
            )

            stack.lastOrNull() == SettingsSection.Appearance -> AppearanceScreen(
                settings = settings,
                onUpdate = viewModel::update,
                onBack = close
            )

            stack.lastOrNull() == SettingsSection.Themes -> ThemeScreen(
                currentBackground = settings.backgroundColor,
                currentKey = settings.keyColor,
                onApply = viewModel::applyTheme,
                onBack = close
            )

            stack.lastOrNull() == SettingsSection.Sounds -> SoundScreen(
                settings = settings,
                onUpdate = viewModel::update,
                onPlay = {
                    val sm = SoundManager(context)
                    sm.play(SoundKind.KEY)
                    sm.release()
                },
                onBack = close
            )

            stack.lastOrNull() == SettingsSection.Haptics -> HapticsScreen(
                settings = settings,
                onUpdate = viewModel::update,
                onBack = close
            )

            stack.lastOrNull() == SettingsSection.About -> AboutScreen(onBack = close)
        }
    }
}