package com.app.clipsteronline.upload.editor.timeline.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class ClipHandleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {
    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#E0FFFFFF") }
    private val gripPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#99000000") }

    var active: Boolean = false
        set(value) { field = value; invalidate() }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val radius = 6f * resources.displayMetrics.density
        canvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), radius, radius, bgPaint)
        val centerX = width / 2f
        val alpha = if (active) 220 else 140
        gripPaint.alpha = alpha
        canvas.drawLine(centerX - 4f, height * 0.3f, centerX - 4f, height * 0.7f, gripPaint)
        canvas.drawLine(centerX, height * 0.3f, centerX, height * 0.7f, gripPaint)
        canvas.drawLine(centerX + 4f, height * 0.3f, centerX + 4f, height * 0.7f, gripPaint)
    }
}
