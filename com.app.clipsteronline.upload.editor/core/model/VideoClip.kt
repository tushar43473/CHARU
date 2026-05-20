package com.app.clipsteronline.upload.editor.core.model

data class VideoClip(
    override val clipId: String,
    override val startMs: Long,
    override val endMs: Long,
    override val layer: Int,
    val sourceUri: String,
    val trimStartMs: Long,
    val trimEndMs: Long,
    val playbackSpeed: Float = 1f,
    val rotationDegrees: Float = 0f,
    val scale: Float = 1f,
    val positionX: Float = 0f,
    val positionY: Float = 0f,
    val opacity: Float = 1f,
    val isMuted: Boolean = false,
    val volume: Float = 1f,
) : TimelineTrack.TimelineClip {
    init {
        require(clipId.isNotBlank())
        require(sourceUri.isNotBlank())
        require(startMs >= 0 && endMs >= startMs)
        require(trimStartMs >= 0 && trimEndMs >= trimStartMs)
        require(playbackSpeed in 0.1f..8f)
        require(scale > 0f)
        require(opacity in 0f..1f)
        require(volume in 0f..4f)
    }

    val sourceDurationMs: Long get() = (trimEndMs - trimStartMs).coerceAtLeast(0)
    val effectiveMediaDurationMs: Long get() = (sourceDurationMs / playbackSpeed).toLong().coerceAtLeast(0)
}
