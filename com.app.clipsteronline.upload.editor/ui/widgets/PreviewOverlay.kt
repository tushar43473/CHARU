package com.app.clipsteronline.upload.editor.ui.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.view.View

/**
 * Preview overlay with guides and borders.
 * Safe area, alignment, transform handles.
 */
class PreviewOverlay(context: Context) : View(context) {

    private val guidePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val handlePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var overlayX = 0.5f
    private var overlayY = 0.5f
    private var overlayWidth = 0.5f
    private var overlayHeight = 0.5f

    private var isSelected = false
    private var showSafeArea = true
    private var showAlignmentGuides = true

    private var dragCallback: ((Float, Float) -> Unit)? = null
    private var resizeCallback: ((Float, Float) -> Unit)? = null

    /**
     * Set overlay transform.
     */
    fun setTransform(x: Float, y: Float, w: Float, h: Float) {
        overlayX = x.coerceIn(0f, 1f)
        overlayY = y.coerceIn(0f, 1f)
        overlayWidth = w.coerceIn(0.1f, 1f)
        overlayHeight = h.coerceIn(0.1f, 1f)
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
     * Show safe area guides.
     */
    fun setShowSafeArea(show: Boolean) {
        showSafeArea = show
        invalidate()
    }

    /**
     * Show alignment guides.
     */
    fun setShowAlignmentGuides(show: Boolean) {
        showAlignmentGuides = show
        invalidate()
    }

    /**
     * Set drag callback.
     */
    fun setOnDragCallback(callback: (Float, Float) -> Unit) {
        dragCallback = callback
    }

    /**
     * Set resize callback.
     */
    fun setOnResizeCallback(callback: (Float, Float) -> Unit) {
        resizeCallback = callback
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()

        // Safe area guides (16:9 safe areas within preview)
        if (showSafeArea) {
            drawSafeAreaGuides(canvas, w, h)
        }

        // Alignment guides (center lines)
        if (showAlignmentGuides) {
            drawAlignmentGuides(canvas, w, h)
        }

        // Overlay border
        drawOverlayBorder(canvas, w, h)

        // Resize handles if selected
        if (isSelected) {
            drawResizeHandles(canvas, w, h)
        }
    }

    private fun drawSafeAreaGuides(canvas: Canvas, w: Float, h: Float) {
        guidePaint.color = 0x40FF6B35.toInt()
        guidePaint.style = Paint.Style.STROKE
        guidePaint.strokeWidth = 1f

        // Rule of thirds
        canvas.drawLine(w / 3, 0f, w / 3, h, guidePaint)
        canvas.drawLine(w * 2 / 3, 0f, w * 2 / 3, h, guidePaint)
        canvas.drawLine(0f, h / 3, w, h / 3, guidePaint)
        canvas.drawLine(0f, h * 2 / 3, w, h * 2 / 3, guidePaint)
    }

    private fun drawAlignmentGuides(canvas: Canvas, w: Float, h: Float) {
        guidePaint.color = 0x20FFFFFF.toInt()
        guidePaint.style = Paint.Style.STROKE
        guidePaint.strokeWidth = 1f

        // Center lines
        canvas.drawLine(w / 2, 0f, w / 2, h, guidePaint)
        canvas.drawLine(0f, h / 2, w, h / 2, guidePaint)
    }

    private fun drawOverlayBorder(canvas: Canvas, w: Float, h: Float) {
        val ox = overlayX * w
        val oy = overlayY * h
        val ow = overlayWidth * w
        val oh = overlayHeight * h

        val rect = android.graphics.RectF(
            (ow / 2).coerceAtLeast(ox - ow / 2),
            (oh / 2).coerceAtLeast(oy - oh / 2),
            (w - ow / 2).coerceAtMost(ox + ow / 2),
            (h - oh / 2).coerceAtMost(oy + oh / 2)
        )

        borderPaint.style = Paint.Style.STROKE
        borderPaint.strokeWidth = if (isSelected) 3f else 2f
        borderPaint.color = if (isSelected) 0xFFFF6B35.toInt() else 0xFFFFFFFF.toInt()

        canvas.drawRect(rect, borderPaint)
    }

    private fun drawResizeHandles(canvas: Canvas, w: Float, h: Float) {
        handlePaint.style = Paint.Style.FILL
        handlePaint.color = 0xFFFF6B35.toInt()

        val handleSize = 12f

        val ox = overlayX * w
        val oy = overlayY * h
        val ow = overlayWidth * w
        val oh = overlayHeight * h

        // Corner handles
        val corners = listOf(
            Pair(ox - ow / 2, oy - oh / 2),
            Pair(ox + ow / 2, oy - oh / 2),
            Pair(ox - ow / 2, oy + oh / 2),
            Pair(ox + ow / 2, oy + oh / 2)
        )

        for ((cx, cy) in corners) {
            canvas.drawCircle(cx, cy, handleSize / 2, handlePaint)
        }

        // Edge handles (if large enough)
        if (ow > 100 && oh > 50) {
            val midCorners = listOf(
                Pair(ox, oy - oh / 2),
                Pair(ox, oy + oh / 2),
                Pair(ox - ow / 2, oy),
                Pair(ox + ow / 2, oy)
            )

            handlePaint.color = 0xFFFF6B35.toInt()
            val smallSize = handleSize * 0.6f

            for ((cx, cy) in midCorners) {
                canvas.drawRect(cx - smallSize / 2, cy - smallSize / 2, cx + smallSize / 2, cy + smallSize / 2, handlePaint)
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredHeight = 300

        val width = resolveSize(desiredWidth, widthMeasureSpec)
        val height = resolveSize(desiredHeight, heightMeasureSpec)

        setMeasuredDimension(width, height)
    }

    private val desiredWidth = 300
}