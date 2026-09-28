package com.typely.keyboard.ime

enum class KeyType {
    CHAR,
    SHIFT,
    BACKSPACE,
    ENTER,
    SPACE,
    MODE_NUMBER,
    MODE_SYMBOL,
    MODE_EXTRA_SYMBOLS,
    MODE_LETTERS,
    DELETE_WORD
}

enum class KeyWidth {
    NORMAL,
    WIDE,
    EXTRA_WIDE,
    SPACE
}

data class Key(
    val type: KeyType,
    val primary: String = "",
    val label: String = primary,
    val width: KeyWidth = KeyWidth.NORMAL
)

sealed interface KeyAction {
    data class Output(val text: String) : KeyAction
    data object Delete : KeyAction
    data object DeleteWord : KeyAction
    data object Newline : KeyAction
    data object Space : KeyAction
    data object ToggleShift : KeyAction
    data object ToggleCapsLock : KeyAction
    data object SwitchToNumbers : KeyAction
    data object SwitchToSymbols : KeyAction
    data object SwitchToExtraSymbols : KeyAction
    data object SwitchToLetters : KeyAction
}