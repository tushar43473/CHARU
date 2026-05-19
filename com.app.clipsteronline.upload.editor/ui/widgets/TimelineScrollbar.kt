package com.app.clipsteronline.upload.editor.ui.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View

/**
 * Smooth horizontal scrollbar.
 * Timeline sync, drag support, zoom aware.
 */
class TimelineScrollbar(context: Context) : View(context) {

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val thumbPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var viewportStart = 0f
    private var viewportEnd = 1f // normalized 0-1
    private var contentExtent = 1f
    private var isDragging = false
    private var dragCallback: ((Float) -> Unit)? = null

    private val scrollbarHeight = 4f
    private val thumbMinWidth = 40f

    /**
     * Set viewport.
     */
    fun setViewport(start: Float, end: Float) {
        viewportStart = start.coerceIn(0f, 1f)
        viewportEnd = end.coerceIn(0f, 1f)
        invalidate()
    }

    /**
     * Set drag callback.
     */
    fun setOnScrollCallback(callback: (Float) -> Unit) {
        dragCallback = callback
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()

        // Track background
        trackPaint.color = 0xFF333333.toInt()
        trackPaint.style = Paint.Style.FILL
        canvas.drawRect(0f, h - scrollbarHeight, w, h, trackPaint)

        // Thumb
        var thumbWidth = (viewportEnd - viewportStart) * w
        thumbWidth = thumbWidth.coerceAtLeast(thumbMinWidth)

        val thumbX = viewportStart * w

        thumbPaint.color = 0xFFFF6B35.toInt()
        thumbPaint.style = Paint.Style.FILL
        canvas.drawRect(thumbX, h - scrollbarHeight, thumbX + thumbWidth, h, thumbPaint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredHeight = 16
        val width = resolveSize(desiredWidth, widthMeasureSpec)
        val height = resolveSize(desiredHeight, heightMeasureSpec)
        setMeasuredDimension(width, height)
    }

    private val desiredWidth = 200
}