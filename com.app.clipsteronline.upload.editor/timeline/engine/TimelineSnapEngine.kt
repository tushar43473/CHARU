package com.app.clipsteronline.upload.editor.timeline.engine

class TimelineSnapEngine(private val snapThresholdMs: Long = 80L) {
    fun snap(targetMs: Long, anchorsMs: List<Long>): Long {
        if (anchorsMs.isEmpty()) return targetMs
        val nearest = anchorsMs.minByOrNull { kotlin.math.abs(it - targetMs) } ?: return targetMs
        return if (kotlin.math.abs(nearest - targetMs) <= snapThresholdMs) nearest else targetMs
    }
}
