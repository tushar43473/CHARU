package com.app.clipsteronline.upload.editor.timeline.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class PlayheadView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF2962FF")
        strokeWidth = 2f * resources.displayMetrics.density
    }
    private val capPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#FF2962FF") }

    var playheadX: Float = 0f
        set(value) { field = value.coerceAtLeast(0f); invalidate() }

    var onSeekRequested: ((Float) -> Unit)? = null

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val x = playheadX.coerceIn(0f, width.toFloat())
        canvas.drawLine(x, 0f, x, height.toFloat(), linePaint)
        canvas.drawCircle(x, 10f * resources.displayMetrics.density, 6f * resources.displayMetrics.density, capPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                playheadX = event.x
                onSeekRequested?.invoke(playheadX)
                return true
            }
        }
        return super.onTouchEvent(event)
    }
}
