package com.app.clipsteronline.upload.editor.sticker

class GIFStickerRenderer {
    private val frameCache = mutableMapOf<String, List<GifFrame>>()

    fun configure() = Unit

    fun registerFrames(asset: String, frames: List<GifFrame>) {
        if (asset.isBlank() || frames.isEmpty()) return
        frameCache[asset] = frames
    }

    fun frameAt(asset: String, timeMs: Long): GifFrame? {
        val frames = frameCache[asset] ?: return null
        val total = frames.sumOf { it.durationMs.coerceAtLeast(1L) }
        if (total <= 0L) return frames.firstOrNull()
        var cursor = (timeMs % total).coerceAtLeast(0L)
        frames.forEach { frame ->
            val span = frame.durationMs.coerceAtLeast(1L)
            if (cursor < span) return frame
            cursor -= span
        }
        return frames.lastOrNull()
    }

    data class GifFrame(
        val textureId: Int,
        val durationMs: Long,
    )
}
