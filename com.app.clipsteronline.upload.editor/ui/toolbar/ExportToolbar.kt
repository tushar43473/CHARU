package com.app.clipsteronline.upload.editor.ui.toolbar

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.view.View

/**
 * Export toolbar.
 * Quality, resolution, FPS, progress controls.
 */
class ExportToolbar(context: Context) : View(context) {

    private val buttonPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var exportCallback: ((ExportSettings) -> Unit)? = null
    private var progress = 0f
    private var isExporting = false

    private var quality: Quality = Quality.HD_720P
    private var resolution: Resolution = Resolution.WIDE_16_9
    private var fps: Int = 30

    private val accentColor = 0xFFFF6B35.toInt()

    /**
     * Set export callback.
     */
    fun setOnExportListener(settings: (ExportSettings) -> Unit) {
        exportCallback = settings
    }

    /**
     * Set progress.
     */
    fun setProgress(value: Float) {
        progress = value.coerceIn(0f, 1f)
        if (progress > 0f && progress < 1f) {
            isExporting = true
        } else {
            isExporting = false
        }
        invalidate()
    }

    /**
     * Export settings.
     */
    data class ExportSettings(
        val quality: Quality = Quality.HD_720P,
        val resolution: Resolution = Resolution.WIDE_16_9,
        val fps: Int = 30,
        val bitrate: Int = 0
    )

    /**
     * Quality options.
     */
    enum class Quality(val label: String, val width: Int, val height: Int, val bitrate: Int) {
        HD_720P("720p", 1280, 720, 5_000_000),
        FULL_HD("1080p", 1920, 1080, 10_000_000),
        QHD("1440p", 2560, 1440, 20_000_000),
        UHD_4K("4K", 3840, 2160, 50_000_000)
    }

    /**
     * Resolution options.
     */
    enum class Resolution(val label: String, val aspectRatio: Float) {
        WIDE_16_9("16:9", 16f / 9),
        SQUARE_1_1("1:1", 1f),
        PORTRAIT_9_16("9:16", 9f / 16),
        CINEMA_2_35("2.35:1", 2.35f)
    }
}

/**
 * Export options selector.
 */
class ExportOptionsView(context: Context) : View(context) {

    private val optionPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var selectedOption: Int = 0
    private var options = listOf("720p", "1080p", "4K")
    private var optionCallback: ((Int) -> Unit)? = null

    fun setOptions(opts: List<String>) {
        options = opts
        invalidate()
    }

    fun setSelected(index: Int) {
        selectedOption = index.coerceIn(0, options.size - 1)
        invalidate()
    }

    fun setOnOptionSelectedListener(callback: (Int) -> Unit) {
        optionCallback = callback
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        val optW = w / options.size

        labelPaint.textSize = 24f

        for ((index, opt) in options.withIndex()) {
            val x = index * optW + optW / 2

            optionPaint.color = if (index == selectedOption) 0xFFFF6B35.toInt() else 0xFF555555.toInt()
            optionPaint.style = if (index == selectedOption) Paint.Style.FILL else Paint.Style.STROKE
            optionPaint.strokeWidth = 2f

            canvas.drawText(opt, x, h / 2 + 8f, labelPaint)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(width, 48)
    }
}

/**
 * Export progress ring.
 */
class ExportProgressRing(context: Context) : View(context) {

    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var progress = 0f

    fun setProgress(value: Float) {
        progress = value.coerceIn(0f, 1f)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cx = width / 2f
        val cy = height / 2f
        val radius = minOf(cx, cy) - 8f

        // Background ring
        bgPaint.color = 0xFF333333.toInt()
        bgPaint.style = Paint.Style.STROKE
        bgPaint.strokeWidth = 6f
        canvas.drawCircle(cx, cy, radius, bgPaint)

        // Progress ring
        ringPaint.color = 0xFFFF6B35.toInt()
        ringPaint.style = Paint.Style.STROKE
        ringPaint.strokeWidth = 6f
        ringPaint.strokeCap = Paint.StrokeCap.ROUND

        val sweep = progress * 360f
        canvas.drawArc(cx - radius, cy - radius, cx + radius, cy + radius, -90f, sweep, false, ringPaint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val size = resolveSize(64, widthMeasureSpec)
        setMeasuredDimension(size, size)
    }
}