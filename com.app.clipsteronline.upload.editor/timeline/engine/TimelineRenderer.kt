package com.app.clipsteronline.upload.editor.timeline.engine

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.app.clipsteronline.upload.editor.core.model.Clip
import com.app.clipsteronline.upload.editor.core.model.TimelineTrack
import kotlin.math.max
import kotlin.math.min

/**
 * Renderer for timeline visible region.
 * Handles efficient rendering and viewport updates.
 */
class TimelineRenderer(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
) {
    private val _renderState = MutableStateFlow(RenderState())
    val renderState: StateFlow<RenderState> = _renderState.asStateFlow()

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val clipPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val playheadPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rulerPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var timelineWidth = 0f
    private var trackHeight = 0f
    private var visibleStartMs = 0L
    private var visibleEndMs = 0L

    init {
        setupPaints()
    }

    /**
     * Set dimensions.
     */
    fun setDimensions(timelineWidth: Float, trackHeight: Float) {
        this.timelineWidth = timelineWidth
        this.trackHeight = trackHeight
    }

    /**
     * Set visible time range.
     */
    fun setVisibleRange(startMs: Long, endMs: Long) {
        visibleStartMs = startMs
        visibleEndMs = endMs
    }

    /**
     * Render visible region.
     */
    fun render(
        canvas: Canvas,
        tracks: List<TimelineTrack>,
        zoom: Float,
        scrollX: Float
    ) {
        val startTimeMs = getTimeForX(scrollX, zoom)
        val endTimeMs = getTimeForX(scrollX + timelineWidth, zoom)

        // Render tracks
        tracks.forEachIndexed { index, track ->
            renderTrack(canvas, track, index, zoom, scrollX, startTimeMs, endTimeMs)
        }

        // Update render state
        _renderState.value = _renderState.value.copy(
            renderedTrackCount = tracks.size,
            visibleTimeStart = startTimeMs,
            visibleTimeEnd = endTimeMs
        )
    }

    /**
     * Render single track.
     */
    private fun renderTrack(
        canvas: Canvas,
        track: TimelineTrack,
        index: Int,
        zoom: Float,
        scrollX: Float,
        startTimeMs: Long,
        endTimeMs: Long
    ) {
        val y = index * trackHeight

        // Draw track background
        trackPaint.color = if (index % 2 == 0) 0xFF1A1A1A.toInt() else 0xFF222222.toInt()
        canvas.drawRect(0f, y, timelineWidth, y + trackHeight, trackPaint)

        // Render visible clips
        val visibleClips = track.clips.filter { clip ->
            clip.timelineStartMs < endTimeMs && clip.timelineEndMs > startTimeMs
        }

        visibleClips.forEach { clip ->
            renderClip(canvas, clip, index, zoom, scrollX, startTimeMs)
        }
    }

    /**
     * Render single clip.
     */
    private fun renderClip(
        canvas: Canvas,
        clip: Clip,
        trackIndex: Int,
        zoom: Float,
        scrollX: Float,
        startTimeMs: Long
    ) {
        val startX = getXForTime(clip.timelineStartMs, zoom) - scrollX
        val endX = getXForTime(clip.timelineEndMs, zoom) - scrollX

        if (endX < 0 || startX > timelineWidth) return // Outside visible area

        val y = trackIndex * trackHeight

        // Draw clip
        clipPaint.color = 0xFF3D5AFE.toInt() // Blue clip color
        canvas.drawRect(startX, y + 4f, endX, y + trackHeight - 4f, clipPaint)
    }

    /**
     * Render playhead.
     */
    fun renderPlayhead(canvas: Canvas, positionMs: Long, zoom: Float, scrollX: Float) {
        val x = getXForTime(positionMs, zoom) - scrollX

        if (x < 0 || x > timelineWidth) return

        playheadPaint.color = 0xFFFF5252.toInt() // Red
        playheadPaint.strokeWidth = 3f
        canvas.drawLine(x, 0f, x, trackHeight * 10, playheadPaint)
    }

    /**
     * Render ruler.
     */
    fun renderRuler(canvas: Canvas, zoom: Float, scrollX: Float) {
        rulerPaint.color = 0xFF666666.toInt()
        rulerPaint.textSize = 24f

        val intervalMs = getRulerIntervalMs(zoom)
        var timeMs = (visibleStartMs / intervalMs) * intervalMs

        while (timeMs < visibleEndMs) {
            val x = getXForTime(timeMs, zoom) - scrollX
            if (x > 0 && x < timelineWidth) {
                canvas.drawLine(x, 0f, x, 20f, rulerPaint)
            }
            timeMs += intervalMs
        }
    }

    /**
     * Get X position for time.
     */
    private fun getXForTime(timeMs: Long, zoom: Float): Float {
        return timeMs * zoom / 1000f
    }

    /**
     * Get time for X position.
     */
    private fun getTimeForX(x: Float, zoom: Float): Long {
        return (x * 1000 / zoom).toLong()
    }

    /**
     * Get ruler interval based on zoom.
     */
    private fun getRulerIntervalMs(zoom: Float): Long {
        return when {
            zoom > 4f -> 1000L // 1 second
            zoom > 2f -> 2000L // 2 seconds
            zoom > 1f -> 5000L // 5 seconds
            else -> 10000L // 10 seconds
        }
    }

    /**
     * Get visible clips for track.
     */
    fun getVisibleClips(track: TimelineTrack): List<Clip> {
        return track.clips.filter { clip ->
            clip.timelineStartMs < visibleEndMs && clip.timelineEndMs > visibleStartMs
        }
    }

    /**
     * Get clip at position.
     */
    fun getClipAt(
        tracks: List<TimelineTrack>,
        x: Float,
        y: Float,
        zoom: Float,
        scrollX: Float
    ): Pair<Clip, TimelineTrack>? {
        val timeMs = getTimeForX(x + scrollX, zoom)
        val trackIndex = (y / trackHeight).toInt()

        val track = tracks.getOrNull(trackIndex) ?: return null

        val clip = track.clips.find { clip ->
            timeMs >= clip.timelineStartMs && timeMs < clip.timelineEndMs
        }

        return clip?.let { it to track }
    }

    /**
     * Request redraw.
     */
    fun invalidate() {
        _renderState.value = _renderState.value.copy(invalidationId = _renderState.value.invalidationId + 1)
    }

    /**
     * Setup paints.
     */
    private fun setupPaints() {
        trackPaint.style = Paint.Style.FILL
        clipPaint.style = Paint.Style.FILL

        playheadPaint.style = Paint.Style.STROKE
        playheadPaint.strokeWidth = 3f
    }
}

/**
 * Render state.
 */
data class RenderState(
    val invalidationId: Int = 0,
    val renderedTrackCount: Int = 0,
    val visibleTimeStart: Long = 0L,
    val visibleTimeEnd: Long = 0L
) {
    companion object {
        val EMPTY = RenderState()
    }
}