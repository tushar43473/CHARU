package upload.editor.core.model

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
        require(id.isNotBlank()) { "id must not be blank" }
        require(order >= 0) { "order must be non-negative" }
        require(volume in 0f..4f) { "volume must be within 0..4" }
        require(clips.all { it.startTimeMs >= 0 && it.endTimeMs >= it.startTimeMs }) { "clip timing is invalid" }
    }

    val startMs: Long get() = clips.minOfOrNull { it.startTimeMs } ?: 0L
    val endMs: Long get() = clips.maxOfOrNull { it.endTimeMs } ?: 0L
    val durationMs: Long get() = (endMs - startMs).coerceAtLeast(0L)

    enum class TrackType {
        VIDEO,
        AUDIO,
        IMAGE,
        TEXT,
        EFFECT,
        STICKER,
    }

    sealed interface TimelineClip {
        val id: String
        val startTimeMs: Long
        val endTimeMs: Long
        val zIndex: Int

        val durationMs: Long
            get() = (endTimeMs - startTimeMs).coerceAtLeast(0L)
    }
}
