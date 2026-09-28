package com.typely.keyboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.typely.keyboard.theme.ThemeManager

@Composable
fun ThemeScreen(
    currentBackground: androidx.compose.ui.graphics.Color,
    currentKey: androidx.compose.ui.graphics.Color,
    onApply: (com.typely.keyboard.theme.KeyboardTheme) -> Unit,
    onBack: () -> Unit
) = SettingsScaffold(title = "Themes", onBack = onBack) {

    Text(
        "Tap a theme to apply it. Themes combine colors for the whole keyboard. Saving, duplicating and sharing themes arrive in V1.1.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    ThemeManager.builtIn.forEach { theme ->
        val isActive = theme.backgroundColor == currentBackground && theme.keyColor == currentKey
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (isActive) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant
                )
                .clickable { onApply(theme) }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(
                    Modifier
                        .size(26.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(theme.backgroundColor)
                )
                Box(
                    Modifier
                        .size(26.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(theme.keyColor)
                )
                Box(
                    Modifier
                        .size(26.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(theme.accentColor)
                )
            }
            Text(
                theme.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            if (isActive) {
                Text(
                    "Active",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}