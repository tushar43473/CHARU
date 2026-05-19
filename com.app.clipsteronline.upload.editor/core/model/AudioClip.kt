package com.app.clipsteronline.upload.editor.core.model

import android.net.Uri

/**
 * Represents an audio clip in the timeline.
 * Contains all audio-specific properties and processing.
 */
data class AudioClip(
    val id: String,
    val sourceUri: Uri,
    val sourceDurationMs: Long,
    val timelineStartMs: Long,
    val timelineEndMs: Long,
    val trimStartMs: Long = 0L,
    val trimEndMs: Long = 0L,
    val speed: Float = 1.0f,
    val volume: Float = 1.0f,
    val isMuted: Boolean = false,
    val isLoop: Boolean = false,
    val fadeInDurationMs: Long = 0L,
    val fadeOutDurationMs: Long = 0L,
    val waveformData: WaveformData? = null,
    val equalizer: EqualizerPreset? = null,
    val effects: List<AudioEffect> = emptyList(),
    val keyframes: List<Keyframe> = emptyList()
) : Clip {

    val durationMs: Long
        get() = timelineEndMs - timelineStartMs

    val effectiveDurationMs: Long
        get() = ((timelineEndMs - timelineStartMs) / speed).toLong()

    val actualTrimStartMs: Long
        get() = trimStartMs

    val actualTrimEndMs: Long
        get() = trimEndMs

    val isVisible: Boolean
        get() = volume > 0f && !isMuted

    fun getPlaybackRange(): LongRange {
        return timelineStartMs until timelineEndMs
    }

    fun containsTime(timeMs: Long): Boolean {
        return timeMs >= timelineStartMs && timeMs < timelineEndMs
    }

    fun withVolume(newVolume: Float): AudioClip {
        return copy(volume = newVolume.coerceIn(0f, 1f))
    }

    fun withSpeed(newSpeed: Float): AudioClip {
        return copy(speed = newSpeed.coerceIn(0.25f, 4.0f))
    }

    fun withTrim(startMs: Long, endMs: Long): AudioClip {
        return copy(trimStartMs = startMs, trimEndMs = endMs)
    }

    fun withFade(fadeInMs: Long, fadeOutMs: Long): AudioClip {
        return copy(fadeInDurationMs = fadeInMs, fadeOutDurationMs = fadeOutMs)
    }

    fun withLoop(loop: Boolean): AudioClip {
        return copy(isLoop = loop)
    }

    fun withMute(mute: Boolean): AudioClip {
        return copy(isMuted = mute)
    }

    fun withWaveform(data: WaveformData?): AudioClip {
        return copy(waveformData = data)
    }

    fun withEffects(audioEffects: List<AudioEffect>): AudioClip {
        return copy(effects = audioEffects)
    }

    fun addEffect(effect: AudioEffect): AudioClip {
        return copy(effects = effects + effect)
    }

    fun toggleMute(): AudioClip {
        return copy(isMuted = !isMuted)
    }

    fun toggleLoop(): AudioClip {
        return copy(isLoop = !isLoop)
    }

    fun calculateVolumeAt(timeMs: Long): Float {
        var result = volume
        val clipTime = timeMs - timelineStartMs

        if (fadeInDurationMs > 0 && clipTime < fadeInDurationMs) {
            result *= clipTime.toFloat() / fadeInDurationMs
        }

        if (fadeOutDurationMs > 0) {
            val fadeStart = durationMs - fadeOutDurationMs
            if (clipTime > fadeStart) {
                result *= (durationMs - clipTime).toFloat() / fadeOutDurationMs
            }
        }

        return result
    }
}

/**
 * Waveform data for audio visualization.
 */
data class WaveformData(
    val samples: FloatArray,
    val sampleRate: Int,
    val channelCount: Int = 2
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WaveformData

        if (!samples.contentEquals(other.samples)) return false
        if (sampleRate != other.sampleRate) return false
        if (channelCount != other.channelCount) return false

        return true
    }

    override fun hashCode(): Int {
        var result = samples.contentHashCode()
        result = 31 * result + sampleRate
        result = 31 * result + channelCount
        return result
    }
}

/**
 * Audio effect presets.
 */
sealed class AudioEffect(val name: String) {
    data class BassBoost(val intensity: Int = 500) : AudioEffect("bass_boost")
    data class Virtualizer(val intensity: Int = 1000) : AudioEffect("virtualizer")
    data class Reverb(val preset: ReverbPreset = ReverbPreset.NONE) : AudioEffect("reverb")
    data class Equalizer(val preset: EqualizerPreset) : AudioEffect("equalizer")
    data class NoiseSuppression(val enabled: Boolean = true) : AudioEffect("noise_suppression")
    data class AutoVolume(val enabled: Boolean = true) : AudioEffect("auto_volume")
}

/**
 * Reverb effect presets.
 */
enum class ReverbPreset(val roomSize: Float, val damping: Float, val wetLevel: Float) {
    NONE(0f, 0f, 0f),
    SMALL_ROOM(0.3f, 0.5f, 0.3f),
    MEDIUM_ROOM(0.5f, 0.4f, 0.4f),
    LARGE_ROOM(0.7f, 0.3f, 0.5f),
    HALL(0.8f, 0.25f, 0.6f),
    CATHEDRAL(0.9f, 0.2f, 0.7f)
}

/**
 * Equalizer presets.
 */
enum class EqualizerPreset(
    val name: String,
    val bandGains: FloatArray
) {
    FLAT("Flat", floatArrayOf(0f, 0f, 0f, 0f, 0f)),
    BASS_BOOST("Bass Boost", floatArrayOf(4f, 3f, 0f, 0f, 0f)),
    BASS_CUT("Bass Cut", floatArrayOf(-4f, -3f, 0f, 0f, 0f)),
    TREBLE_BOOST("Treble Boost", floatArrayOf(0f, 0f, 0f, 3f, 4f)),
    TREBLE_CUT("Treble Cut", floatArrayOf(0f, 0f, 0f, -3f, -4f)),
    VOCAL_BOOST("Vocal Boost", floatArrayOf(-1f, 0f, 3f, 2f, 0f)),
    ROCK("Rock", floatArrayOf(4f, 2f, -1f, 2f, 4f)),
    POP("Pop", floatArrayOf(-1f, 0f, 2f, 3f, 1f)),
    JAZZ("Jazz", floatArrayOf(2f, 0f, 1f, 2f, 3f)),
    CLASSICAL("Classical", floatArrayOf(3f, 1f, 0f, 2f, 3f)),
    ELECTRONIC("Electronic", floatArrayOf(4f, 2f, -1f, 3f, 4f)),
    CUSTOM("Custom", floatArrayOf(0f, 0f, 0f, 0f, 0f));

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return bandGains.contentEquals((other as EqualizerPreset).bandGains)
    }

    override fun hashCode(): Int {
        return name.hashCode() + bandGains.contentHashCode()
    }
}