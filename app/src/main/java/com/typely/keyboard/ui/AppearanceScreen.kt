package com.typely.keyboard.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.typely.keyboard.settings.KeyboardSettings

@Composable
fun AppearanceScreen(
    settings: KeyboardSettings,
    onUpdate: ((KeyboardSettings) -> KeyboardSettings) -> Unit,
    onBack: () -> Unit
) = SettingsScaffold(title = "Appearance", onBack = onBack) {

    SectionTitle("Colors")
    HexColorRow("Keyboard background", settings.backgroundColor) {
        onUpdate { s -> s.copy(backgroundColor = it) }
    }
    HexColorRow("Key color", settings.keyColor) { onUpdate { s -> s.copy(keyColor = it) } }
    HexColorRow("Pressed key color", settings.pressedKeyColor) {
        onUpdate { s -> s.copy(pressedKeyColor = it) }
    }
    HexColorRow("Text color", settings.textColor) { onUpdate { s -> s.copy(textColor = it) } }
    HexColorRow("Accent color", settings.accentColor) { onUpdate { s -> s.copy(accentColor = it) } }
    HexColorRow("Suggestion bar color", settings.suggestionBarColor) {
        onUpdate { s -> s.copy(suggestionBarColor = it) }
    }

    SectionTitle("Background image")
    BackgroundPicker(
        hasImage = settings.backgroundImagePath.isNotBlank(),
        onPicked = { path ->
            onUpdate { s -> s.copy(backgroundImagePath = path) }
        },
        onRemove = {
            onUpdate { s -> s.copy(backgroundImagePath = "") }
        }
    )

    SectionTitle("Keys")
    SliderRow(
        "Corner radius",
        settings.keyCornerRadius,
        0f..24f,
        { onUpdate { s -> s.copy(keyCornerRadius = it) } },
        secondaryText = "${settings.keyCornerRadius.toInt()}dp"
    )
    SliderRow(
        "Key spacing",
        settings.keySpacing,
        0f..16f,
        { onUpdate { s -> s.copy(keySpacing = it) } },
        secondaryText = "${settings.keySpacing.toInt()}dp"
    )
    SliderRow(
        "Key transparency",
        settings.keyTransparency.toFloat(),
        0f..255f,
        { onUpdate { s -> s.copy(keyTransparency = it.toInt()) } },
        secondaryText = "${(settings.keyTransparency * 100 / 255)}%"
    )
}

@Composable
private fun BackgroundPicker(
    hasImage: Boolean,
    onPicked: (String) -> Unit,
    onRemove: () -> Unit
) {
    val context = LocalContext.current
    val store = remember(context) { BackgroundImageStore(context) }

    val pickLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            store.import(uri)?.let(onPicked)
        }
    }

    Button(
        onClick = {
            pickLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        },
        modifier = androidx.compose.ui.Modifier.fillMaxWidth()
    ) {
        Text(if (hasImage) "Replace image" else "Choose image")
    }
    if (hasImage) {
        TextButton(onClick = onRemove, modifier = androidx.compose.ui.Modifier.fillMaxWidth()) {
            Text("Remove image")
        }
        Text(
            "The image is dimmed behind the keys to stay readable. Opacity, blur and position controls arrive in V1.1.",
            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}