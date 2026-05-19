package com.app.clipsteronline.upload.editor.timeline.tracks

import com.app.clipsteronline.upload.editor.core.model.Clip
import com.app.clipsteronline.upload.editor.core.model.VideoClip
import java.util.UUID

/**
 * Video track for managing video clips.
 * Handles clip ordering, overlap detection, and playback visibility.
 */
class VideoTrack(
    override val id: String = UUID.randomUUID().toString(),
    override var name: String = "Video",
    override var index: Int = 0,
    override var isVisible: Boolean = true,
    override var isLocked: Boolean = false,
    override var isMuted: Boolean = false,
    override var volume: Float = 1f
) : BaseTrack {

    private val clips = mutableListOf<VideoClip>()

    override fun getClips(): List<Clip> = clips.toList()

    /**
     * Add video clip.
     */
    fun addClip(clip: VideoClip) {
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
    fun getClip(clipId: String): VideoClip? {
        return clips.find { it.id == clipId }
    }

    /**
     * Get clip at index.
     */
    fun getClipAt(index: Int): VideoClip? {
        return clips.getOrNull(index)
    }

    /**
     * Update clip.
     */
    fun updateClip(clipId: String, update: (VideoClip) -> VideoClip) {
        val index = clips.indexOfFirst { it.id == clipId }
        if (index >= 0) {
            clips[index] = update(clips[index])
        }
    }

    /**
     * Get clip at time.
     */
    fun getClipAtTime(timeMs: Long): VideoClip? {
        return clips.find { it.containsTime(timeMs) }
    }

    /**
     * Get clips in range.
     */
    fun getClipsInRange(startMs: Long, endMs: Long): List<VideoClip> {
        return clips.filter { it.timelineStartMs < endMs && it.timelineEndMs > startMs }
    }

    /**
     * Check for overlaps.
     */
    fun detectOverlaps(): List<Pair<VideoClip, VideoClip>> {
        val overlaps = mutableListOf<Pair<VideoClip, VideoClip>>()

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
 * Base track interface.
 */
interface BaseTrack {
    val id: String
    var name: String
    var index: Int
    var isVisible: Boolean
    var isLocked: Boolean
    var isMuted: Boolean
    var volume: Float

    fun getClips(): List<Clip>
    fun getDuration(): Long
    fun getClipCount(): Int
}