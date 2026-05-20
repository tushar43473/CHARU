package upload.editor.core.model

data class AudioClip(
    override val id: String,
    override val startTimeMs: Long,
    override val endTimeMs: Long,
    override val zIndex: Int = 0,
    val sourcePath: String,
    val trimStartMs: Long,
    val trimEndMs: Long,
    val fadeInMs: Long = 0L,
    val fadeOutMs: Long = 0L,
    val waveformSamples: List<Float> = emptyList(),
    val volume: Float = 1f,
    val speed: Float = 1f,
    val isLooping: Boolean = false,
) : TimelineTrack.TimelineClip {
    val sourceDurationMs: Long = (trimEndMs - trimStartMs).coerceAtLeast(0)

    init {
        require(id.isNotBlank())
        require(sourcePath.isNotBlank())
        require(startTimeMs >= 0 && endTimeMs >= startTimeMs)
        require(trimStartMs >= 0 && trimEndMs >= trimStartMs)
        require(fadeInMs >= 0 && fadeOutMs >= 0)
        require(fadeInMs + fadeOutMs <= sourceDurationMs) { "fade sum exceeds clip duration" }
        require(volume in 0f..4f)
        require(speed in 0.25f..4f)
        require(waveformSamples.none { it.isNaN() || it.isInfinite() }) { "waveform samples must be finite" }
    }
}
