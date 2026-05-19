package com.app.clipsteronline.upload.editor.timeline.tracks

import com.app.clipsteronline.upload.editor.core.model.AudioClip
import com.app.clipsteronline.upload.editor.core.model.Clip
import java.util.UUID

/**
 * Audio track for managing audio clips.
 * Handles waveforms, volume, fade in/out, and mixing.
 */
class AudioTrack(
    override val id: String = UUID.randomUUID().toString(),
    override var name: String = "Audio",
    override var index: Int = 0,
    override var isVisible: Boolean = true,
    override var isLocked: Boolean = false,
    override var isMuted: Boolean = false,
    override var volume: Float = 1f
) : BaseTrack {

    private val clips = mutableListOf<AudioClip>()

    override fun getClips(): List<Clip> = clips.toList()

    /**
     * Add audio clip.
     */
    fun addClip(clip: AudioClip) {
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
    fun getClip(clipId: String): AudioClip? {
        return clips.find { it.id == clipId }
    }

    /**
     * Get clip at index.
     */
    fun getClipAt(index: Int): AudioClip? {
        return clips.getOrNull(index)
    }

    /**
     * Update clip.
     */
    fun updateClip(clipId: String, update: (AudioClip) -> AudioClip) {
        val index = clips.indexOfFirst { it.id == clipId }
        if (index >= 0) {
            clips[index] = update(clips[index])
        }
    }

    /**
     * Get clip at time.
     */
    fun getClipAtTime(timeMs: Long): AudioClip? {
        return clips.find { it.containsTime(timeMs) }
    }

    /**
     * Get clips in range.
     */
    fun getClipsInRange(startMs: Long, endMs: Long): List<AudioClip> {
        return clips.filter { it.timelineStartMs < endMs && it.timelineEndMs > startMs }
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
     * Get waveform data.
     */
    fun getWaveformData(): FloatArray? {
        // Combine waveforms from all clips
        return null
    }

    /**
     * Calculate volume at time.
     */
    fun getVolumeAt(timeMs: Long): Float {
        if (isMuted) return 0f

        var totalVolume = volume

        clips.find { it.containsTime(timeMs) }?.let { clip ->
            totalVolume *= clip.calculateVolumeAt(timeMs)
        }

        return totalVolume.coerceIn(0f, 1f)
    }

    /**
     * Detect overlaps.
     */
    fun detectOverlaps(): List<Pair<AudioClip, AudioClip>> {
        val overlaps = mutableListOf<Pair<AudioClip, AudioClip>>()

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