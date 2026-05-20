package com.app.clipsteronline.upload.editor.timeline.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.max

class TimelineRulerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    private val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#66FFFFFF") }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 11f * context.resources.displayMetrics.scaledDensity
    }

    var scrollPx: Float = 0f
        set(value) { field = value; invalidate() }
    var pxPerSecond: Float = 160f
        set(value) { field = max(16f, value); invalidate() }
    var frameRate: Int = 30
        set(value) { field = value.coerceAtLeast(1); invalidate() }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width <= 0 || height <= 0) return

        val majorStepPx = chooseStepPx()
        val startX = -((scrollPx % majorStepPx + majorStepPx) % majorStepPx)
        var x = startX
        while (x <= width) {
            val ms = ((scrollPx + x) / pxPerSecond * 1000f).toLong().coerceAtLeast(0L)
            canvas.drawLine(x, height * 0.35f, x, height.toFloat(), tickPaint)
            canvas.drawText(formatTimecode(ms), x + 6f, height * 0.3f, textPaint)
            x += majorStepPx
        }
    }

    private fun chooseStepPx(): Float {
        return when {
            pxPerSecond > 1000f -> pxPerSecond / 10f
            pxPerSecond > 420f -> pxPerSecond / 5f
            else -> pxPerSecond
        }.coerceAtLeast(48f)
    }

    private fun formatTimecode(ms: Long): String {
        val totalSeconds = ms / 1000
        val frames = (((ms % 1000) / 1000f) * frameRate).toInt().coerceAtLeast(0)
        val s = totalSeconds % 60
        val m = (totalSeconds / 60) % 60
        val h = totalSeconds / 3600
        return "%02d:%02d:%02d:%02d".format(h, m, s, frames)
    }
}
