package com.app.clipsteronline.upload.editor.core.model

/**
 * Represents a track in the timeline.
 * Contains the track type, clips, and visibility/lock states.
 */
data class TimelineTrack(
    val id: String,
    val type: TrackType,
    val index: Int,
    val name: String,
    val clips: List<Clip> = emptyList(),
    val isVisible: Boolean = true,
    val isLocked: Boolean = false,
    val isMuted: Boolean = false,
    val volume: Float = 1f,
    val pan: Float = 0f,
    val solo: Boolean = false
) {

    val durationMs: Long
        get() = clips.maxOfOrNull { it.timelineEndMs } ?: 0L

    val clipCount: Int
        get() = clips.size

    val isEmpty: Boolean
        get() = clips.isEmpty()

    fun getClipAtIndex(index: Int): Clip? {
        return clips.getOrNull(index)
    }

    fun getClipById(clipId: String): Clip? {
        return clips.find { it.id == clipId }
    }

    fun findClipAtTime(timeMs: Long): Clip? {
        return clips.find { it.containsTime(timeMs) }
    }

    fun withClips(newClips: List<Clip>): TimelineTrack {
        return copy(clips = newClips)
    }

    fun addClip(clip: Clip): TimelineTrack {
        return copy(clips = clips + clip)
    }

    fun removeClip(clipId: String): TimelineTrack {
        return copy(clips = clips.filter { it.id != clipId })
    }

    fun updateClip(clipId: String, update: (Clip) -> Clip): TimelineTrack {
        return copy(clips = clips.map { if (it.id == clipId) update(it) else it })
    }

    fun withVisibility(visible: Boolean): TimelineTrack {
        return copy(isVisible = visible)
    }

    fun withLocked(locked: Boolean): TimelineTrack {
        return copy(isLocked = locked)
    }

    fun withMuted(muted: Boolean): TimelineTrack {
        return copy(isMuted = muted)
    }

    fun withVolume(volume: Float): TimelineTrack {
        return copy(volume = volume.coerceIn(0f, 1f))
    }

    fun withPan(pan: Float): TimelineTrack {
        return copy(pan = pan.coerceIn(-1f, 1f))
    }

    fun withSolo(solo: Boolean): TimelineTrack {
        return copy(solo = solo)
    }

    fun sortClips(): TimelineTrack {
        return copy(clips = clips.sortedBy { it.timelineStartMs })
    }

    fun canEdit(): Boolean {
        return isVisible && !isLocked
    }

    fun getEditableClips(): List<Clip> {
        return if (canEdit()) clips else emptyList()
    }

    companion object {
        fun createVideoTrack(id: String, index: Int, name: String): TimelineTrack {
            return TimelineTrack(
                id = id,
                type = TrackType.VIDEO,
                index = index,
                name = name
            )
        }

        fun createAudioTrack(id: String, index: Int, name: String): TimelineTrack {
            return TimelineTrack(
                id = id,
                type = TrackType.AUDIO,
                index = index,
                name = name
            )
        }

        fun createStickerTrack(id: String, index: Int, name: String): TimelineTrack {
            return TimelineTrack(
                id = id,
                type = TrackType.STICKER,
                index = index,
                name = name
            )
        }

        fun createTextTrack(id: String, index: Int, name: String): TimelineTrack {
            return TimelineTrack(
                id = id,
                type = TrackType.TEXT,
                index = index,
                name = name
            )
        }
    }
}

/**
 * Track types in the timeline.
 */
enum class TrackType(val displayName: String) {
    VIDEO("Video"),
    AUDIO("Audio"),
    STICKER("Sticker"),
    TEXT("Text"),
    EFFECT("Effect");

    fun isMedia(): Boolean = this == VIDEO || this == AUDIO
    fun isOverlay(): Boolean = this == STICKER || this == TEXT
}

/**
 * Track configuration presets.
 */
enum class TrackPreset(
    val trackType: TrackType,
    val displayName: String,
    val maxDurationMs: Long
) {
    VIDEO_PRIMARY(TrackType.VIDEO, "Video", Long.MAX_VALUE),
    VIDEO_SECONDARY(TrackType.VIDEO, "Overlay", Long.MAX_VALUE),
    AUDIO_MUSIC(TrackType.AUDIO, "Music", Long.MAX_VALUE),
    AUDIO_VOICE(TrackType.AUDIO, "Voice", Long.MAX_VALUE),
    AUDIO_SOUND(TrackType.AUDIO, "Sound", Long.MAX_VALUE),
    TEXT_TITLE(TrackType.TEXT, "Title", Long.MAX_VALUE),
    TEXT_SUBTITLE(TrackType.TEXT, "Subtitle", Long.MAX_VALUE),
    STICKER_EMOJI(TrackType.STICKER, "Emoji", Long.MAX_VALUE),
    STICKER_ANIMATED(TrackType.STICKER, "Animated", Long.MAX_VALUE)
}