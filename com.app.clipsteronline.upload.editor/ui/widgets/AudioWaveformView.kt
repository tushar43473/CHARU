package com.app.clipsteronline.upload.editor.ui.widgets

class AudioWaveformView {
    private var peaks: List<Float> = emptyList()
    private var beatMarkersMs: List<Long> = emptyList()
    private var viewportStartMs: Long = 0L
    private var viewportEndMs: Long = 0L

    fun configure() = Unit

    fun submitWaveform(peaks: List<Float>, beatMarkersMs: List<Long>) {
        this.peaks = peaks
        this.beatMarkersMs = beatMarkersMs
    }

    fun updateViewport(startMs: Long, endMs: Long) {
        viewportStartMs = startMs.coerceAtLeast(0L)
        viewportEndMs = endMs.coerceAtLeast(viewportStartMs)
    }

    fun visiblePeaks(targetPoints: Int): List<Float> {
        if (targetPoints <= 0 || peaks.isEmpty()) return emptyList()
        val rangeMs = (viewportEndMs - viewportStartMs).coerceAtLeast(1L)
        val startIndex = ((viewportStartMs.toDouble() / rangeMs) * peaks.size).toInt().coerceIn(0, peaks.lastIndex)
        val endIndex = ((viewportEndMs.toDouble() / rangeMs) * peaks.size).toInt().coerceIn(startIndex + 1, peaks.size)
        val slice = peaks.subList(startIndex, endIndex)
        if (slice.size <= targetPoints) return slice
        val bucket = slice.size.toFloat() / targetPoints
        return List(targetPoints) { idx ->
            val from = (idx * bucket).toInt().coerceAtMost(slice.lastIndex)
            val to = (((idx + 1) * bucket).toInt().coerceAtMost(slice.size)).coerceAtLeast(from + 1)
            slice.subList(from, to).maxOrNull() ?: 0f
        }
    }

    fun visibleBeatMarkers(): List<Long> = beatMarkersMs.filter { it in viewportStartMs..viewportEndMs }
}
