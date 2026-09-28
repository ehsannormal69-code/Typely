package com.typely.keyboard.theme

import androidx.compose.ui.graphics.Color

/**
 * A named theme = full coloring applied to the keyboard.
 * More aspects (background image, sounds, haptics, layout)
 * join in V1.1 per the PRD.
 */
data class KeyboardTheme(
    val name: String,
    val backgroundColor: Color,
    val keyColor: Color,
    val pressedKeyColor: Color,
    val textColor: Color,
    val accentColor: Color,
    val suggestionBarColor: Color
)

object ThemeManager {

    val builtIn = listOf(
        KeyboardTheme(
            name = "Midnight",
            backgroundColor = Color(0xFF101010),
            keyColor = Color(0xFF202020),
            pressedKeyColor = Color(0xFF2E2E2E),
            textColor = Color(0xFFFFFFFF),
            accentColor = Color(0xFF7C4DFF),
            suggestionBarColor = Color(0xFF161616)
        ),
        KeyboardTheme(
            name = "Light",
            backgroundColor = Color(0xFFECEFF1),
            keyColor = Color(0xFFFFFFFF),
            pressedKeyColor = Color(0xFFCFD8DC),
            textColor = Color(0xFF263238),
            accentColor = Color(0xFF0288D1),
            suggestionBarColor = Color(0xFFF5F5F5)
        ),
        KeyboardTheme(
            name = "Slate",
            backgroundColor = Color(0xFF263238),
            keyColor = Color(0xFF37474F),
            pressedKeyColor = Color(0xFF455A64),
            textColor = Color(0xFFECEFF1),
            accentColor = Color(0xFF4DD0E1),
            suggestionBarColor = Color(0xFF1C272B)
        ),
        KeyboardTheme(
            name = "Rose",
            backgroundColor = Color(0xFF1E1016),
            keyColor = Color(0xFF3A1F2A),
            pressedKeyColor = Color(0xFF4E2836),
            textColor = Color(0xFFFFE4EB),
            accentColor = Color(0xFFE91E63),
            suggestionBarColor = Color(0xFF2A1620)
        ),
        KeyboardTheme(
            name = "Forest",
            backgroundColor = Color(0xFF0F1B12),
            keyColor = Color(0xFF16261B),
            pressedKeyColor = Color(0xFF1E3323),
            textColor = Color(0xFFE6F0E8),
            accentColor = Color(0xFF2ECC71),
            suggestionBarColor = Color(0xFF15221A)
        )
    )
}