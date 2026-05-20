package com.app.clipsteronline.upload.editor.audio

import com.app.clipsteronline.upload.editor.core.model.AudioClip

class AudioTrackPlayer(
    val trackId: String,
) {
    private var clip: AudioClip? = null
    private var playing = false
    private var currentPositionMs: Long = 0L

    fun configure() = Unit

    fun load(audioClip: AudioClip) {
        clip = audioClip
        currentPositionMs = audioClip.startMs
    }

    fun play() {
        playing = true
    }

    fun pause() {
        playing = false
    }

    fun seekTo(positionMs: Long) {
        val c = clip ?: return
        currentPositionMs = positionMs.coerceIn(c.startMs, c.endMs)
    }

    fun snapshot(positionMs: Long): AudioFrame {
        val c = clip ?: return AudioFrame.silence(trackId, positionMs)
        val position = positionMs.coerceIn(c.startMs, c.endMs)
        val localMs = (position - c.startMs) + c.trimStartMs
        val gain = c.volume * c.volumeAtTimelineMs(position)
        return AudioFrame(trackId = trackId, clipId = c.clipId, positionMs = position, sourceTimeMs = localMs, gain = gain)
    }

    data class AudioFrame(
        val trackId: String,
        val clipId: String?,
        val positionMs: Long,
        val sourceTimeMs: Long,
        val gain: Float,
    ) {
        companion object {
            fun silence(trackId: String, positionMs: Long) = AudioFrame(trackId, null, positionMs, 0L, 0f)
        }
    }
}
