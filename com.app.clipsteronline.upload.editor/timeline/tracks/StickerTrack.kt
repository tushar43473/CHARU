package com.app.clipsteronline.upload.editor.timeline.tracks

import com.app.clipsteronline.upload.editor.core.model.Clip
import com.app.clipsteronline.upload.editor.core.model.StickerClip
import java.util.UUID

/**
 * Sticker track for managing sticker/graphical overlays.
 * Handles animated stickers, transforms, and z-index.
 */
class StickerTrack(
    override val id: String = UUID.randomUUID().toString(),
    override var name: String = "Stickers",
    override var index: Int = 0,
    override var isVisible: Boolean = true,
    override var isLocked: Boolean = false,
    override var isMuted: Boolean = false,
    override var volume: Float = 1f
) : BaseTrack {

    private val clips = mutableListOf<StickerClip>()

    override fun getClips(): List<Clip> = clips.toList()

    /**
     * Add sticker clip.
     */
    fun addClip(clip: StickerClip) {
        clips.add(clip)
        sortClips()
    }

    /**
     * Remove clip by ID.
     */
    fun removeClip(clipId: String) {
        clips.removeAll { it.id == clipId }
    }

    /**
     * Get clip by ID.
     */
    fun getClip(clipId: String): StickerClip? {
        return clips.find { it.id == clipId }
    }

    /**
     * Get clip at index.
     */
    fun getClipAt(index: Int): StickerClip? {
        return clips.getOrNull(index)
    }

    /**
     * Update clip.
     */
    fun updateClip(clipId: String, update: (StickerClip) -> StickerClip) {
        val index = clips.indexOfFirst { it.id == clipId }
        if (index >= 0) {
            clips[index] = update(clips[index])
        }
    }

    /**
     * Get clip at time.
     */
    fun getClipAtTime(timeMs: Long): StickerClip? {
        return clips.find { it.containsTime(timeMs) }
    }

    /**
     * Get clips in range.
     */
    fun getClipsInRange(startMs: Long, endMs: Long): List<StickerClip> {
        return clips.filter { it.timelineStartMs < endMs && it.timelineEndMs > startMs }
    }

    /**
     * Get active stickers at time.
     */
    fun getActiveStickers(timeMs: Long): List<StickerClip> {
        return clips.filter { it.containsTime(timeMs) }
    }

    /**
     * Get transform at time.
     */
    fun getTransformAt(clipId: String, timeMs: Long): StickerTransform? {
        return clips.find { it.id == clipId }?.calcTransformAt(timeMs)
    }

    /**
     * Get track duration.
     */
    override fun getDuration(): Long {
        return clips.maxOfOrNull { it.timelineEndMs } ?: 0L
    }

    /**
     * Get clip count.
     */
    override fun getClipCount(): Int = clips.size

    /**
     * Bring sticker to front.
     */
    fun bringToFront(clipId: String) {
        val clip = getClip(clipId) ?: return
        clips.remove(clip)
        clips.add(clip)
    }

    /**
     * Send sticker to back.
     */
    fun sendToBack(clipId: String) {
        val clip = getClip(clipId) ?: return
        clips.remove(clip)
        clips.add(0, clip)
    }

    /**
     * Sort clips by start time.
     */
    private fun sortClips() {
        clips.sortBy { it.timelineStartMs }
    }

    /**
     * Clear all clips.
     */
    fun clear() {
        clips.clear()
    }
}

/**
 * Sticker transform data.
 */
data class StickerTransform(
    val x: Float = 0f,
    val y: Float = 0f,
    val scale: Float = 1f,
    val rotation: Float = 0f,
    val opacity: Float = 1f
)