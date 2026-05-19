package com.app.clipsteronline.upload.editor.ui.bottomsheet

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.view.View
import android.widget.LinearLayout

/**
 * Bottom speed menu.
 * Speed slider, curve, reverse playback.
 */
class SpeedMenu(context: Context) : LinearLayout(context) {

    private var speedCallback: ((SpeedAction) -> Unit)? = null
    private var currentSpeed: Float = 1f

    private val presets = listOf(
        SpeedPreset("0.25x", 0.25f),
        SpeedPreset("0.5x", 0.5f),
        SpeedPreset("0.75x", 0.75f),
        SpeedPreset("1x", 1f),
        SpeedPreset("1.5x", 1.5f),
        SpeedPreset("2x", 2f),
        SpeedPreset("3x", 3f),
        SpeedPreset("4x", 4f)
    )

    /**
     * Set speed callback.
     */
    fun setOnSpeedSelectedListener(callback: (SpeedAction) -> Unit) {
        speedCallback = callback
    }

    /**
     * Set current speed.
     */
    fun setSpeed(speed: Float) {
        currentSpeed = speed.coerceIn(0.1f, 10f)
        invalidate()
    }

    /**
     * Speed action data.
     */
    data class SpeedAction(
        val type: String,
        val speed: Float = 1f
    )

    /**
     * Speed preset data.
     */
    data class SpeedPreset(val name: String, val speed: Float)
}

/**
 * Speed curve view (bezier curve editor).
 */
class SpeedCurveView(context: Context) : View(context) {

    private val curvePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val pointPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val curvePath = Path()
    private var points = listOf(0f to 0f, 1f to 1f)

    fun setPoints(newPoints: List<Pair<Float, Float>>) {
        points = newPoints
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        val padding = 32f

        // Grid
        gridPaint.color = 0xFF333333.toInt()
        gridPaint.style = Paint.Style.STROKE
        gridPaint.strokeWidth = 1f

        // Horizontal lines
        for (i in 0..4) {
            val y = padding + (h - padding * 2) * i / 4
            canvas.drawLine(padding, y, w - padding, y, gridPaint)
        }

        // Vertical lines
        for (i in 0..4) {
            val x = padding + (w - padding * 2) * i / 4
            canvas.drawLine(x, padding, x, h - padding, gridPaint)
        }

        // Curve
        curvePaint.color = 0xFFFF6B35.toInt()
        curvePaint.style = Paint.Style.STROKE
        curvePaint.strokeWidth = 4f

        curvePath.reset()
        curvePath.moveTo(padding, h - padding)

        if (points.size >= 2) {
            for ((i, pt) in points.withIndex()) {
                val x = padding + (w - padding * 2) * pt.first
                val y = h - padding - (h - padding * 2) * pt.second
                curvePath.lineTo(x, y)
            }
        }

        canvas.drawPath(curvePath, curvePaint)

        // Points
        pointPaint.color = 0xFFFFFFFF.toInt()
        pointPaint.style = Paint.Style.FILL

        for ((i, pt) in points.withIndex()) {
            val x = padding + (w - padding * 2) * pt.first
            val y = h - padding - (h - padding * 2) * pt.second
            canvas.drawCircle(x, y, 8f, pointPaint)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = resolveSize(desiredWidth, widthMeasureSpec)
        val height = resolveSize(160, heightMeasureSpec)
        setMeasuredDimension(width, height)
    }

    private val desiredWidth = 280
}