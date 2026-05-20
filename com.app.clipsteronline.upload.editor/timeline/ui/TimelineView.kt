package com.app.clipsteronline.upload.editor.timeline.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class TimelineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#121212")
    }

    var gridView: TimelineGridView? = null
    var trackView: TimelineTrackView? = null
    var indicatorView: TimelineIndicatorView? = null
    var rulerView: TimelineRulerView? = null

    init {
        setLayerType(LAYER_TYPE_HARDWARE, null)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)
        drawLayer(canvas, gridView)
        drawLayer(canvas, trackView)
        drawLayer(canvas, rulerView)
        drawLayer(canvas, indicatorView)
    }

    private fun drawLayer(canvas: Canvas, child: View?) {
        child ?: return
        val checkpoint = canvas.save()
        canvas.translate(child.left.toFloat(), child.top.toFloat())
        child.draw(canvas)
        canvas.restoreToCount(checkpoint)
    }

    fun requestTimelineRedraw() {
        postInvalidateOnAnimation()
    }
}
