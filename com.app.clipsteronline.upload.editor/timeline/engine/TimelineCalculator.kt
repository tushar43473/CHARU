package com.app.clipsteronline.upload.editor.timeline.engine

class TimelineCalculator(private val pxPerSecondAt1x: Float = 120f) {
    fun msToPx(ms: Long, zoom: Float): Float = (ms / 1000f) * pxPerSecondAt1x * zoom

    fun pxToMs(px: Float, zoom: Float): Long {
        val pxPerSecond = pxPerSecondAt1x * zoom
        return ((px / pxPerSecond) * 1000f).toLong()
    }

    fun clampPosition(positionMs: Long, durationMs: Long): Long = positionMs.coerceIn(0L, durationMs)
}
