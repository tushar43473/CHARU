package com.app.clipsteronline.upload.editor.audio

import kotlin.math.max

class AudioMixer {
    fun configure() = Unit

    fun mix(frames: List<AudioTrackPlayer.AudioFrame>, masterVolume: Float = 1f): MixResult {
        if (frames.isEmpty()) return MixResult(0f, 0, emptyMap())
        val clampedMaster = masterVolume.coerceIn(0f, 2f)
        var sum = 0f
        var active = 0
        val perTrack = mutableMapOf<String, Float>()
        frames.forEach { frame ->
            if (frame.gain > 0f && frame.clipId != null) {
                active += 1
                val normalized = frame.gain.coerceIn(0f, 4f)
                sum += normalized
                perTrack[frame.trackId] = normalized
            }
        }
        val compressed = if (active == 0) 0f else (sum / max(1, active)).coerceIn(0f, 1f)
        return MixResult((compressed * clampedMaster).coerceIn(0f, 1f), active, perTrack)
    }

    data class MixResult(
        val level: Float,
        val activeTracks: Int,
        val trackLevels: Map<String, Float>,
    )
}
