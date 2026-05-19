package com.app.clipsteronline.upload.editor.timeline.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View

/**
 * View for rendering playhead with glow effect.
 */
class PlayheadView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val playheadPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val handlePath = Path()

    private var playheadX = 0f

    private var isPlaying = false

    init {
        setupPaints()
        setWillNotDraw(false)
    }

    /**
     * Set playhead position.
     */
    fun setPosition(x: Float) {
        this.playheadX = x
        invalidate()
    }

    /**
     * Set playing state.
     */
    fun setPlaying(playing: Boolean) {
        this.isPlaying = playing
        invalidate()
    }

    /**
     * Draw playhead.
     */
    fun draw(canvas: Canvas, x: Float, height: Float) {
        this.playheadX = x
        drawPlayhead(canvas, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawPlayhead(canvas, height.toFloat())
    }

    /**
     * Draw playhead.
     */
    private fun drawPlayhead(canvas: Canvas, height: Float) {
        // Draw glow
        glowPaint.color = Color.parseColor("#FF5252")
        glowPaint.alpha = 50
        glowPaint.strokeWidth = 8f
        glowPaint.style = Paint.Style.STROKE

        canvas.drawLine(playheadX, 0f, playheadX, height, glowPaint)

        // Draw playhead line
        playheadPaint.color = Color.parseColor("#FF5252")
        playheadPaint.strokeWidth = 3f
        playheadPaint.style = Paint.Style.STROKE
        canvas.drawLine(playheadX, 0f, playheadX, height, playheadPaint)

        // Draw handle at top
        handlePath.reset()
        handlePath.moveTo(playheadX - 10, 0f)
        handlePath.lineTo(playheadX + 10, 0f)
        handlePath.lineTo(playheadX, 15f)
        handlePath.close()

        playheadPaint.style = Paint.Style.FILL
        canvas.drawPath(handlePath, playheadPaint)
    }

    private fun setupPaints() {
        playheadPaint.color = Color.parseColor("#FF5252")
        playheadPaint.style = Paint.Style.FILL
        glowPaint.style = Paint.Style.STROKE
    }
}