package upload.editor.core.model

data class VideoClip(
    override val id: String,
    override val startTimeMs: Long,
    override val endTimeMs: Long,
    override val zIndex: Int = 0,
    val sourcePath: String,
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
    val sourceDurationMs: Long = (trimEndMs - trimStartMs).coerceAtLeast(0)
    val effectiveDurationMs: Long = (sourceDurationMs / playbackSpeed).toLong().coerceAtLeast(0)

    init {
        require(id.isNotBlank())
        require(sourcePath.isNotBlank())
        require(startTimeMs >= 0 && endTimeMs >= startTimeMs)
        require(trimStartMs >= 0 && trimEndMs >= trimStartMs)
        require(playbackSpeed in 0.1f..8f)
        require(scale > 0f)
        require(opacity in 0f..1f)
        require(volume in 0f..4f)
    }
}
