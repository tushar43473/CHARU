package com.app.clipsteronline.upload.editor.timeline.engine

data class TimelineState(
    val durationMs: Long,
    val zoom: Float = 1f,
    val scrollMs: Long = 0L,
    val playheadMs: Long = 0L,
) {
    init {
        require(durationMs >= 0) { "duration must be >=0" }
        require(zoom in 0.25f..10f) { "zoom out of range" }
    }
}
