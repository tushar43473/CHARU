package com.app.clipsteronline.upload.editor.timeline.clips

import com.app.clipsteronline.upload.editor.core.model.StickerClip

data class OverlayClip(
    val id: String,
    val startMs: Long,
    val endMs: Long,
    val zIndex: Int,
    val opacity: Float,
    val stickerClip: StickerClip,
) {
    init {
        require(id.isNotBlank())
        require(startMs >= 0 && endMs >= startMs)
        require(opacity in 0f..1f)
    }

    fun visibleAt(timeMs: Long): Boolean = timeMs in startMs..endMs
}
