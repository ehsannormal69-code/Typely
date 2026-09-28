package com.typely.keyboard.ui

import androidx.compose.runtime.Composable
import com.typely.keyboard.settings.KeyboardSettings

@Composable
fun TypingScreen(
    settings: KeyboardSettings,
    onUpdate: ((KeyboardSettings) -> KeyboardSettings) -> Unit,
    onBack: () -> Unit
) = SettingsScaffold(title = "Typing", onBack = onBack) {
    SectionTitle("Behavior")
    ToggleRow(
        "Auto-capitalization",
        settings.autoCapitalize,
        { onUpdate { s -> s.copy(autoCapitalize = it) } }
    )
    ToggleRow(
        "Auto-correct",
        settings.autoCorrect,
        { onUpdate { s -> s.copy(autoCorrect = it) } },
        subtitle = "Automatic correction of common mistakes. On-device only."
    )
    ToggleRow(
        "Word suggestions",
        settings.suggestions,
        { onUpdate { s -> s.copy(suggestions = it) } }
    )
    ToggleRow(
        "Predictive text",
        settings.predictiveText,
        { onUpdate { s -> s.copy(predictiveText = it) } }
    )
    ToggleRow(
        "Double-space period",
        settings.doubleSpacePeriod,
        { onUpdate { s -> s.copy(doubleSpacePeriod = it) } },
        subtitle = "Press space twice to insert \". \""
    )
    ToggleRow(
        "Automatic spacing",
        settings.autoSpacingEnabled,
        { onUpdate { s -> s.copy(autoSpacingEnabled = it) } }
    )
    ToggleRow(
        "Emoji suggestions",
        settings.emojiSuggestions,
        { onUpdate { s -> s.copy(emojiSuggestions = it) } }
    )

    SectionTitle("Input methods")
    ToggleRow(
        "Swipe typing",
        settings.swipeTyping,
        { onUpdate { s -> s.copy(swipeTyping = it) } },
        subtitle = "Coming soon — currently a placeholder."
    )
}