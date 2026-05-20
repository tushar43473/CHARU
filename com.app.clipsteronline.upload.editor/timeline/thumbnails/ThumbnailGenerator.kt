package com.app.clipsteronline.upload.editor.timeline.thumbnails

class ThumbnailGenerator {
    fun sampleFrameTimesUs(startUs: Long, endUs: Long, zoom: Float, viewportWidthPx: Int): List<Long> {
        val safeStart = startUs.coerceAtLeast(0L)
        val safeEnd = endUs.coerceAtLeast(safeStart)
        val duration = (safeEnd - safeStart).coerceAtLeast(1L)
        val density = (viewportWidthPx.coerceAtLeast(1) / 120f * zoom.coerceIn(0.25f, 8f)).toInt().coerceAtLeast(1)
        val step = (duration / density).coerceAtLeast(33_333L)
        val result = ArrayList<Long>()
        var t = safeStart
        while (t <= safeEnd) {
            result.add(t)
            t += step
        }
        if (result.lastOrNull() != safeEnd) result.add(safeEnd)
        return result
    }
}
