package com.app.clipsteronline.upload.editor.ui.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.view.View
import android.animation.ValueAnimator
import android.os.Handler
import android.os.Looper

/**
 * Animated export button.
 * Loading state, progress, disabled handling.
 */
class ExportButton(context: Context) : View(context) {

    private val buttonPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val buttonRect = RectF()

    private var state = State.IDLE
    private var progress = 0f
    private var animProgress = 0f
    private var cornerRadius = 24f

    private var clickCallback: (() -> Unit)? = null

    private val accentColor = 0xFFFF6B35.toInt()
    private val darkBackground = 0xFF1A1A1A.toInt()
    private val disabledColor = 0xFF333333.toInt()

    /**
     * Set click listener.
     */
    fun setOnClickListener(callback: () -> Unit) {
        clickCallback = callback
    }

    /**
     * Set state.
     */
    fun setState(newState: State) {
        state = newState
        invalidate()
    }

    /**
     * Set progress.
     */
    fun setProgress(newProgress: Float) {
        progress = newProgress.coerceIn(0f, 1f)
        state = if (progress > 0f && progress < 1f) State.EXPORTING else state
        animateProgress()
    }

    /**
     * Animate progress.
     */
    private fun animateProgress() {
        ValueAnimator.ofFloat(animProgress, progress).apply {
            duration = 200
            addUpdateListener { animation ->
                animProgress = animation.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()

        // Background
        buttonRect.set(0f, 0f, w, h)

        when (state) {
            State.IDLE -> {
                buttonPaint.color = accentColor
                buttonPaint.style = Paint.Style.FILL
                canvas.drawRoundRect(buttonRect, cornerRadius, cornerRadius, buttonPaint)

                // Export icon
                drawExportIcon(canvas, w / 2, h / 2)
            }
            State.EXPORTING -> {
                buttonPaint.color = darkBackground
                buttonPaint.style = Paint.Style.FILL
                canvas.drawRoundRect(buttonRect, cornerRadius, cornerRadius, buttonPaint)

                // Progress arc
                progressPaint.color = accentColor
                progressPaint.style = Paint.Style.STROKE
                progressPaint.strokeWidth = 4f

                val sweepAngle = animProgress * 360f
                canvas.drawArc(buttonRect, -90f, sweepAngle, false, progressPaint)

                // Export icon
                drawExportIcon(canvas, w / 2, h / 2)
            }
            State.COMPLETED -> {
                buttonPaint.color = 0xFF4CAF50.toInt()
                buttonPaint.style = Paint.Style.FILL
                canvas.drawRoundRect(buttonRect, cornerRadius, cornerRadius, buttonPaint)

                // Checkmark
                drawCheckmark(canvas, w / 2, h / 2)
            }
            State.DISABLED -> {
                buttonPaint.color = disabledColor
                buttonPaint.style = Paint.Style.FILL
                canvas.drawRoundRect(buttonRect, cornerRadius, cornerRadius, buttonPaint)

                // Export icon
                drawExportIcon(canvas, w / 2, h / 2)
            }
        }
    }

    private fun drawExportIcon(canvas: Canvas, cx: Float, cy: Float) {
        iconPaint.color = 0xFFFFFFFF.toInt()
        iconPaint.style = Paint.Style.STROKE
        iconPaint.strokeWidth = 3f
        iconPaint.strokeCap = Paint.StrokeCap.ROUND

        val arrowSize = 10f
        val boxSize = 16f

        // Arrow polygon
        canvas.save()
        canvas.translate(cx - boxSize / 2, cy - boxSize / 2)

        canvas.drawLine(boxSize * 0.2f, boxSize * 0.5f, boxSize * 0.45f, boxSize * 0.5f, iconPaint)
        canvas.drawLine(boxSize * 0.35f, boxSize * 0.3f, boxSize * 0.5f, boxSize * 0.5f, iconPaint)
        canvas.drawLine(boxSize * 0.35f, boxSize * 0.7f, boxSize * 0.5f, boxSize * 0.5f, iconPaint)

        // Box
        canvas.drawRect(boxSize * 0.55f, boxSize * 0.3f, boxSize * 0.8f, boxSize * 0.7f, iconPaint)

        canvas.restore()
    }

    private fun drawCheckmark(canvas: Canvas, cx: Float, cy: Float) {
        iconPaint.color = 0xFFFFFFFF.toInt()
        iconPaint.style = Paint.Style.STROKE
        iconPaint.strokeWidth = 4f

        val size = 12f

        canvas.drawLine(cx - size * 0.4f, cy, cx - size * 0.1f, cy + size * 0.3f, iconPaint)
        canvas.drawLine(cx - size * 0.1f, cy + size * 0.3f, cx + size * 0.4f, cy - size * 0.3f, iconPaint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = 120
        val desiredHeight = 48

        val width = resolveSize(desiredWidth, widthMeasureSpec)
        val height = resolveSize(desiredHeight, heightMeasureSpec)

        setMeasuredDimension(width, height)
    }

    /**
     * Button states.
     */
    enum class State {
        IDLE,
        EXPORTING,
        COMPLETED,
        DISABLED
    }
}