package com.app.clipsteronline.upload.editor.timeline.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

/**
 * View for rendering background grid and guidelines.
 */
class TimelineGridView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val trackLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var zoom = 1f
    private var scrollX = 0f
    private var trackCount = 5
    private var trackHeight = 80f
    private var rulerHeight = 40f

    private var showGrid = true
    private var showTrackLines = true

    init {
        setupPaints()
    }

    /**
     * Set configuration.
     */
    fun configure(trackCount: Int, trackHeight: Float, rulerHeight: Float) {
        this.trackCount = trackCount
        this.trackHeight = trackHeight
        this.rulerHeight = rulerHeight
        invalidate()
    }

    /**
     * Set transform.
     */
    fun setTransform(zoom: Float, scrollX: Float) {
        this.zoom = zoom
        this.scrollX = scrollX
        invalidate()
    }

    /**
     * Set grid visibility.
     */
    fun setGridVisible(visible: Boolean) {
        this.showGrid = visible
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (showGrid) {
            drawVerticalGrid(canvas)
        }

        if (showTrackLines) {
            drawTrackLines(canvas)
        }
    }

    /**
     * Draw vertical grid lines.
     */
    private fun drawVerticalGrid(canvas: Canvas) {
        val intervalMs = getGridInterval()
        val startTime = ((scrollX / (zoom * 100)) * 1000).toLong()
        val adjustedStart = (startTime / intervalMs) * intervalMs

        gridPaint.color = Color.parseColor("#1A1A1A")
        gridPaint.strokeWidth = 1f
        gridPaint.style = Paint.Style.STROKE

        var timeMs = adjustedStart
        while (timeMs < startTime + (width / (zoom * 100) * 1000).toLong() + intervalMs) {
            val x = timeToX(timeMs)
            if (x >= 0 && x <= width) {
                canvas.drawLine(x, rulerHeight, x, height.toFloat(), gridPaint)
            }
            timeMs += intervalMs
        }
    }

    /**
     * Draw horizontal track lines.
     */
    private fun drawTrackLines(canvas: Canvas) {
        trackLinePaint.color = Color.parseColor("#2A2A2A")
        trackLinePaint.strokeWidth = 2f
        trackLinePaint.style = Paint.Style.STROKE

        var y = rulerHeight + trackHeight
        repeat(trackCount) {
            if (y <= height) {
                canvas.drawLine(0f, y, width.toFloat(), y, trackLinePaint)
            }
            y += trackHeight
        }
    }

    /**
     * Get grid interval.
     */
    private fun getGridInterval(): Long {
        return when {
            zoom > 4f -> 1000L
            zoom > 2f -> 2000L
            zoom > 1f -> 5000L
            else -> 10000L
        }
    }

    /**
     * Convert time to X.
     */
    private fun timeToX(timeMs: Long): Float {
        return (timeMs * zoom * 100 / 1000).toFloat() - scrollX
    }

    private fun setupPaints() {
        gridPaint.style = Paint.Style.STROKE
        trackLinePaint.style = Paint.Style.STROKE
    }
}