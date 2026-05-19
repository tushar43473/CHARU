package com.app.clipsteronline.upload.editor.ui.effects

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.MotionEvent
import android.view.View

/**
 * Effect intensity slider.
 * Glow design, smooth control, realtime updates.
 */
class EffectIntensitySlider(context: Context) : View(context) {

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val thumbPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var intensity = 1f
    private var callback: ((Float) -> Unit)? = null
    private var isDragging = false

    private val accentColor = 0xFFFF6B35.toInt()
    private var startX = 0f

    /**
     * Set intensity.
     */
    fun setIntensity(value: Float) {
        intensity = value.coerceIn(0f, 1f)
        invalidate()
    }

    /**
     * Set callback.
     */
    fun setOnIntensityChangedListener(callback: (Float) -> Unit) {
        this.callback = callback
    }

    /**
     * Animate to value.
     */
    fun animateTo(target: Float, duration: Long = 200) {
        ValueAnimator.ofFloat(intensity, target).apply {
            this.duration = duration
            addUpdateListener { animator ->
                intensity = animator.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        val trackH = 8f
        val trackY = h / 2 - trackH / 2

        // Track background
        trackPaint.color = 0xFF333333.toInt()
        trackPaint.style = Paint.Style.FILL
        canvas.drawRoundRect(0f, trackY, w, trackY + trackH, trackH / 2, trackH / 2, trackPaint)

        // Fill
        fillPaint.color = accentColor
        fillPaint.style = Paint.Style.FILL
        canvas.drawRoundRect(0f, trackY, w * intensity, trackY + trackH, trackH / 2, trackH / 2, fillPaint)

        // Glow at fill end
        if (intensity > 0.1f) {
            glowPaint.color = accentColor
            glowPaint.alpha = 60
            canvas.drawCircle(w * intensity, h / 2, 16f, glowPaint)
        }

        // Thumb
        thumbPaint.color = 0xFFFFFFFF.toInt()
        thumbPaint.style = Paint.Style.FILL
        canvas.drawCircle(w * intensity, h / 2, 14f, thumbPaint)

        thumbPaint.color = accentColor
        thumbPaint.style = Paint.Style.FILL
        canvas.drawCircle(w * intensity, h / 2, 8f, thumbPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.x
                isDragging = true
            }
            MotionEvent.ACTION_MOVE -> {
                if (isDragging) {
                    intensity = (event.x / width).coerceIn(0f, 1f)
                    callback?.invoke(intensity)
                    invalidate()
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isDragging = false
                callback?.invoke(intensity)
            }
        }
        return true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(width, resolveSize(48, heightMeasureSpec))
    }
}

/**
 * Double-ended intensity slider (for filter adjustments).
 */
class DualIntensitySlider(context: Context) : View(context) {

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val indicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var value = 0f // -1 to 1 range centered at 0
    private var callback: ((Float) -> Unit)? = null

    fun setValue(v: Float) {
        value = v.coerceIn(-1f, 1f)
        invalidate()
    }

    fun setOnValueChangedListener(callback: (Float) -> Unit) {
        this.callback = callback
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        val center = w / 2
        val trackH = 6f

        // Track
        trackPaint.color = 0xFF333333.toInt()
        trackPaint.style = Paint.Style.FILL
        canvas.drawRoundRect(0f, h / 2 - trackH / 2, w, h / 2 + trackH / 2, trackH / 2, trackH / 2, trackPaint)

        // Center line
        trackPaint.color = 0xFF555555.toInt()
        trackPaint.strokeWidth = 2f
        canvas.drawLine(center, 4f, center, h - 4f, trackPaint)

        // Value indicator
        indicatorPaint.color = 0xFFFF6B35.toInt()
        val x = if (value >= 0) center + (w / 2 - 8) * value else center + (w / 2 - 8) * value
        canvas.drawCircle(x, h / 2, 10f, indicatorPaint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(width, resolveSize(36, heightMeasureSpec))
    }
}