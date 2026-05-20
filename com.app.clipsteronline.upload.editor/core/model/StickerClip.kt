package com.app.clipsteronline.upload.editor.core.model

data class StickerClip(
    override val clipId: String,
    override val startMs: Long,
    override val endMs: Long,
    override val layer: Int,
    val stickerAsset: String,
    val animation: StickerAnimation? = null,
    val transform: Transform = Transform(),
    val scale: Float = 1f,
    val rotationDegrees: Float = 0f,
    val opacity: Float = 1f,
    val blendMode: BlendMode = BlendMode.NORMAL,
    val isFlippedHorizontally: Boolean = false,
    val isFlippedVertically: Boolean = false,
) : TimelineTrack.TimelineClip {
    init {
        require(clipId.isNotBlank())
        require(stickerAsset.isNotBlank())
        require(startMs >= 0 && endMs >= startMs)
        require(scale > 0f)
        require(opacity in 0f..1f)
        require(transform.x in 0f..1f && transform.y in 0f..1f)
    }

    fun contains(timeMs: Long): Boolean = timeMs in startMs..endMs

    data class StickerAnimation(val preset: Preset, val loop: Boolean = true, val speed: Float = 1f) {
        init { require(speed in 0.25f..4f) }
        enum class Preset { BOUNCE, POP, WIGGLE, ROTATE, PULSE }
    }

    enum class BlendMode { NORMAL, ADD, SCREEN, MULTIPLY }

    data class Transform(
        val x: Float = 0.5f,
        val y: Float = 0.5f,
        val anchorX: Float = 0.5f,
        val anchorY: Float = 0.5f,
        val scaleX: Float = 1f,
        val scaleY: Float = 1f,
    )
}
