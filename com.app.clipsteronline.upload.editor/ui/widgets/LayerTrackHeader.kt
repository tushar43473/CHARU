package com.app.clipsteronline.upload.editor.ui.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.view.View

/**
 * Timeline layer track header.
 * Labels, mute/lock/visibility toggles.
 */
class LayerTrackHeader(context: Context) : View(context) {

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var trackName = "Video"
    private var isSelected = false
    private var isMuted = false
    private var isLocked = false
    private var isHidden = false

    private var iconClickCallback: ((IconType) -> Unit)? = null

    private val iconSize = 20f
    private val padding = 8f

    /**
     * Set track name.
     */
    fun setTrackName(name: String) {
        trackName = name
        invalidate()
    }

    /**
     * Set selected.
     */
    fun setSelected(selected: Boolean) {
        isSelected = selected
        invalidate()
    }

    /**
     * Set mute state.
     */
    fun setMuted(muted: Boolean) {
        isMuted = muted
        invalidate()
    }

    /**
     * Set locked state.
     */
    fun setLocked(locked: Boolean) {
        isLocked = locked
        invalidate()
    }

    /**
     * Set hidden state.
     */
    fun setHidden(hidden: Boolean) {
        isHidden = hidden
        invalidate()
    }

    /**
     * Set icon click callback.
     */
    fun setOnIconClickListener(callback: (IconType) -> Unit) {
        iconClickCallback = callback
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()

        // Background
        if (isSelected) {
            backgroundPaint.color = 0xFFFF6B35.toInt()
            backgroundPaint.alpha = 30
        } else {
            backgroundPaint.color = 0xFF222222.toInt()
            backgroundPaint.alpha = 255
        }

        backgroundPaint.style = Paint.Style.FILL
        canvas.drawRect(0f, 0f, w, h, backgroundPaint)

        // Track name label
        labelPaint.color = 0xFFFFFFFF.toInt()
        labelPaint.textSize = iconSize
        labelPaint.textAlign = Paint.Align.LEFT
        canvas.drawText(trackName, padding, h / 2 + iconSize / 3, labelPaint)

        // Icons - visibility (eye)
        drawVisibilityIcon(canvas, w - (padding + iconSize) * 3, h / 2)

        // Lock icon
        drawLockIcon(canvas, w - (padding + iconSize) * 2, h / 2)

        // Mute icon
        drawMuteIcon(canvas, w - (padding + iconSize), h / 2)
    }

    private fun drawVisibilityIcon(canvas: Canvas, cx: Float, cy: Float) {
        iconPaint.color = if (isHidden) 0xFF666666.toInt() else 0xFFFFFFFF.toInt()
        iconPaint.style = Paint.Style.STROKE
        iconPaint.strokeWidth = 2f

        val size = iconSize

        // Eye shape
        canvas.drawCircle(cx, cy - size * 0.2f, size * 0.25f, iconPaint)
        canvas.drawLine(cx - size * 0.3f, cy + size * 0.3f, cx + size * 0.3f, cy + size * 0.3f, iconPaint)
    }

    private fun drawLockIcon(canvas: Canvas, cx: Float, cy: Float) {
        iconPaint.color = if (isLocked) 0xFFFF6B35.toInt() else 0xFF888888.toInt()
        iconPaint.style = Paint.Style.STROKE
        iconPaint.strokeWidth = 2f

        val size = iconSize * 0.5f

        // Lock body
        canvas.drawRect(cx - size, cy - size * 0.5f, cx + size, cy + size * 0.5f, iconPaint)

        // Lock shackle
        val path = Path()
        path.moveTo(cx - size * 0.6f, cy - size * 0.5f)
        path.arcTo(cx - size * 0.6f, cy - size * 1.2f, cx + size * 0.6f, cy - size * 0.5f, -180f, 90f, false)
    }

    private fun drawMuteIcon(canvas: Canvas, cx: Float, cy: Float) {
        iconPaint.color = if (isMuted) 0xFFFF6B35.toInt() else 0xFFFF6B35.toInt()
        iconPaint.style = if (isMuted) Paint.Style.FILL else Paint.Style.STROKE
        iconPaint.strokeWidth = 2f

        // Speaker icon
        val path = Path()
        val size = iconSize * 0.5f

        path.moveTo(cx - size, cy - size * 0.5f)
        path.lineTo(cx - size, cy + size * 0.5f)
        path.lineTo(cx - size * 0.5f, cy + size * 0.5f)
        path.lineTo(cx + size * 0.3f, cy + size * 0.5f)
        path.lineTo(cx + size * 0.3f, cy - size * 0.5f)
        path.close()

        canvas.drawPath(path, iconPaint)

        if (isMuted) {
            // Cross line
            canvas.drawLine(cx + size * 0.2f, cy - size * 0.3f, cx + size * 0.8f, cy + size * 0.3f, iconPaint)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = 80
        val desiredHeight = 48

        val width = resolveSize(desiredWidth, widthMeasureSpec)
        val height = resolveSize(desiredHeight, heightMeasureSpec)

        setMeasuredDimension(width, height)
    }

    /**
     * Icon types.
     */
    enum class IconType {
        VISIBILITY,
        LOCK,
        MUTE
    }
}