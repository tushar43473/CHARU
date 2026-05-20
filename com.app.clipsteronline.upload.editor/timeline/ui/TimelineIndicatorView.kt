package com.app.clipsteronline.upload.editor.timeline.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.max

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

    var selectionRange: Pair<Float, Float>? = null
        set(value) { field = value; invalidate() }

    private val selectionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#223D5AFE") }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width <= 0 || height <= 0) return
        selectionRange?.let { (start, end) ->
            val l = start.coerceIn(0f, width.toFloat())
            val r = end.coerceIn(0f, width.toFloat())
            canvas.drawRect(minOf(l, r), 0f, max(l, r), height.toFloat(), selectionPaint)
        }
        val clampedX = playheadX.coerceIn(0f, width.toFloat())
        canvas.drawLine(clampedX, 0f, clampedX, height.toFloat(), playheadPaint)
        snapX?.let { sx ->
            val clampedSnap = sx.coerceIn(0f, width.toFloat())
            canvas.drawLine(clampedSnap, 0f, clampedSnap, height.toFloat(), snapPaint)
        }
    }
}
