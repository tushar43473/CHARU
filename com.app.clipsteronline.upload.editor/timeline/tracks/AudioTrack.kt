package com.app.clipsteronline.upload.editor.timeline.tracks

import com.app.clipsteronline.upload.editor.core.model.AudioClip

class AudioTrack(
    val id: String,
    val name: String,
    val clips: List<AudioClip>,
    val muted: Boolean = false,
    val solo: Boolean = false,
) {
    init {
        require(id.isNotBlank())
        require(name.isNotBlank())
    }

    fun configure() = Unit

    fun clipAt(timeMs: Long): AudioClip? {
        if (muted) return null
        return clips.firstOrNull { timeMs in it.startMs..it.endMs }
    }

    fun activeClips(timeMs: Long): List<AudioClip> {
        if (muted) return emptyList()
        return clips.filter { timeMs in it.startMs..it.endMs }
    }
}
