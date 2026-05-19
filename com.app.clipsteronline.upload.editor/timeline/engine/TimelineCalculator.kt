package com.app.clipsteronline.upload.editor.timeline.engine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToLong

/**
 * Timeline position calculations.
 * Handles frame/time conversion and positioning.
 */
class TimelineCalculator(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
) {
    companion object {
        private const val DEFAULT_PIXELS_PER_SECOND = 100f
        private const val MIN_PIXELS_PER_SECOND = 10f
        private const val MAX_PIXELS_PER_SECOND = 500f

        private const val DEFAULT_FRAME_RATE = 30
    }

    private var pixelsPerSecond = DEFAULT_PIXELS_PER_SECOND
    private var frameRate = DEFAULT_FRAME_RATE
    private var duration = 0L

    /**
     * Set configuration.
     */
    fun configure(
        pixelsPerSecond: Float = DEFAULT_PIXELS_PER_SECOND,
        frameRate: Int = DEFAULT_FRAME_RATE
    ) {
        this.pixelsPerSecond = pixelsPerSecond.coerceIn(MIN_PIXELS_PER_SECOND, MAX_PIXELS_PER_SECOND)
        this.frameRate = frameRate
    }

    /**
     * Set duration.
     */
    fun setDuration(durationMs: Long) {
        this.duration = durationMs
    }

    /**
     * Get time in milliseconds for X position.
     */
    fun timeForX(x: Float, zoom: Float = 1f): Long {
        return (x * 1000 / (pixelsPerSecond * zoom)).toLong()
    }

    /**
     * Get X position for time in milliseconds.
     */
    fun xForTime(timeMs: Long, zoom: Float = 1f): Float {
        return timeMs * pixelsPerSecond * zoom / 1000f
    }

    /**
     * Get pixels per millisecond.
     */
    fun pixelsPerMs(zoom: Float = 1f): Float {
        return pixelsPerSecond * zoom / 1000f
    }

    /**
     * Get milliseconds per frame.
     */
    fun msPerFrame(): Long {
        return 1000L / frameRate
    }

    /**
     * Get frame for time.
     */
    fun frameForTime(timeMs: Long): Int {
        return (timeMs * frameRate / 1000).toInt()
    }

    /**
     * Get time for frame.
     */
    fun timeForFrame(frame: Int): Long {
        return frame * 1000L / frameRate
    }

    /**
     * Get frame for position.
     */
    fun frameForX(x: Float, zoom: Float = 1f): Int {
        val timeMs = timeForX(x, zoom)
        return frameForTime(timeMs)
    }

    /**
     * Get X position for frame.
     */
    fun xForFrame(frame: Int, zoom: Float = 1f): Float {
        val timeMs = timeForFrame(frame)
        return xForTime(timeMs, zoom)
    }

    /**
     * Snap time to frame.
     */
    fun snapToFrame(timeMs: Long): Long {
        val frame = frameForTime(timeMs)
        return timeForFrame(frame)
    }

    /**
     * Get visible time range.
     */
    fun getVisibleRange(
        scrollX: Float,
        viewWidth: Float,
        zoom: Float
    ): LongRange {
        val startMs = timeForX(scrollX, zoom)
        val endMs = timeForX(scrollX + viewWidth, zoom)
        return startMs..endMs
    }

    /**
     * Calculate clip offset.
     */
    fun getClipOffset(clipStartMs: Long, playheadMs: Long): Float {
        return xForTime(clipStartMs - playheadMs)
    }

    /**
     * Get track Y position.
     */
    fun getTrackY(trackIndex: Int, trackHeight: Float): Float {
        return trackIndex * trackHeight
    }

    /**
     * Get track index for Y.
     */
    fun getTrackIndexForY(y: Float, trackHeight: Float): Int {
        return (y / trackHeight).toInt()
    }

    /**
     * Clamp scroll to valid range.
     */
    fun clampScroll(scrollX: Float, viewWidth: Float, zoom: Float): Float {
        val maxScroll = max(0f, xForTime(duration, zoom) - viewWidth)
        return scrollX.coerceIn(0f, maxScroll)
    }

    /**
     * Calculate snap point.
     */
    fun calculateSnapPoint(
        positionMs: Long,
        snapPoints: List<Long>,
        thresholdMs: Long = 100L
    ): Long {
        var closest = positionMs
        var closestDistance = thresholdMs

        for (point in snapPoints) {
            val distance = kotlin.math.abs(positionMs - point)
            if (distance < closestDistance) {
                closestDistance = distance
                closest = point
            }
        }

        return closest
    }

    /**
     * Calculate clip position in track.
     */
    fun getClipPosition(
        clip: upload.editor.core.model.Clip,
        trackIndex: Int,
        trackHeight: Float,
        zoom: Float,
        scrollX: Float
    ): Rect {
        val x = xForTime(clip.timelineStartMs, zoom) - scrollX
        val width = xForTime(clip.durationMs, zoom)
        val y = getTrackY(trackIndex, trackHeight)

        return Rect(x, y + 2, x + width, y + trackHeight - 2)
    }

    /**
     * Calculate total timeline width.
     */
    fun getTotalWidth(zoom: Float = 1f): Float {
        return xForTime(duration, zoom)
    }

    /**
     * Get current zoom level.
     */
    fun getZoomForDuration(viewWidth: Float): Float {
        if (duration <= 0) return 1f
        val timelineWidth = xForTime(duration)
        return if (timelineWidth > 0) viewWidth / timelineWidth else 1f
    }

    /**
     * Round to nearest frame time.
     */
    fun roundToFrameTime(timeMs: Long): Long {
        return ((timeMs / msPerFrame()) * msPerFrame())
    }
}

/**
 * Simple Rect class.
 */
data class Rect(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {
    val width: Float get() = right - left
    val height: Float get() = bottom - top

    fun contains(x: Float, y: Float): Boolean {
        return x in left..right && y in top..bottom
    }
}