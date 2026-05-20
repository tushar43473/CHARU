package com.app.clipsteronline.upload.editor.sticker

import com.app.clipsteronline.upload.editor.core.model.StickerClip

class StickerEngine(
    private val renderer: StickerRenderer = StickerRenderer(),
    private val gestureHandler: StickerGestureHandler = StickerGestureHandler(),
) {
    private val clips = mutableListOf<StickerClip>()

    fun configure() = Unit

    fun setClips(stickers: List<StickerClip>) {
        clips.clear()
        clips.addAll(stickers)
    }

    fun addClip(clip: StickerClip) {
        clips += clip
    }

    fun removeClip(id: String) {
        clips.removeAll { it.clipId == id }
    }

    fun renderAt(timeMs: Long, width: Int, height: Int): List<StickerRenderer.RenderedSticker> {
        return clips.filter { it.contains(timeMs) }
            .sortedBy { it.layer }
            .map { renderer.render(it, timeMs, width, height) }
    }

    fun applyDrag(id: String, deltaX: Float, deltaY: Float) {
        mutate(id) { gestureHandler.drag(it, deltaX, deltaY) }
    }

    fun applyPinchRotate(id: String, startA: StickerGestureHandler.TouchPoint, startB: StickerGestureHandler.TouchPoint, endA: StickerGestureHandler.TouchPoint, endB: StickerGestureHandler.TouchPoint) {
        mutate(id) { gestureHandler.pinchAndRotate(it, startA, startB, endA, endB) }
    }

    fun flipHorizontal(id: String) {
        mutate(id) { gestureHandler.flipHorizontal(it) }
    }

    fun flipVertical(id: String) {
        mutate(id) { gestureHandler.flipVertical(it) }
    }

    private fun mutate(id: String, transform: (StickerClip) -> StickerClip) {
        val index = clips.indexOfFirst { it.clipId == id }
        if (index < 0) return
        clips[index] = transform(clips[index])
    }
}
