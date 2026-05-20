package com.app.clipsteronline.upload.editor.timeline.engine

data class TimelineState(
    val durationMs: Long = 0L,
    val playheadMs: Long = 0L,
    val scrollPx: Float = 0f,
    val zoom: Float = 1f,
    val viewportWidthPx: Int = 0,
    val selectedTrackId: String? = null,
    val activeClipId: String? = null,
) {
    init {
        require(durationMs >= 0) { "durationMs must be >= 0" }
        require(playheadMs >= 0) { "playheadMs must be >= 0" }
        require(zoom in MIN_ZOOM..MAX_ZOOM) { "zoom out of range" }
        require(viewportWidthPx >= 0) { "viewportWidthPx must be >= 0" }
    }

    fun normalizedPlayhead(): Float =
        if (durationMs == 0L) 0f else (playheadMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)

    companion object {
        const val MIN_ZOOM = 0.25f
        const val MAX_ZOOM = 12f
    }
}
