package com.app.clipsteronline.upload.editor.timeline.tracks

import com.app.clipsteronline.upload.editor.core.model.Clip
import com.app.clipsteronline.upload.editor.core.model.TextClip
import java.util.UUID

/**
 * Text track for managing text/subtitle clips.
 * Handles text ordering, animations, and transitions.
 */
class TextTrack(
    override val id: String = UUID.randomUUID().toString(),
    override var name: String = "Text",
    override var index: Int = 0,
    override var isVisible: Boolean = true,
    override var isLocked: Boolean = false,
    override var isMuted: Boolean = false,
    override var volume: Float = 1f
) : BaseTrack {

    private val clips = mutableListOf<TextClip>()

    override fun getClips(): List<Clip> = clips.toList()

    /**
     * Add text clip.
     */
    fun addClip(clip: TextClip) {
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
    fun getClip(clipId: String): TextClip? {
        return clips.find { it.id == clipId }
    }

    /**
     * Get clip at index.
     */
    fun getClipAt(index: Int): TextClip? {
        return clips.getOrNull(index)
    }

    /**
     * Update clip.
     */
    fun updateClip(clipId: String, update: (TextClip) -> TextClip) {
        val index = clips.indexOfFirst { it.id == clipId }
        if (index >= 0) {
            clips[index] = update(clips[index])
        }
    }

    /**
     * Get clip at time.
     */
    fun getClipAtTime(timeMs: Long): TextClip? {
        return clips.find { it.containsTime(timeMs) }
    }

    /**
     * Get clips in range.
     */
    fun getClipsInRange(startMs: Long, endMs: Long): List<TextClip> {
        return clips.filter { it.timelineStartMs < endMs && it.timelineEndMs > startMs }
    }

    /**
     * Get active clips at time.
     */
    fun getActiveClips(timeMs: Long): List<TextClip> {
        return clips.filter { it.containsTime(timeMs) }
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
     * Get visible clips (based on animation).
     */
    fun getVisibleClips(timeMs: Long): List<TextClip> {
        return clips.filter { clip ->
            clip.containsTime(timeMs) && clip.calcTransformAt(timeMs).opacity > 0
        }
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