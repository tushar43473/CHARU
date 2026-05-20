package com.app.clipsteronline.upload.editor.sticker

import com.app.clipsteronline.upload.editor.core.model.StickerClip

class StickerRenderer(
    private val animatedSticker: AnimatedSticker = AnimatedSticker(),
    private val gifRenderer: GIFStickerRenderer = GIFStickerRenderer(),
) {
    fun configure() = Unit

    fun render(clip: StickerClip, timeMs: Long, viewportWidth: Int, viewportHeight: Int): RenderedSticker {
        val animState = animatedSticker.evaluate(clip, timeMs)
        val gifFrame = if (clip.stickerAsset.endsWith(".gif", ignoreCase = true)) gifRenderer.frameAt(clip.stickerAsset, timeMs) else null

        return RenderedSticker(
            clipId = clip.clipId,
            textureId = gifFrame?.textureId ?: 0,
            xPx = clip.transform.x * viewportWidth,
            yPx = clip.transform.y * viewportHeight,
            scale = animState.scale * clip.transform.scaleX,
            rotationDegrees = animState.rotationDegrees,
            opacity = (animState.opacity * clip.opacity).coerceIn(0f, 1f),
            flippedH = clip.isFlippedHorizontally,
            flippedV = clip.isFlippedVertically,
            blendMode = clip.blendMode,
        )
    }

    data class RenderedSticker(
        val clipId: String,
        val textureId: Int,
        val xPx: Float,
        val yPx: Float,
        val scale: Float,
        val rotationDegrees: Float,
        val opacity: Float,
        val flippedH: Boolean,
        val flippedV: Boolean,
        val blendMode: StickerClip.BlendMode,
    )
}
