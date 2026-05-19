package com.app.clipsteronline.upload.editor.timeline.tracks

import com.app.clipsteronline.upload.editor.core.model.Clip
import com.app.clipsteronline.upload.editor.core.model.EffectClip
import java.util.UUID

/**
 * Effect track for managing effect clips.
 * Handles effect stacking, blending, and priorities.
 */
class EffectTrack(
    override val id: String = UUID.randomUUID().toString(),
    override var name: String = "Effects",
    override var index: Int = 0,
    override var isVisible: Boolean = true,
    override var isLocked: Boolean = false,
    override var isMuted: Boolean = false,
    override var volume: Float = 1f
) : BaseTrack {

    private val clips = mutableListOf<EffectClip>()

    override fun getClips(): List<Clip> = clips.toList()

    /**
     * Add effect clip.
     */
    fun addClip(clip: EffectClip) {
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
    fun getClip(clipId: String): EffectClip? {
        return clips.find { it.id == clipId }
    }

    /**
     * Get clip at index.
     */
    fun getClipAt(index: Int): EffectClip? {
        return clips.getOrNull(index)
    }

    /**
     * Update clip.
     */
    fun updateClip(clipId: String, update: (EffectClip) -> EffectClip) {
        val index = clips.indexOfFirst { it.id == clipId }
        if (index >= 0) {
            clips[index] = update(clips[index])
        }
    }

    /**
     * Get clip at time.
     */
    fun getClipAtTime(timeMs: Long): EffectClip? {
        return clips.find { it.containsTime(timeMs) }
    }

    /**
     * Get clips in range.
     */
    fun getClipsInRange(startMs: Long, endMs: Long): List<EffectClip> {
        return clips.filter { it.timelineStartMs < endMs && it.timelineEndMs > startMs }
    }

    /**
     * Get effects at time.
     */
    fun getEffectsAt(timeMs: Long): List<EffectClip> {
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
     * Get effect count at time.
     */
    fun getEffectCountAt(timeMs: Long): Int {
        return clips.count { it.containsTime(timeMs) }
    }

    /**
     * Detect overlaps.
     */
    fun detectOverlaps(): List<Pair<EffectClip, EffectClip>> {
        val overlaps = mutableListOf<Pair<EffectClip, EffectClip>>()

        for (i in clips.indices) {
            for (j in i + 1 until clips.size) {
                val clipA = clips[i]
                val clipB = clips[j]

                if (clipA.timelineStartMs < clipB.timelineEndMs &&
                    clipA.timelineEndMs > clipB.timelineStartMs) {
                    overlaps.add(clipA to clipB)
                }
            }
        }

        return overlaps
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