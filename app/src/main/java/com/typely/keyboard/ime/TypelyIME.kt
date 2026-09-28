package com.typely.keyboard.ime

import android.content.Intent
import android.content.res.ColorStateList
import android.inputmethodservice.InputMethodService
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.lifecycleScope
import com.typely.keyboard.haptics.HapticManager
import com.typely.keyboard.R
import com.typely.keyboard.settings.SettingsRepository
import com.typely.keyboard.sound.SoundKind
import com.typely.keyboard.sound.SoundManager
import com.typely.keyboard.ui.MainActivity
import kotlinx.coroutines.launch

class TypelyIME : InputMethodService(), LifecycleOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)

    private lateinit var controller: KeyboardController
    private lateinit var keyboardView: KeyboardView
    private lateinit var soundManager: SoundManager
    private lateinit var hapticManager: HapticManager
    private var settingsRepository: SettingsRepository? = null
    private var processor: KeyProcessor? = null
    private var collectingSettings = false
    private var toolbar: LinearLayout? = null
    private var settingsButton: ImageButton? = null
    private var hideButton: ImageButton? = null

    override val lifecycle: Lifecycle get() = lifecycleRegistry

    override fun onCreate() {
        super.onCreate()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        controller = KeyboardController()
        soundManager = SoundManager(this)
        hapticManager = HapticManager()
    }

    /**
     * Gboard-style input view: a slim toolbar with a hide chevron and a
     * settings gear above a fast custom-drawn keyboard.
     */
    override fun onCreateInputView(): View {
        keyboardView = KeyboardView(this) { key -> handleKey(key) }
        keyboardView.controller = controller

        toolbar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(8), 0, dp(8), 0)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(40)
            )
        }

        val spacer = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f)
        }

        hideButton = ImageButton(this, null, android.R.attr.borderlessButtonStyle).apply {
            setImageResource(R.drawable.ic_keyboard_chevron_down)
            contentDescription = "Hide keyboard"
            background = null
            setOnClickListener { requestHideSelf(0) }
            layoutParams = ViewGroup.LayoutParams(dp(36), dp(36))
        }

        settingsButton = ImageButton(this, null, android.R.attr.borderlessButtonStyle).apply {
            setImageResource(R.drawable.ic_settings)
            contentDescription = "Settings"
            background = null
            setOnClickListener {
                val intent = Intent(this@TypelyIME, MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            layoutParams = ViewGroup.LayoutParams(dp(36), dp(36))
        }

        toolbar?.apply {
            addView(hideButton)
            addView(spacer)
            addView(settingsButton)
        }

        val container = KeyboardContainer(this)
        container.addView(toolbar, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dp(40)
        ))
        container.addView(
            keyboardView,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        startSettingsCollection()
        return container
    }

    /**
     * Caps the whole IME (toolbar + keyboard) at a Gboard-like height so the
     * keyboard never covers the entire screen.
     */
    private inner class KeyboardContainer(context: android.content.Context) :
        LinearLayout(context) {

        init {
            orientation = LinearLayout.VERTICAL
        }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            val density = resources.displayMetrics.density
            val minHeightPx = (270 * density).toInt()
            // Gboard-like size: roughly a third of the display height.
            val autoHeightPx = maxOf(
                (resources.displayMetrics.heightPixels * 0.37f).toInt(),
                minHeightPx
            )
            val target = if (::keyboardView.isInitialized && keyboardView.settings.keyboardHeight > 0) {
                (keyboardView.settings.keyboardHeight * density).toInt()
            } else {
                autoHeightPx
            }.coerceAtLeast(minHeightPx)

            val mode = MeasureSpec.getMode(heightMeasureSpec)
            val size = MeasureSpec.getSize(heightMeasureSpec)
            val h = when (mode) {
                MeasureSpec.AT_MOST, MeasureSpec.EXACTLY -> minOf(size, target)
                else -> target
            }
            super.onMeasure(
                widthMeasureSpec,
                MeasureSpec.makeMeasureSpec(h.coerceAtLeast(minHeightPx), MeasureSpec.EXACTLY)
            )
        }
    }

    private fun startSettingsCollection() {
        if (collectingSettings) return
        collectingSettings = true
        val repo = settingsRepository ?: SettingsRepository(this).also {
            settingsRepository = it
        }
        lifecycleScope.launch {
            repo.settings.collect { settings ->
                if (::keyboardView.isInitialized) {
                    keyboardView.settings = settings
                    keyboardView.rebuild()
                }
                toolbar?.setBackgroundColor(settings.suggestionBarColor.toArgb())
                val tint = ColorStateList.valueOf(settings.textColor.toArgb())
                hideButton?.imageTintList = tint
                settingsButton?.imageTintList = tint
            }
        }
    }

    private fun dp(value: Int): Int {
        val scale = resources.displayMetrics.density
        return (value * scale).toInt()
    }

    private fun handleKey(key: Key) {
        val action = controller.process(key)
        if (action == null) return

        val settings = keyboardView.settings
        val input = currentInputConnection
        val kind = when (key.type) {
            KeyType.SPACE -> SoundKind.SPACE
            KeyType.BACKSPACE, KeyType.DELETE_WORD -> SoundKind.BACKSPACE
            KeyType.ENTER -> SoundKind.ENTER
            KeyType.SHIFT, KeyType.MODE_NUMBER, KeyType.MODE_SYMBOL,
            KeyType.MODE_EXTRA_SYMBOLS, KeyType.MODE_LETTERS -> SoundKind.SPECIAL
            else -> SoundKind.KEY
        }

        when (action) {
            is KeyAction.Output, is KeyAction.Space, is KeyAction.Delete,
            is KeyAction.DeleteWord, is KeyAction.Newline -> {
                keyboardProcessor().process(action, input)
                if (settings.soundsEnabled) soundManager.play(kind)
            }

            else -> {
                // UI-only actions (shift / mode switches) still get feedback.
                if (settings.soundsEnabled) soundManager.play(kind)
            }
        }

        if (settings.hapticsEnabled) {
            hapticManager.vibrate(keyboardView, settings.hapticLevel)
        }
        keyboardView.rebuild()
    }

    private fun keyboardProcessor(): KeyProcessor {
        val existing = processor
        if (existing != null) return existing
        val created = KeyProcessor { keyboardView.settings }
        processor = created
        return created
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        // onStartInput may be invoked before onCreateInputView, so guard access.
        if (!::keyboardView.isInitialized) return
        if (!restarting) {
            controller = KeyboardController()
            keyboardView.controller = controller
            processor?.onStartInput()
            keyboardView.rebuild()
        }
    }

    override fun onFinishInput() {
        super.onFinishInput()
        if (!::controller.isInitialized || !::keyboardView.isInitialized) return
        controller = KeyboardController()
        keyboardView.controller = controller
        processor?.onStartInput()
        keyboardView.rebuild()
    }

    override fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        soundManager.release()
        super.onDestroy()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean =
        super.onKeyDown(keyCode, event)
}