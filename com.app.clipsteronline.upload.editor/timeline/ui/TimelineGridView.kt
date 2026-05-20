package com.app.clipsteronline.upload.editor.timeline.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.max

class TimelineGridView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    private val majorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#33FFFFFF")
        strokeWidth = context.resources.displayMetrics.density
    }
    private val minorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1FFFFFFF")
        strokeWidth = 0.5f * context.resources.displayMetrics.density
    }

    var scrollPx: Float = 0f
        set(value) { field = value; invalidate() }

    var pxPerSecond: Float = 160f
        set(value) { field = max(16f, value); invalidate() }

    var showBeatMarkers: Boolean = false
        set(value) { field = value; invalidate() }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width <= 0 || height <= 0) return

        val majorStepPx = (pxPerSecond / majorDivisions()).coerceAtLeast(20f)
        val minorStepPx = (majorStepPx / 4f).coerceAtLeast(8f)
        drawVerticalGrid(canvas, minorStepPx, minorPaint)
        drawVerticalGrid(canvas, majorStepPx, majorPaint)
    }

    private fun drawVerticalGrid(canvas: Canvas, stepPx: Float, paint: Paint) {
        val start = -((scrollPx % stepPx + stepPx) % stepPx)
        var x = start
        while (x <= width) {
            canvas.drawLine(x, 0f, x, height.toFloat(), paint)
            x += stepPx
        }
    }

    private fun majorDivisions(): Float = if (showBeatMarkers) 2f else 1f
}
