package com.typely.keyboard.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(
    onEnabled: () -> Unit,
    onConfigure: () -> Unit
) {
    val context = LocalContext.current
    var enabled by remember { mutableStateOf(isTypelyEnabled(context)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Welcome to Typely") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    ) { padding ->
        androidx.compose.foundation.layout.Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Your keyboard. Your rules.",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                "A fast, normal Android keyboard with deep customization — when you want it.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )

            androidx.compose.foundation.layout.Column(
                modifier = Modifier.padding(top = 28.dp)
            ) {
                SetupStep(
                    step = 1,
                    title = "Open keyboard settings",
                    detail = "This opens the Android keyboard settings.",
                    done = false
                )
                SetupStep(
                    step = 2,
                    title = "Enable Typely",
                    detail = "Turn on the Typely toggle in the enabled keyboards list.",
                    done = enabled
                )
                SetupStep(
                    step = 3,
                    title = "Set Typely as default",
                    detail = "Open the input method picker and choose Typely.",
                    done = false
                )
            }

            Button(
                onClick = { openKeyboardSettings(context) },
                modifier = Modifier.fillMaxWidth().padding(top = 28.dp)
            ) {
                Text("Open keyboard settings")
            }

            OutlinedButton(
                onClick = {
                    enabled = isTypelyEnabled(context)
                    if (enabled) onEnabled() else openInputMethodPicker(context)
                },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Text("I've enabled it — continue")
            }

            OutlinedButton(
                onClick = onConfigure,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Text("Skip for now")
            }
        }
    }
}

@Composable
private fun SetupStep(step: Int, title: String, detail: String, done: Boolean) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (done) {
            Icon(
                Icons.Filled.CheckCircle,
                contentDescription = "Done",
                tint = MaterialTheme.colorScheme.primary
            )
        } else {
            Text(
                "$step.",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 6.dp)
            )
        }
        androidx.compose.foundation.layout.Column(Modifier.padding(start = 8.dp).weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}