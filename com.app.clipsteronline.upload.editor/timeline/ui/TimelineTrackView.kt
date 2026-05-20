package com.app.clipsteronline.upload.editor.timeline.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.math.max

class TimelineTrackView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#1AFFFFFF") }
    private val activeTrackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#33FFFFFF") }
    private val separatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#26FFFFFF") }
    private val clipPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#4FC3F7") }
    private val clipRect = RectF()

    var tracks: List<TrackVisual> = emptyList()
        set(value) { field = value; invalidate() }

    var activeTrackId: String? = null
        set(value) { field = value; invalidate() }

    var scrollYPx: Float = 0f
        set(value) { field = value; invalidate() }

    var scrollXPx: Float = 0f
        set(value) { field = value; invalidate() }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width <= 0 || height <= 0) return

        var y = -scrollYPx
        tracks.forEach { track ->
            val trackHeight = max(36f, track.heightPx)
            if (y + trackHeight >= 0f && y <= height) {
                canvas.drawRect(0f, y, width.toFloat(), y + trackHeight, if (track.id == activeTrackId) activeTrackPaint else trackPaint)
                track.clips.forEach { clip ->
                    clipRect.set(clip.startPx - scrollXPx, y + 6f, clip.endPx - scrollXPx, y + trackHeight - 6f)
                    if (clipRect.right >= 0f && clipRect.left <= width) {
                        canvas.drawRoundRect(clipRect, 8f, 8f, clipPaint)
                    }
                }
                canvas.drawLine(0f, y + trackHeight, width.toFloat(), y + trackHeight, separatorPaint)
            }
            y += trackHeight
        }
    }
}

data class TrackVisual(
    val id: String,
    val heightPx: Float,
    val clips: List<ClipVisual>,
)

data class ClipVisual(
    val startPx: Float,
    val endPx: Float,
)
