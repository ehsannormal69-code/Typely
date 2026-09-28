package com.typely.keyboard.ui

import androidx.compose.runtime.Composable
import com.typely.keyboard.settings.HapticLevel
import com.typely.keyboard.settings.KeyboardSettings

@Composable
fun HapticsScreen(
    settings: KeyboardSettings,
    onUpdate: ((KeyboardSettings) -> KeyboardSettings) -> Unit,
    onBack: () -> Unit
) = SettingsScaffold(title = "Haptics", onBack = onBack) {

    ToggleRow(
        "Haptic feedback",
        settings.hapticsEnabled,
        { onUpdate { s -> s.copy(hapticsEnabled = it) } }
    )

    if (settings.hapticsEnabled) {
        SectionTitle("Strength")
        SelectRow(
            "Haptic level",
            listOf("Light", "Medium", "Strong"),
            settings.hapticLevel.ordinal - 1,
            { index ->
                onUpdate { s -> s.copy(hapticLevel = HapticLevel.entries[index + 1]) }
            }
        )
        InfoBox("Special keys may feel slightly different to normal keys.")
    }
}