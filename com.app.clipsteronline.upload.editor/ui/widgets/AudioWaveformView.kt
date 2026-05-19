package com.app.clipsteronline.upload.editor.ui.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.view.View

/**
 * Audio waveform rendering.
 * Zoom aware bar rendering, progress overlay.
 */
class AudioWaveformView(context: Context) : View(context) {

    private val waveformPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val peakPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var waveformData: FloatArray = floatArrayOf()
    private var playbackProgress = 0f
    private var barWidth = 3f
    private var barSpacing = 1f

    /**
     * Set waveform data.
     */
    fun setWaveform(data: FloatArray) {
        waveformData = data
        invalidate()
    }

    /**
     * Set playback progress.
     */
    fun setPlaybackProgress(progress: Float) {
        playbackProgress = progress.coerceIn(0f, 1f)
        invalidate()
    }

    /**
     * Set bar dimensions.
     */
    fun setBarDimensions(width: Float, spacing: Float) {
        barWidth = width.coerceIn(1f, 10f)
        barSpacing = spacing.coerceIn(0f, 10f)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()

        if (waveformData.isEmpty()) return

        val barCount = (w / (barWidth + barSpacing)).toInt()
        val samplesPerBar = waveformData.size / barCount

        val centerY = h / 2
        val maxBarHeight = h * 0.8f

        for (i in 0 until barCount) {
            val barX = i * (barWidth + barSpacing)

            // Average samples for this bar
            val startIdx = i * samplesPerBar
            val endIdx = minOf(startIdx + samplesPerBar, waveformData.size)

            var sum = 0f
            var max = 0f

            for (idx in startIdx until endIdx) {
                val amplitude = kotlin.math.abs(waveformData[idx])
                sum += amplitude
                max = maxOf(max, amplitude)
            }

            val avgAmplitude = if (endIdx > startIdx) sum / (endIdx - startIdx) else 0f
            val barHeight = avgAmplitude * maxBarHeight
            val top = centerY - barHeight / 2
            val bottom = centerY + barHeight / 2

            // Determine if this bar is before/after playback
            val barProgress = i.toFloat() / barCount

            if (barProgress <= playbackProgress) {
                progressPaint.style = Paint.Style.FILL
                progressPaint.color = 0xFFFF6B35.toInt()
                canvas.drawRect(barX, top, barX + barWidth, bottom, progressPaint)
            } else {
                waveformPaint.style = Paint.Style.FILL
                waveformPaint.color = 0xFF555555.toInt()

                // Gradient from darker in back to lighter in front
                val alpha = (0.4f + 0.3f * (1f - barProgress) * 2f).toInt()
                waveformPaint.alpha = alpha.coerceIn(50, 180)

                canvas.drawRect(barX, top, barX + barWidth, bottom, waveformPaint)
            }
        }

        // Playhead line
        val playheadX = playbackProgress * w

        peakPaint.color = 0xFFFF6B35.toInt()
        peakPaint.style = Paint.Style.STROKE
        peakPaint.strokeWidth = 2f

        canvas.drawLine(playheadX, 0f, playheadX, h, peakPaint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredHeight = 64
        val width = resolveSize(desiredWidth, widthMeasureSpec)
        val height = resolveSize(desiredHeight, heightMeasureSpec)
        setMeasuredDimension(width, height)
    }

    private val desiredWidth = 200
}