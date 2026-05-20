package com.app.clipsteronline.upload.editor.timeline.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class TimelineClipView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {
    private val videoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#4FC3F7") }
    private val audioPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#81C784") }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#BA68C8") }
    private val effectPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#FFB74D") }
    private val selectedStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f * context.resources.displayMetrics.density
        color = Color.WHITE
    }
    private val rect = RectF()

    var clip: ClipUiModel? = null
        set(value) { field = value; invalidate() }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val c = clip ?: return
        rect.set(0f, 0f, width.toFloat(), height.toFloat())
        val paint = when (c.type) {
            ClipType.VIDEO -> videoPaint
            ClipType.AUDIO -> audioPaint
            ClipType.TEXT -> textPaint
            ClipType.EFFECT -> effectPaint
        }
        canvas.drawRoundRect(rect, 10f, 10f, paint)
        if (c.isSelected) canvas.drawRoundRect(rect, 10f, 10f, selectedStroke)
    }
}

enum class ClipType { VIDEO, AUDIO, TEXT, EFFECT }

data class ClipUiModel(
    val id: String,
    val type: ClipType,
    val isSelected: Boolean,
)
