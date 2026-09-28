package com.typely.keyboard.ime

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import androidx.compose.ui.graphics.toArgb
import com.typely.keyboard.settings.KeyboardSettings

/**
 * Custom-drawn keyboard with a Gboard-like look: flat rounded keys, vector
 * icons for modifier keys, a letter popup while typing, and long-press
 * repeat for the backspace / delete-word keys.
 */
class KeyboardView(
    context: Context,
    private val onKeyPress: (Key) -> Unit
) : View(context) {

    private val keyPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    var settings: KeyboardSettings = KeyboardSettings()
        set(value) {
            if (field.backgroundImagePath != value.backgroundImagePath) {
                loadBackground(value.backgroundImagePath)
            }
            field = value
            invalidate()
        }

    private var backgroundBitmap: Bitmap? = null
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val dimPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private fun loadBackground(path: String) {
        if (path.isBlank()) {
            backgroundBitmap = null
            return
        }
        try {
            backgroundBitmap = BitmapFactory.decodeFile(path)
        } catch (_: Exception) {
            backgroundBitmap = null
        }
    }

    var controller: KeyboardController? = null
        set(value) {
            field = value
            rebuild()
        }

    private var keyRects: MutableList<MutableList<Pair<Key, RectF>>> = mutableListOf()
    private var rows: List<List<Key>> = emptyList()
    private var pressedRect: RectF? = null
    private var pressedKey: Key? = null

    // Long-press auto-repeat for backspace / delete-word.
    private val repeatHandler = Handler(Looper.getMainLooper())
    private var repeatRunnable: Runnable? = null
    private var repeated = false
    private var repeatKey: Key? = null

    private val repeatTask = object : Runnable {
        override fun run() {
            if (repeatKey == null) return
            if (pressedRect != null && repeatBounds != pressedRect) {
                stopRepeat()
                return
            }
            repeated = true
            onKeyPress(repeatKey!!)
            postRepeat()
        }
    }

    private var repeatBounds: RectF? = null

    fun rebuild() {
        controller?.let {
            rows = it.rows
            computeLayout(width.toFloat(), height.toFloat())
        }
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        computeLayout(w.toFloat(), h.toFloat())
    }

    private fun computeLayout(w: Float, h: Float) {
        keyRects = mutableListOf()
        if (w <= 0 || h <= 0 || rows.isEmpty()) return

        val density = resources.displayMetrics.density
        // Gboard-style: inset the key grid slightly from the screen edges.
        val sideInset = 3 * density
        val topInset = 6 * density
        val bottomInset = 7 * density
        val innerW = w - sideInset * 2
        val innerH = h - topInset - bottomInset

        val spacing = settings.keySpacing.toFloat()
        val rowGap = spacing
        val nRow = rows.size
        val rowH = (innerH - rowGap * (nRow - 1)) / nRow

        rows.forEachIndexed { r, row ->
            val totalWeight = row.fold(0f) { acc, k -> acc + k.width.weight }
            val gapTotal = spacing * (row.size - 1)
            val unit = (innerW - gapTotal) / totalWeight
            val rects = mutableListOf<Pair<Key, RectF>>()
            var x = sideInset
            row.forEach { key ->
                val kw = unit * key.width.weight
                val top = topInset + r * (rowH + rowGap)
                val rect = RectF(x, top, x + kw, top + rowH)
                x += kw + spacing
                rects.add(key to rect)
            }
            keyRects.add(rects)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val s = settings
        val density = resources.displayMetrics.density

        // Background
        canvas.drawColor(s.backgroundColor.toArgb())

        // Optional image background, dimmed so keys stay readable.
        backgroundBitmap?.let { bmp ->
            val dest = scaleCoverRect(bmp.width.toFloat(), bmp.height.toFloat(), width.toFloat(), height.toFloat())
            canvas.drawBitmap(bmp, null, dest, backgroundPaint)
            dimPaint.color = AndroidColor.argb(
                (255 * s.backgroundDim).toInt().coerceIn(0, 255), 0, 0, 0
            )
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), dimPaint)
        }

        keyRects.forEach { row ->
            row.forEach { (key, rect) ->
                val isPressed = rect == pressedRect
                val radius = keyRadius(rect, density)
                val fill = if (isPressed) s.pressedKeyColor else s.keyColor
                keyPaint.style = Paint.Style.FILL
                keyPaint.color = withAlpha(fill.toArgb(), s.keyTransparency)
                canvas.drawRoundRect(rect, radius, radius, keyPaint)

                // Subtle border, only if the user asked for one.
                if (s.borderColor.alpha != 0f) {
                    keyPaint.style = Paint.Style.STROKE
                    keyPaint.strokeWidth = 1.5f
                    keyPaint.color = s.borderColor.toArgb()
                    canvas.drawRoundRect(rect, radius, radius, keyPaint)
                }

                drawKeyContent(canvas, key, rect, s, density)
            }
        }

        // Gboard-style letter popup preview.
        val pk = pressedKey
        val pr = pressedRect
        if (pk != null && pk.type == KeyType.CHAR && pr != null) {
            drawPopup(canvas, pk, pr, s, density)
        }
    }

    private fun keyRadius(rect: RectF, density: Float): Float {
        val maxCorner = settings.keyCornerRadius * density
        return minOf(rect.height() * 0.22f, maxCorner)
    }

    private fun drawKeyContent(
        canvas: Canvas,
        key: Key,
        rect: RectF,
        s: KeyboardSettings,
        density: Float
    ) {
        when (key.type) {
            KeyType.CHAR -> {
                val displayText = if (controller?.let { it.shift || it.capsLock } == true) {
                    key.label.uppercase()
                } else {
                    key.label
                }
                drawLabel(canvas, displayText, rect, s.textColor.toArgb(), 0.42f, 22f * density)
            }

            KeyType.SHIFT -> {
                val iconColor = when {
                    controller?.capsLock == true -> s.accentColor.toArgb()
                    controller?.shift == true -> s.textColor.toArgb()
                    else -> s.textColor.toArgb()
                }
                drawShiftIcon(canvas, rect, iconColor, controller?.capsLock == true)
            }

            KeyType.BACKSPACE, KeyType.DELETE_WORD ->
                drawBackspaceIcon(canvas, rect, s.textColor.toArgb())

            KeyType.ENTER ->
                drawEnterIcon(canvas, rect, s.textColor.toArgb())

            KeyType.MODE_NUMBER, KeyType.MODE_SYMBOL, KeyType.MODE_EXTRA_SYMBOLS,
            KeyType.MODE_LETTERS ->
                drawLabel(canvas, key.label, rect, s.textColor.toArgb(), 0.30f, 16f * density)

            KeyType.SPACE -> Unit
        }
    }

    private fun drawLabel(
        canvas: Canvas,
        text: String,
        rect: RectF,
        color: Int,
        ratio: Float,
        cap: Float
    ) {
        if (text.isEmpty()) return
        textPaint.color = color
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = minOf(rect.height() * ratio, cap)
        val baseline = rect.centerY() - (textPaint.descent() + textPaint.ascent()) / 2
        canvas.drawText(text, rect.centerX(), baseline, textPaint)
    }

    private fun strokeConfig(density: Float): Float = 2.2f * density

    private fun drawShiftIcon(canvas: Canvas, rect: RectF, color: Int, filled: Boolean) {
        iconPaint.color = color
        iconPaint.strokeWidth = strokeConfig(resources.displayMetrics.density)
        val cx = rect.centerX()
        val cy = rect.centerY()
        val unit = minOf(rect.width(), rect.height()) * 0.45f

        val path = Path()
        path.moveTo(cx, cy - unit * 0.55f)
        path.lineTo(cx + unit * 0.55f, cy)
        path.lineTo(cx + unit * 0.16f, cy)
        path.lineTo(cx + unit * 0.16f, cy + unit * 0.5f)
        path.lineTo(cx - unit * 0.16f, cy + unit * 0.5f)
        path.lineTo(cx - unit * 0.16f, cy)
        path.lineTo(cx - unit * 0.55f, cy)
        path.close()
        iconPaint.style = if (filled) Paint.Style.FILL else Paint.Style.STROKE
        canvas.drawPath(path, iconPaint)
    }

    private fun drawBackspaceIcon(canvas: Canvas, rect: RectF, color: Int) {
        iconPaint.color = color
        iconPaint.strokeWidth = strokeConfig(resources.displayMetrics.density)
        val density = resources.displayMetrics.density
        val cx = rect.centerX()
        val cy = rect.centerY()
        val unit = minOf(rect.width(), rect.height()) * 0.42f

        // Left-pointing arrow body.
        val body = Path()
        body.moveTo(cx - unit * 0.5f, cy)
        body.lineTo(cx - unit * 0.1f, cy - unit * 0.6f)
        body.lineTo(cx + unit * 0.35f, cy - unit * 0.6f)
        body.lineTo(cx + unit * 0.35f, cy + unit * 0.6f)
        body.lineTo(cx - unit * 0.1f, cy + unit * 0.6f)
        body.close()
        iconPaint.style = Paint.Style.FILL
        canvas.drawPath(body, iconPaint)

        // Cross ("delete") on the right.
        iconPaint.style = Paint.Style.STROKE
        val x0 = cx + unit * 0.62f
        val x1 = cx + unit * 1.1f
        canvas.drawLine(x0, cy - unit * 0.35f, x1, cy + unit * 0.35f, iconPaint)
        canvas.drawLine(x0, cy + unit * 0.35f, x1, cy - unit * 0.35f, iconPaint)
    }

    private fun drawEnterIcon(canvas: Canvas, rect: RectF, color: Int) {
        iconPaint.color = color
        iconPaint.strokeWidth = strokeConfig(resources.displayMetrics.density)
        val cx = rect.centerX()
        val cy = rect.centerY()
        val unit = minOf(rect.width(), rect.height()) * 0.38f

        val path = Path()
        path.moveTo(cx - unit * 0.7f, cy - unit * 0.35f)
        path.lineTo(cx + unit * 0.1f, cy - unit * 0.35f)
        path.lineTo(cx + unit * 0.1f, cy + unit * 0.4f)
        path.lineTo(cx - unit * 0.2f, cy + unit * 0.15f)
        path.moveTo(cx + unit * 0.1f, cy + unit * 0.4f)
        path.lineTo(cx + unit * 0.4f, cy + unit * 0.15f)
        iconPaint.style = Paint.Style.STROKE
        canvas.drawPath(path, iconPaint)
    }

    /** Gboard-style popup showing the pressed character above the key. */
    private fun drawPopup(
        canvas: Canvas,
        key: Key,
        originalRect: RectF,
        s: KeyboardSettings,
        density: Float
    ) {
        val unit = minOf(originalRect.width(), originalRect.height()) * 0.45f
        val popW = originalRect.width() * 1.55f
        val popH = unit * 2.4f
        val cx = originalRect.centerX()
        val top = originalRect.top - popH + unit * 0.9f
        val pop = RectF(cx - popW / 2, top, cx + popW / 2, top + popH)
        val radius = minOf(pop.height() * 0.25f, 12f * density)
        val radiusOrig = keyRadius(originalRect, density)

        // The popup inherits the key fill so it reads as one pressed unit.
        keyPaint.style = Paint.Style.FILL
        keyPaint.color = withAlpha(s.keyColor.toArgb(), s.keyTransparency)
        canvas.drawRoundRect(pop, radius, radius, keyPaint)

        // Connect the popup to the key with a rounded notch.
        val bridge = RectF(pop.left, pop.bottom - unit * 0.8f, pop.right, pop.bottom + radiusOrig)
        canvas.drawRoundRect(bridge, radius, radius, keyPaint)

        val displayText = if (controller?.shift == true || controller?.capsLock == true) {
            key.label.uppercase()
        } else {
            key.label
        }
        drawLabel(canvas, displayText, pop, s.textColor.toArgb(), 0.5f, 34f * density)
    }

    private fun scaleCoverRect(bw: Float, bh: Float, vw: Float, vh: Float): RectF {
        val scale = maxOf(vw / bw, vh / bh)
        val dw = bw * scale
        val dh = bh * scale
        val dx = (vw - dw) / 2f
        val dy = (vh - dh) / 2f
        return RectF(dx, dy, dx + dw, dy + dh)
    }

    private fun withAlpha(color: Int, alpha: Int): Int {
        val a = (color ushr 24) * alpha / 255
        return (a shl 24) or (color and 0x00FFFFFF)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                pressedRect = findKeyRect(event.x, event.y)
                pressedKey = findKey(event.x, event.y)
                val pr = pressedRect
                val pk = pressedKey
                if (pr != null && pk != null) {
                    startRepeatIfNeeded(pk, pr)
                }
                invalidate()
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val next = findKeyRect(event.x, event.y)
                if (next != pressedRect) {
                    pressedRect = next
                    pressedKey = next?.let { findKey(event.x, event.y) }
                    invalidate()
                }
                return true
            }

            MotionEvent.ACTION_UP -> {
                val pressed = pressedRect
                val key = pressedKey
                if (pressed != null && key != null) {
                    if (repeated) {
                        // If the key already auto-repeated, don't fire a second press.
                    } else {
                        onKeyPress(key)
                    }
                }
                stopRepeat()
                pressedRect = null
                pressedKey = null
                invalidate()
                return true
            }

            MotionEvent.ACTION_CANCEL -> {
                stopRepeat()
                pressedRect = null
                pressedKey = null
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun startRepeatIfNeeded(key: Key, rect: RectF) {
        val type = key.type
        if (type != KeyType.BACKSPACE && type != KeyType.DELETE_WORD) return
        stopRepeat()
        repeated = false
        repeatKey = key
        repeatBounds = rect
        repeatRunnable = repeatTask
        repeatHandler.postDelayed(repeatTask, REPEAT_DELAY_MILLIS)
    }

    private fun postRepeat() {
        cancelTask()
        repeatRunnable = repeatTask
        repeatHandler.postDelayed(repeatTask, REPEAT_INTERVAL_MILLIS)
    }

    private fun cancelTask() {
        repeatHandler.removeCallbacks(repeatTask)
    }

    private fun stopRepeat() {
        cancelTask()
        repeatKey = null
        repeatBounds = null
        repeated = false
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopRepeat()
    }

    private fun findKeyRect(x: Float, y: Float): RectF? {
        keyRects.forEach { row ->
            row.forEach { (_, rect) ->
                if (rect.contains(x, y)) return rect
            }
        }
        return null
    }

    private fun findKey(x: Float, y: Float): Key? {
        keyRects.forEach { row ->
            row.forEach { (key, rect) ->
                if (rect.contains(x, y)) return key
            }
        }
        return null
    }

    private val KeyWidth.weight: Float
        get() = when (this) {
            KeyWidth.NORMAL -> 1f
            KeyWidth.WIDE -> 1.5f
            KeyWidth.EXTRA_WIDE -> 2f
            KeyWidth.SPACE -> 4f
        }

    private companion object {
        const val REPEAT_DELAY_MILLIS = 350L
        const val REPEAT_INTERVAL_MILLIS = 45L
    }
}