package upload.editor.core.model

data class ImageClip(
    override val id: String,
    override val startTimeMs: Long,
    override val endTimeMs: Long,
    override val zIndex: Int = 0,
    val sourcePath: String,
    val durationMs: Long,
    val scale: Float = 1f,
    val rotationDegrees: Float = 0f,
    val transform: Transform = Transform(),
    val kenBurns: KenBurns? = null,
) : TimelineTrack.TimelineClip {
    init {
        require(id.isNotBlank())
        require(sourcePath.isNotBlank())
        require(startTimeMs >= 0 && endTimeMs >= startTimeMs)
        require(durationMs > 0)
        require(scale > 0f)
        require(durationMs == (endTimeMs - startTimeMs)) { "durationMs must match timeline range" }
    }

    data class Transform(
        val positionX: Float = 0f,
        val positionY: Float = 0f,
        val anchorX: Float = 0.5f,
        val anchorY: Float = 0.5f,
    ) {
        init {
            require(anchorX in 0f..1f)
            require(anchorY in 0f..1f)
        }
    }

    data class KenBurns(
        val startScale: Float,
        val endScale: Float,
        val startX: Float,
        val startY: Float,
        val endX: Float,
        val endY: Float,
        val easing: Easing = Easing.EASE_IN_OUT,
    ) {
        init {
            require(startScale > 0f && endScale > 0f)
        }

        enum class Easing { LINEAR, EASE_IN, EASE_OUT, EASE_IN_OUT }
    }
}
