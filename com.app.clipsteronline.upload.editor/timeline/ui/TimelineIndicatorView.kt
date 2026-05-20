package com.app.clipsteronline.upload.editor.timeline.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class TimelineIndicatorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    private val playheadPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF3D5AFE")
        strokeWidth = 2f * context.resources.displayMetrics.density
    }
    private val snapPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFFFC107")
        strokeWidth = context.resources.displayMetrics.density
    }

    var playheadX: Float = 0f
        set(value) { field = value; invalidate() }

    var snapX: Float? = null
        set(value) { field = value; invalidate() }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width <= 0 || height <= 0) return
        val clampedX = playheadX.coerceIn(0f, width.toFloat())
        canvas.drawLine(clampedX, 0f, clampedX, height.toFloat(), playheadPaint)
        snapX?.let { sx ->
            val clampedSnap = sx.coerceIn(0f, width.toFloat())
            canvas.drawLine(clampedSnap, 0f, clampedSnap, height.toFloat(), snapPaint)
        }
    }
}
