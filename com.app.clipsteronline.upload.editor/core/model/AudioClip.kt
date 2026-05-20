package com.app.clipsteronline.upload.editor.core.model

data class AudioClip(
    override val clipId: String,
    override val startMs: Long,
    override val endMs: Long,
    override val layer: Int,
    val sourceUri: String,
    val trimStartMs: Long,
    val trimEndMs: Long,
    val fadeInMs: Long = 0L,
    val fadeOutMs: Long = 0L,
    val waveformSamples: List<Float> = emptyList(),
    val volume: Float = 1f,
    val speed: Float = 1f,
    val isLooping: Boolean = false,
) : TimelineTrack.TimelineClip {
    init {
        require(clipId.isNotBlank())
        require(sourceUri.isNotBlank())
        require(startMs >= 0 && endMs >= startMs)
        require(trimStartMs >= 0 && trimEndMs >= trimStartMs)
        require(fadeInMs >= 0 && fadeOutMs >= 0)
        require(volume in 0f..4f)
        require(speed in 0.25f..4f)
        require(waveformSamples.none { it.isNaN() || it.isInfinite() })
        require(fadeInMs + fadeOutMs <= (trimEndMs - trimStartMs).coerceAtLeast(0L))
    }
}
