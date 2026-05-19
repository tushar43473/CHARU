package com.app.clipsteronline.upload.editor.timeline.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.app.clipsteronline.upload.editor.core.model.Clip
import com.app.clipsteronline.upload.editor.timeline.engine.TrackType

/**
 * View for rendering timeline tracks.
 */
class TimelineTrackView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val clipPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var trackIndex = 0
    private var trackType = TrackType.VIDEO
    private var trackHeight = 80f

    private var clips: List<Clip> = emptyList()
    private var selectedClipIds: Set<String> = emptySet()
    private var zoom = 1f
    private var scrollX = 0f

    init {
        setupPaints()
    }

    /**
     * Configure track.
     */
    fun configure(index: Int, type: TrackType, height: Float) {
        this.trackIndex = index
        this.trackType = type
        this.trackHeight = height
        invalidate()
    }

    /**
     * Set clips.
     */
    fun setClips(clips: List<Clip>) {
        this.clips = clips
        invalidate()
    }

    /**
     * Set selection.
     */
    fun setSelection(selectedIds: Set<String>) {
        this.selectedClipIds = selectedIds
        invalidate()
    }

    /**
     * Set transform.
     */
    fun setTransform(zoom: Float, scrollX: Float) {
        this.zoom = zoom
        this.scrollX = scrollX
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        drawTrackBackground(canvas)
        drawClips(canvas)
    }

    /**
     * Draw track background.
     */
    private fun drawTrackBackground(canvas: Canvas) {
        val bgColor = if (trackIndex % 2 == 0) Color.parseColor("#141414") else Color.parseColor("#1A1A1A")
        trackPaint.color = bgColor
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), trackPaint)
    }

    /**
     * Draw clips.
     */
    private fun drawClips(canvas: Canvas) {
        clips.forEach { clip ->
            val isSelected = clip.id in selectedClipIds
            drawClip(canvas, clip, isSelected)
        }
    }

    /**
     * Draw single clip.
     */
    private fun drawClip(canvas: Canvas, clip: Clip, isSelected: Boolean) {
        val startX = timeToX(clip.timelineStartMs) - scrollX
        val endX = timeToX(clip.timelineEndMs) - scrollX
        val clipWidth = endX - startX

        if (endX < 0 || startX > width) return

        // Clip background
        val clipColor = when (trackType) {
            TrackType.VIDEO -> Color.parseColor("#3D5AFE")
            TrackType.AUDIO -> Color.parseColor("#4CAF50")
            TrackType.TEXT -> Color.parseColor("#FF9800")
            TrackType.STICKER -> Color.parseColor("#E91E63")
            TrackType.EFFECT -> Color.parseColor("#9C27B0")
        }
        clipPaint.color = clipColor

        val rect = RectF(
            startX + 4f,
            4f,
            endX - 4f,
            height - 4f
        )
        canvas.drawRoundRect(rect, 8f, 8f, clipPaint)

        // Selection border
        if (isSelected) {
            borderPaint.color = Color.parseColor("#FF6B35")
            borderPaint.style = Paint.Style.STROKE
            borderPaint.strokeWidth = 3f
            canvas.drawRoundRect(rect, 8f, 8f, borderPaint)
        }
    }

    /**
     * Convert time to X position.
     */
    private fun timeToX(timeMs: Long): Float {
        return timeMs * zoom * 100 / 1000f
    }

    private fun setupPaints() {
        trackPaint.style = Paint.Style.FILL
        clipPaint.style = Paint.Style.FILL
        borderPaint.style = Paint.Style.STROKE
    }
}