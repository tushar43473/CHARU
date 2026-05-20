package upload.editor.core.model

data class StickerClip(
    override val id: String,
    override val startTimeMs: Long,
    override val endTimeMs: Long,
    override val zIndex: Int = 0,
    val stickerAssetPath: String,
    val animation: StickerAnimation? = null,
    val transform: Transform = Transform(),
    val scale: Float = 1f,
    val rotationDegrees: Float = 0f,
    val opacity: Float = 1f,
) : TimelineTrack.TimelineClip {
    init {
        require(id.isNotBlank())
        require(stickerAssetPath.isNotBlank())
        require(startTimeMs >= 0 && endTimeMs >= startTimeMs)
        require(scale > 0f)
        require(opacity in 0f..1f)
    }

    data class StickerAnimation(
        val preset: Preset,
        val loop: Boolean = true,
        val speed: Float = 1f,
    ) {
        init {
            require(speed in 0.25f..4f)
        }

        enum class Preset { BOUNCE, POP, WIGGLE, ROTATE, PULSE }
    }

    data class Transform(
        val x: Float = 0.5f,
        val y: Float = 0.5f,
        val anchorX: Float = 0.5f,
        val anchorY: Float = 0.5f,
    ) {
        init {
            require(x in 0f..1f)
            require(y in 0f..1f)
            require(anchorX in 0f..1f)
            require(anchorY in 0f..1f)
        }
    }
}
