package com.typely.keyboard.haptics

import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.View
import com.typely.keyboard.settings.HapticLevel

/**
 * Handles haptic feedback for key presses. Uses only the system's built-in
 * lightweight haptic taps (respecting the device's touch-feedback setting)
 * rather than raw vibration, so it feels crisp instead of buzzy.
 */
class HapticManager {

    fun vibrate(host: View?, level: HapticLevel) {
        if (level == HapticLevel.OFF) return
        if (host == null || !host.isHapticFeedbackEnabled) return
        try {
            when (level) {
                HapticLevel.LIGHT ->
                    host.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                HapticLevel.MEDIUM ->
                    host.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                HapticLevel.STRONG ->
                    host.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                HapticLevel.OFF -> Unit
            }
        } catch (e: Exception) {
            Log.w("TypelyHaptics", "Haptic feedback failed", e)
        }
    }
}