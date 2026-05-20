package com.app.clipsteronline.upload.editor.ui.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class PreviewOverlay @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {
    private val guidePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#66FFFFFF")
        strokeWidth = context.resources.displayMetrics.density
    }

    var showRuleOfThirds: Boolean = true
        set(value) { field = value; invalidate() }

    var showSafeArea: Boolean = false
        set(value) { field = value; invalidate() }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width <= 0 || height <= 0) return
        if (showRuleOfThirds) drawThirds(canvas)
        if (showSafeArea) drawSafeArea(canvas)
    }

    private fun drawThirds(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        canvas.drawLine(w / 3f, 0f, w / 3f, h, guidePaint)
        canvas.drawLine(2f * w / 3f, 0f, 2f * w / 3f, h, guidePaint)
        canvas.drawLine(0f, h / 3f, w, h / 3f, guidePaint)
        canvas.drawLine(0f, 2f * h / 3f, w, 2f * h / 3f, guidePaint)
    }

    private fun drawSafeArea(canvas: Canvas) {
        val marginW = width * 0.08f
        val marginH = height * 0.08f
        canvas.drawRect(marginW, marginH, width - marginW, height - marginH, guidePaint)
    }
}
