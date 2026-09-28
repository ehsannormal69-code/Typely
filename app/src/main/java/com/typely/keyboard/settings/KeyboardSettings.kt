package com.typely.keyboard.settings

import androidx.compose.ui.graphics.Color

enum class HapticLevel { OFF, LIGHT, MEDIUM, STRONG }

data class KeyboardSettings(
    val autoCapitalize: Boolean = true,
    val autoCorrect: Boolean = true,
    val suggestions: Boolean = true,
    val predictiveText: Boolean = true,
    val doubleSpacePeriod: Boolean = true,
    val autoSpacingEnabled: Boolean = true,
    val emojiSuggestions: Boolean = false,
    val swipeTyping: Boolean = false,
    val soundsEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val hapticLevel: HapticLevel = HapticLevel.MEDIUM,
    val animationsEnabled: Boolean = true,
    val numberRowEnabled: Boolean = false,
    val keyboardHeight: Int = 0,
    val backgroundColor: Color = Color(0xFF101010),
    val keyColor: Color = Color(0xFF202020),
    val pressedKeyColor: Color = Color(0xFF2E2E2E),
    val textColor: Color = Color(0xFFFFFFFF),
    val accentColor: Color = Color(0xFF7C4DFF),
    val suggestionBarColor: Color = Color(0xFF161616),
    val borderColor: Color = Color(0x00000000),
    val backgroundImagePath: String = "",
    val backgroundDim: Float = 0.55f,
    val keyCornerRadius: Float = 8f,
    val keySpacing: Float = 3f,
    val keyTransparency: Int = 255
)