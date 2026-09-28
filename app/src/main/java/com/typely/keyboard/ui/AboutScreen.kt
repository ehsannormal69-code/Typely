package com.typely.keyboard.ui

import android.os.Build
import androidx.compose.runtime.Composable

@Composable
fun AboutScreen(onBack: () -> Unit) = SettingsScaffold(title = "About", onBack = onBack) {

    SectionTitle("Typely")
    InfoBox("Your keyboard. Your rules.\nVersion 0.1.0 (MVP)")

    SectionTitle("Privacy")
    InfoBox(
        "Typely keeps keyboard processing on-device. It does not require an account, " +
            "does not require internet access for typing, and does not collect your typed text. " +
            "Any future feature that sends text externally will be clearly explained and optional."
    )

    SectionTitle("Device")
    InfoBox("Android ${Build.VERSION.RELEASE} · API ${Build.VERSION.SDK_INT}")
}