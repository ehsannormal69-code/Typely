package com.typely.keyboard.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.typely.keyboard.settings.KeyboardSettings

@Composable
fun SoundScreen(
    settings: KeyboardSettings,
    onUpdate: ((KeyboardSettings) -> KeyboardSettings) -> Unit,
    onPlay: () -> Unit,
    onBack: () -> Unit
) = SettingsScaffold(title = "Sounds", onBack = onBack) {

    ToggleRow(
        "Typing sounds",
        settings.soundsEnabled,
        { onUpdate { s -> s.copy(soundsEnabled = it) } },
        subtitle = "Distinct sounds for keys, space, backspace, enter and special keys."
    )

    if (settings.soundsEnabled) {
        SectionTitle("Preview")
        Button(
            onClick = onPlay,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Play key sound")
        }
    }

    SectionTitle("Sound packs")
    InfoBox(
        "Typely ships with a default sound pack using an original, soft click. " +
            "Importing your own sounds and sound packs is planned for V1.1."
    )

    Text(
        "Volume follows your device media/sonification volume for now; a dedicated volume control is planned.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 12.dp)
    )
}