package com.typely.keyboard.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.typely.keyboard.R

/**
 * Plays typing sounds through a [SoundPool]. Uses the real iPhone keyboard
 * sounds: the key press click for letters/space, the delete sound for
 * backspace, and the modifier sound for shift/enter/mode keys.
 */
class SoundManager(context: Context) {

    private val soundPool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val soundIds = mutableMapOf<SoundKind, Int>()

    init {
        soundIds[SoundKind.KEY] = soundPool.load(context, R.raw.key_click, 1)
        soundIds[SoundKind.SPACE] = soundPool.load(context, R.raw.key_click, 1)
        soundIds[SoundKind.BACKSPACE] = soundPool.load(context, R.raw.key_delete, 1)
        soundIds[SoundKind.ENTER] = soundPool.load(context, R.raw.key_modifier, 1)
        soundIds[SoundKind.SPECIAL] = soundPool.load(context, R.raw.key_modifier, 1)
    }

    fun play(kind: SoundKind) {
        soundIds[kind]?.takeIf { it != 0 }?.let { id ->
            soundPool.play(id, 1f, 1f, 1, 0, 1f)
        }
    }

    fun release() {
        soundPool.release()
    }
}

enum class SoundKind {
    KEY, SPACE, BACKSPACE, ENTER, SPECIAL
}