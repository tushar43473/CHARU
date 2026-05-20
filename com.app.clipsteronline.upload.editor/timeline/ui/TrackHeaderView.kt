package com.app.clipsteronline.upload.editor.timeline.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.max

class TrackHeaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 12f * context.resources.displayMetrics.scaledDensity
    }
    private val mutedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#44FF5252") }
    private val lockedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#44FFD740") }
    private val rowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#14000000") }

    var headers: List<TrackHeaderState> = emptyList()
        set(value) { field = value; invalidate() }

    var scrollYPx: Float = 0f
        set(value) { field = value; invalidate() }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        var y = -scrollYPx
        headers.forEach { header ->
            val rowHeight = max(36f, header.heightPx)
            if (y + rowHeight >= 0f && y <= height) {
                canvas.drawRect(0f, y, width.toFloat(), y + rowHeight, rowPaint)
                canvas.drawText(header.label, 12f, y + rowHeight * 0.62f, textPaint)
                if (header.isMuted) canvas.drawCircle(width - 42f, y + rowHeight / 2f, 8f, mutedPaint)
                if (header.isLocked) canvas.drawCircle(width - 20f, y + rowHeight / 2f, 8f, lockedPaint)
            }
            y += rowHeight
        }
    }
}

data class TrackHeaderState(
    val label: String,
    val heightPx: Float,
    val isMuted: Boolean,
    val isLocked: Boolean,
)
