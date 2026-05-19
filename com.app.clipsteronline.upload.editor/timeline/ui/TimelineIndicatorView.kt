package com.app.clipsteronline.upload.editor.timeline.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

/**
 * View for rendering timeline indicators (snap, position feedback).
 */
class TimelineIndicatorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val indicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val snapPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var indicatorType = IndicatorType.NONE
    private var indicatorX = 0f

    init {
        setupPaints()
    }

    /**
     * Set indicator.
     */
    fun setIndicator(type: IndicatorType, x: Float) {
        this.indicatorType = type
        this.indicatorX = x
        invalidate()
    }

    /**
     * Clear indicator.
     */
    fun clear() {
        indicatorType = IndicatorType.NONE
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        when (indicatorType) {
            IndicatorType.SNAP -> drawSnapIndicator(canvas)
            IndicatorType.POSITION -> drawPositionIndicator(canvas)
            IndicatorType.CENTER -> drawCenterIndicator(canvas)
            IndicatorType.NONE -> { /* Nothing to draw */ }
        }
    }

    /**
     * Draw snap indicator.
     */
    private fun drawSnapIndicator(canvas: Canvas) {
        snapPaint.color = Color.parseColor("#FF6B35")
        snapPaint.strokeWidth = 3f
        snapPaint.style = Paint.Style.STROKE
        canvas.drawLine(indicatorX, 0f, indicatorX, height.toFloat(), snapPaint)
    }

    /**
     * Draw position indicator.
     */
    private fun drawPositionIndicator(canvas: Canvas) {
        indicatorPaint.color = Color.parseColor("#4CAF50")
        indicatorPaint.strokeWidth = 2f
        indicatorPaint.style = Paint.Style.STROKE
        canvas.drawLine(indicatorX, 0f, indicatorX, height.toFloat(), indicatorPaint)
    }

    /**
     * Draw center indicator.
     */
    private fun drawCenterIndicator(canvas: Canvas) {
        indicatorPaint.color = Color.parseColor("#FFFFFF")
        indicatorPaint.alpha = 100
        indicatorPaint.strokeWidth = 1f
        indicatorPaint.style = Paint.Style.STROKE
        canvas.drawLine(width / 2f, 0f, width / 2f, height.toFloat(), indicatorPaint)
    }

    private fun setupPaints() {
        indicatorPaint.style = Paint.Style.STROKE
        snapPaint.style = Paint.Style.STROKE
    }
}

/**
 * Indicator types.
 */
enum class IndicatorType {
    NONE,
    SNAP,
    POSITION,
    CENTER
}