package com.app.clipsteronline.upload.editor.ui.toolbar

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.view.View

/**
 * Timeline toolbar.
 * Zoom, track controls, snapping, playback mode.
 */
class TimelineToolbar(context: Context) : View(context) {

    private val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var zoomLevel = 1f
    private var isSnappingEnabled = true
    private var isMagnetEnabled = true
    private var playbackMode: PlaybackMode = PlaybackMode.NORMAL

    private var zoomCallback: ((Float) -> Unit)? = null
    private var snapCallback: ((Boolean) -> Unit)? = null
    private var magnetCallback: ((Boolean) -> Unit)? = null
    private var modeCallback: ((PlaybackMode) -> Unit)? = null

    private val accentColor = 0xFFFF6B35.toInt()

    /**
     * Set zoom callback.
     */
    fun setOnZoomChangedListener(callback: (Float) -> Unit) {
        zoomCallback = callback
    }

    /**
     * Set snapping callback.
     */
    fun setOnSnappingChangedListener(callback: (Boolean) -> Unit) {
        snapCallback = callback
    }

    /**
     * Set magnet callback.
     */
    fun setOnMagnetChangedListener(callback: (Boolean) -> Unit) {
        magnetCallback = callback
    }

    /**
     * Set mode callback.
     */
    fun setOnPlaybackModeChangedListener(callback: (PlaybackMode) -> Unit) {
        modeCallback = callback
    }

    /**
     * Set zoom.
     */
    fun setZoom(level: Float) {
        zoomLevel = level.coerceIn(0.1f, 10f)
        invalidate()
    }

    /**
     * Set snapping enabled.
     */
    fun setSnappingEnabled(enabled: Boolean) {
        isSnappingEnabled = enabled
        invalidate()
    }

    /**
     * Set magnet enabled.
     */
    fun setMagnetEnabled(enabled: Boolean) {
        isMagnetEnabled = enabled
        invalidate()
    }

    /**
     * Set playback mode.
     */
    fun setPlaybackMode(mode: PlaybackMode) {
        playbackMode = mode
        invalidate()
    }

    /**
     * Toggle snapping.
     */
    fun toggleSnapping() {
        isSnappingEnabled = !isSnappingEnabled
        snapCallback?.invoke(isSnappingEnabled)
        invalidate()
    }

    /**
     * Toggle magnet.
     */
    fun toggleMagnet() {
        isMagnetEnabled = !isMagnetEnabled
        magnetCallback?.invoke(isMagnetEnabled)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        val iconSize = 20f

        // Zoom out button
        drawZoomOut(canvas, 16f, h / 2, iconSize)

        // Zoom level label
        labelPaint.color = 0xFFFFFFFF.toInt()
        labelPaint.textSize = 24f
        labelPaint.textAlign = Paint.Align.CENTER
        canvas.drawText("${zoomLevel}x", 80f, h / 2 + 8f, labelPaint)

        // Zoom in button
        drawZoomIn(canvas, 130f, h / 2, iconSize)

        // Snapping toggle
        drawSnapToggle(canvas, w - 200f, h / 2, iconSize)

        // Magnet toggle
        drawMagnetToggle(canvas, w - 160f, h / 2, iconSize)

        // Playback mode
        drawPlaybackMode(canvas, w - 48f, h / 2, iconSize)
    }

    private fun drawZoomOut(canvas: Canvas, cx: Float, cy: Float, size: Float) {
        iconPaint.color = 0xFFFFFFFF.toInt()
        iconPaint.style = Paint.Style.STROKE
        iconPaint.strokeWidth = 2f
        canvas.drawLine(cx - size / 2, cy, cx + size / 2, cy, iconPaint)
    }

    private fun drawZoomIn(canvas: Canvas, cx: Float, cy: Float, size: Float) {
        iconPaint.color = 0xFFFFFFFF.toInt()
        iconPaint.style = Paint.Style.STROKE
        iconPaint.strokeWidth = 2f
        canvas.drawLine(cx - size / 2, cy, cx + size / 2, cy, iconPaint)
        canvas.drawLine(cx, cy - size / 2, cx, cy + size / 2, iconPaint)
    }

    private fun drawSnapToggle(canvas: Canvas, cx: Float, cy: Float, size: Float) {
        iconPaint.color = if (isSnappingEnabled) accentColor else 0xFF666666.toInt()
        iconPaint.style = Paint.Style.STROKE
        iconPaint.strokeWidth = 2f

        // Snap icon (box with arrows)
        canvas.drawRect(cx - size / 2, cy - size / 2, cx + size / 2, cy + size / 2, iconPaint)
    }

    private fun drawMagnetToggle(canvas: Canvas, cx: Float, cy: Float, size: Float) {
        iconPaint.color = if (isMagnetEnabled) accentColor else 0xFF666666.toInt()
        iconPaint.style = Paint.Style.STROKE
        iconPaint.strokeWidth = 2f

        // Magnet icon (horseshoe)
        val path = Path()
        path.moveTo(cx + size / 2, cy - size / 2)
        path.quadTo(cx, cy - size, cx - size / 2, cy - size / 2)
        path.quadTo(cx - size / 2, cy, cx, cy + size / 3)
        canvas.drawPath(path, iconPaint)
    }

    private fun drawPlaybackMode(canvas: Canvas, cx: Float, cy: Float, size: Float) {
        iconPaint.color = accentColor
        iconPaint.style = Paint.Style.STROKE
        iconPaint.strokeWidth = 2f

        // Play mode indicator
        labelPaint.color = 0xFFFFFFFF.toInt()
        labelPaint.textSize = 20f

        val label = when (playbackMode) {
            PlaybackMode.NORMAL -> "▶"
            PlaybackMode.LOOP -> "🔁"
            PlaybackMode.SHUFFLE -> "⤮"
        }

        canvas.drawText(label, cx - 10f, cy + 8f, labelPaint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(width, resolveSize(48, heightMeasureSpec))
    }

    /**
     * Playback modes.
     */
    enum class PlaybackMode {
        NORMAL,
        LOOP,
        SHUFFLE
    }
}