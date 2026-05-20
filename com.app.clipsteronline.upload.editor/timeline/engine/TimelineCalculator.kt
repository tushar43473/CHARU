package com.app.clipsteronline.upload.editor.timeline.engine

import kotlin.math.abs
import kotlin.math.roundToLong

class TimelineCalculator(
    private val pxPerSecondAt1x: Float = 120f,
) {
    fun timeToPx(timeMs: Long, zoom: Float): Float {
        require(zoom > 0f)
        return (timeMs / 1000f) * pxPerSecondAt1x * zoom
    }

    fun pxToTime(px: Float, zoom: Float): Long {
        require(zoom > 0f)
        return ((px / (pxPerSecondAt1x * zoom)) * 1000f).roundToLong().coerceAtLeast(0L)
    }

    fun pxDeltaToTimeDeltaMs(deltaPx: Float, zoom: Float): Long = abs(pxToTime(deltaPx, zoom) - pxToTime(0f, zoom))

    fun clampPlayhead(playheadMs: Long, durationMs: Long): Long = playheadMs.coerceIn(0L, durationMs.coerceAtLeast(0L))

    fun isOverlapping(aStart: Long, aEnd: Long, bStart: Long, bEnd: Long): Boolean {
        require(aStart <= aEnd && bStart <= bEnd)
        return aStart < bEnd && bStart < aEnd
    }

    fun visibleRange(scrollPx: Float, viewportWidthPx: Int, zoom: Float): LongRange {
        val start = pxToTime(scrollPx.coerceAtLeast(0f), zoom)
        val end = pxToTime(scrollPx.coerceAtLeast(0f) + viewportWidthPx.coerceAtLeast(0), zoom)
        return start..end
    }

    fun snapToAnchors(targetMs: Long, anchors: List<Long>, thresholdMs: Long = 80L): Long {
        if (anchors.isEmpty()) return targetMs
        val nearest = anchors.minByOrNull { abs(it - targetMs) } ?: return targetMs
        return if (abs(nearest - targetMs) <= thresholdMs) nearest else targetMs
    }
}
