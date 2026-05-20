package com.app.clipsteronline.upload.editor.core.model

data class TimelineTrack(
    val id: String,
    val type: TrackType,
    val order: Int,
    val clips: List<TimelineClip>,
    val isVisible: Boolean = true,
    val isLocked: Boolean = false,
    val isMuted: Boolean = false,
    val volume: Float = 1f,
) {
    init {
        require(id.isNotBlank()) { "track id cannot be blank" }
        require(order >= 0) { "track order must be >= 0" }
        require(volume in 0f..4f) { "track volume must be in 0..4" }
        require(clips.all { it.startMs >= 0 && it.endMs >= it.startMs }) { "invalid clip timing in track" }
    }

    val durationMs: Long get() = (clips.maxOfOrNull { it.endMs } ?: 0L) - (clips.minOfOrNull { it.startMs } ?: 0L)

    enum class TrackType { VIDEO, AUDIO, IMAGE, TEXT, EFFECT, STICKER }

    sealed interface TimelineClip {
        val clipId: String
        val startMs: Long
        val endMs: Long
        val layer: Int
        val timelineDurationMs: Long get() = (endMs - startMs).coerceAtLeast(0)
    }
}
