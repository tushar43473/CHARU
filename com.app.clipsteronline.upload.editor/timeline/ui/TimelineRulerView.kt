package com.app.clipsteronline.upload.editor.timeline.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.floor

/**
 * View for rendering timeline ruler with markers.
 */
class TimelineRulerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val rulerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var zoom = 1f
    private var scrollX = 0f
    private var duration = 60000L // 1 minute default

    private val rulerHeight = 40f

    init {
        setupPaints()
    }

    /**
     * Set zoom level.
     */
    fun setZoom(zoom: Float) {
        this.zoom = zoom
        invalidate()
    }

    /**
     * Set scroll position.
     */
    fun setScroll(scrollX: Float) {
        this.scrollX = scrollX
        invalidate()
    }

    /**
     * Set duration.
     */
    fun setDuration(durationMs: Long) {
        this.duration = durationMs
        invalidate()
    }

    /**
     * Draw ruler.
     */
    fun draw(canvas: Canvas, zoom: Float, scrollX: Float) {
        this.zoom = zoom
        this.scrollX = scrollX

        // Draw background
        rulerPaint.color = Color.parseColor("#0D0D0D")
        canvas.drawRect(0f, 0f, width.toFloat(), rulerHeight, rulerPaint)

        // Draw ticks
        drawTicks(canvas)
    }

    /**
     * Draw ticks.
     */
    private fun drawTicks(canvas: Canvas) {
        val intervalMs = getTickInterval()
        val startTime = ((scrollX / (zoom * 100)) * 1000).toLong()
        val adjustedStart = (startTime / intervalMs) * intervalMs

        var timeMs = adjustedStart
        while (timeMs < startTime + (width / (zoom * 100) * 1000).toLong() + intervalMs) {
            val x = timeToX(timeMs)

            if (x >= 0 && x <= width) {
                val isMajor = timeMs % (intervalMs * 5) == 0L

                tickPaint.color = if (isMajor) Color.parseColor("#666666") else Color.parseColor("#444444")
                tickPaint.strokeWidth = if (isMajor) 2f else 1f

                val tickHeight = if (isMajor) 16f else 10f
                canvas.drawLine(x, rulerHeight - tickHeight, x, rulerHeight, tickPaint)

                // Draw time label for major ticks
                if (isMajor) {
                    val label = formatTime(timeMs)
                    textPaint.color = Color.parseColor("#888888")
                    canvas.drawText(label, x + 4, rulerHeight - 20, textPaint)
                }
            }

            timeMs += intervalMs
        }
    }

    /**
     * Get tick interval based on zoom.
     */
    private fun getTickInterval(): Long {
        return when {
            zoom > 4f -> 1000L // 1 second
            zoom > 2f -> 2000L // 2 seconds
            zoom > 1f -> 5000L // 5 seconds
            else -> 10000L // 10 seconds
        }
    }

    /**
     * Convert time to X.
     */
    private fun timeToX(timeMs: Long): Float {
        return (timeMs * zoom * 100 / 1000).toFloat() - scrollX
    }

    /**
     * Format time for display.
     */
    private fun formatTime(timeMs: Long): String {
        val totalSeconds = timeMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60

        return if (minutes > 0) {
            String.format("%d:%02d", minutes, seconds)
        } else {
            String.format("0:%02d", seconds)
        }
    }

    private fun setupPaints() {
        rulerPaint.style = Paint.Style.FILL
        tickPaint.style = Paint.Style.STROKE
        textPaint.textSize = 20f
    }
}