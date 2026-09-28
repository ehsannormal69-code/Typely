package com.typely.keyboard.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.typely.keyboard.settings.KeyboardSettings

enum class SettingsSection(val label: String) {
    Typing("Typing"),
    Appearance("Appearance"),
    Themes("Themes"),
    Sounds("Sounds"),
    Haptics("Haptics"),
    About("About")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsHomeScreen(
    settings: KeyboardSettings,
    onOpen: (SettingsSection) -> Unit,
    onReset: () -> Unit
) = SettingsScaffold(title = "Typely", onBack = null) {
    var confirming by remember { mutableStateOf(false) }

    Text(
        "Your keyboard. Your rules.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    SettingsSection.entries.forEach { section ->
        SettingsRow(title = section.label) { onOpen(section) }
    }

    SettingsRow(title = "Reset to defaults") { confirming = true }

    if (confirming) {
        ConfirmResetDialog(
            onConfirm = {
                onReset()
                confirming = false
            },
            onDismiss = { confirming = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsRow(title: String, onClick: () -> Unit) {
    androidx.compose.material3.Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}