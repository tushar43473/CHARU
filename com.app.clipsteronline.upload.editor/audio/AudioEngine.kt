package com.app.clipsteronline.upload.editor.audio

import com.app.clipsteronline.upload.editor.core.model.AudioClip
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AudioEngine(
    private val mixer: AudioMixer = AudioMixer(),
    private val waveformGenerator: AudioWaveformGenerator = AudioWaveformGenerator(),
    private val beatDetector: BeatDetector = BeatDetector(),
) {
    private val trackPlayers = mutableMapOf<String, AudioTrackPlayer>()
    private val _state = MutableStateFlow(AudioState())
    val state: StateFlow<AudioState> = _state.asStateFlow()

    fun configure() = Unit

    fun loadTrack(trackId: String, clip: AudioClip) {
        val player = trackPlayers.getOrPut(trackId) { AudioTrackPlayer(trackId) }
        player.load(clip)
        emitState()
    }

    fun analyzePcm(samples: FloatArray, sampleRate: Int) {
        val waveform = waveformGenerator.generate(samples, sampleRate)
        val beats = beatDetector.detect(samples, sampleRate)
        _state.value = _state.value.copy(
            waveformPeaks = waveform.peaks,
            waveformRms = waveform.rms,
            beatMarkersMs = beats.beatMarkersMs,
            bpm = beats.bpm,
            beatConfidence = beats.confidence,
        )
    }

    fun play() {
        trackPlayers.values.forEach { it.play() }
        _state.value = _state.value.copy(isPlaying = true)
    }

    fun pause() {
        trackPlayers.values.forEach { it.pause() }
        _state.value = _state.value.copy(isPlaying = false)
    }

    fun seekTo(positionMs: Long) {
        trackPlayers.values.forEach { it.seekTo(positionMs) }
        val mixed = renderAt(positionMs)
        _state.value = _state.value.copy(positionMs = positionMs, outputLevel = mixed.level)
    }

    fun renderAt(positionMs: Long, masterVolume: Float = _state.value.masterVolume): AudioMixer.MixResult {
        val frames = trackPlayers.values.map { it.snapshot(positionMs) }
        val mixed = mixer.mix(frames, masterVolume)
        _state.value = _state.value.copy(positionMs = positionMs, outputLevel = mixed.level, activeTracks = mixed.activeTracks)
        return mixed
    }

    fun nearestBeat(positionMs: Long, thresholdMs: Long = 120L): Long? {
        val nearest = _state.value.beatMarkersMs.minByOrNull { kotlin.math.abs(it - positionMs) } ?: return null
        return nearest.takeIf { kotlin.math.abs(it - positionMs) <= thresholdMs }
    }

    fun setMasterVolume(volume: Float) {
        _state.value = _state.value.copy(masterVolume = volume.coerceIn(0f, 2f))
    }

    private fun emitState() {
        _state.value = _state.value.copy(trackCount = trackPlayers.size)
    }

    data class AudioState(
        val isPlaying: Boolean = false,
        val positionMs: Long = 0L,
        val trackCount: Int = 0,
        val activeTracks: Int = 0,
        val outputLevel: Float = 0f,
        val masterVolume: Float = 1f,
        val waveformPeaks: List<Float> = emptyList(),
        val waveformRms: List<Float> = emptyList(),
        val beatMarkersMs: List<Long> = emptyList(),
        val bpm: Float = 0f,
        val beatConfidence: Float = 0f,
    )
}
