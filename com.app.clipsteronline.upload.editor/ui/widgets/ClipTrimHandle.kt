package com.app.clipsteronline.upload.editor.ui.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.view.View

/**
 * Left/right trim handles.
 * Gesture handling, visual feedback, snapping.
 */
class ClipTrimHandle(context: Context) : View(context) {

    private val handlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var handleSide = Side.LEFT
    private var isSelected = false
    private var isHighlighted = false
    private var dragCallback: ((Float) -> Unit)? = null
    private var positionCallback: ((Float) -> Unit)? = null

    private val handleWidth = 16f
    private val handleTriangles = 2

    /**
     * Set handle side.
     */
    fun setSide(side: Side) {
        handleSide = side
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
     * Set highlighted.
     */
    fun setHighlighted(highlighted: Boolean) {
        isHighlighted = highlighted
        invalidate()
    }

    /**
     * Set drag callback.
     */
    fun setOnDragCallback(callback: (Float) -> Unit) {
        dragCallback = callback
    }

    /**
     * Set position callback.
     */
    fun setOnPositionCallback(callback: (Float) -> Unit) {
        positionCallback = callback
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val h = height.toFloat()
        val w = width.toFloat()

        // Draw handle background
        if (handleSide == Side.LEFT) {
            drawLeftHandle(canvas, w, h)
        } else {
            drawRightHandle(canvas, w, h)
        }

        // Draw border
        drawBorder(canvas, w, h)
    }

    private fun drawLeftHandle(canvas: Canvas, w: Float, h: Float) {
        if (isSelected || isHighlighted) {
            handlePaint.color = 0xFFFF6B35.toInt()
        } else {
            handlePaint.color = 0xFF444444.toInt()
        }

        handlePaint.style = Paint.Style.FILL

        // Triangle indicators
        val triangleSize = 8f
        val spacing = h / (handleTriangles + 1)

        for (i in 1..handleTriangles) {
            val y = spacing * i
            val path = Path()

            path.moveTo(w * 0.3f, y)
            path.lineTo(w * 0.7f, y - triangleSize / 2)
            path.lineTo(w * 0.7f, y + triangleSize / 2)
            path.close()

            canvas.drawPath(path, handlePaint)
        }
    }

    private fun drawRightHandle(canvas: Canvas, w: Float, h: Float) {
        if (isSelected || isHighlighted) {
            handlePaint.color = 0xFFFF6B35.toInt()
        } else {
            handlePaint.color = 0xFF444444.toInt()
        }

        handlePaint.style = Paint.Style.FILL

        val triangleSize = 8f
        val spacing = h / (handleTriangles + 1)

        for (i in 1..handleTriangles) {
            val y = spacing * i
            val path = Path()

            path.moveTo(w * 0.7f, y)
            path.lineTo(w * 0.3f, y - triangleSize / 2)
            path.lineTo(w * 0.3f, y + triangleSize / 2)
            path.close()

            canvas.drawPath(path, handlePaint)
        }
    }

    private fun drawBorder(canvas: Canvas, w: Float, h: Float) {
        borderPaint.style = Paint.Style.STROKE
        borderPaint.strokeWidth = 2f

        if (isSelected) {
            borderPaint.color = 0xFFFF6B35.toInt()
        } else {
            borderPaint.color = 0xFF666666.toInt()
        }

        canvas.drawRect(0f, 0f, w / 2, h, borderPaint)
        canvas.drawRect(w / 2, 0f, w, h, borderPaint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = handleWidth.toInt()
        val desiredHeight = 48

        val width = resolveSize(desiredWidth, widthMeasureSpec)
        val height = resolveSize(desiredHeight, heightMeasureSpec)

        setMeasuredDimension(width, height)
    }

    /**
     * Handle sides.
     */
    enum class Side {
        LEFT,
        RIGHT
    }
}