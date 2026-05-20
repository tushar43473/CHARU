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
) : TimelineTrack.TimelineClip {
    init {
        require(clipId.isNotBlank())
        require(stickerAsset.isNotBlank())
        require(startMs >= 0 && endMs >= startMs)
        require(scale > 0f)
        require(opacity in 0f..1f)
    }

    data class StickerAnimation(val preset: Preset, val loop: Boolean = true, val speed: Float = 1f) {
        init { require(speed in 0.25f..4f) }
        enum class Preset { BOUNCE, POP, WIGGLE, ROTATE, PULSE }
    }

    data class Transform(val x: Float = 0.5f, val y: Float = 0.5f, val anchorX: Float = 0.5f, val anchorY: Float = 0.5f)
}
