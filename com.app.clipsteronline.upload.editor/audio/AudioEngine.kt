package com.app.clipsteronline.upload.editor.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Central audio coordinator.
 * Manages audio playback, tracks, and session.
 */
class AudioEngine(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {
    private val _audioState = MutableStateFlow(AudioEngineState())
    val audioState: StateFlow<AudioEngineState> = _audioState.asStateFlow()

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val audioPlayers = mutableMapOf<String, AudioTrackPlayer>()

    private var isAudioSessionActive = false

    /**
     * Initialize audio engine.
     */
    fun initialize() {
        _audioState.value = _audioState.value.copy(isInitialized = true)
    }

    /**
     * Add audio track.
     */
    fun addTrack(trackId: String, player: AudioTrackPlayer) {
        audioPlayers[trackId] = player
        _audioState.value = _audioState.value.copy(
            trackCount = audioPlayers.size
        )
    }

    /**
     * Remove audio track.
     */
    fun removeTrack(trackId: String) {
        audioPlayers.remove(trackId)
        _audioState.value = _audioState.value.copy(
            trackCount = audioPlayers.size
        )
    }

    /**
     * Get track player.
     */
    fun getTrackPlayer(trackId: String): AudioTrackPlayer? = audioPlayers[trackId]

    /**
     * Play all tracks.
     */
    fun play() {
        audioPlayers.values.forEach { it.play() }
        _audioState.value = _audioState.value.copy(isPlaying = true)
    }

    /**
     * Pause all tracks.
     */
    fun pause() {
        audioPlayers.values.forEach { it.pause() }
        _audioState.value = _audioState.value.copy(isPlaying = false)
    }

    /**
     * Stop all tracks.
     */
    fun stop() {
        audioPlayers.values.forEach { it.stop() }
        _audioState.value = _audioState.value.copy(isPlaying = false)
    }

    /**
     * Seek all tracks.
     */
    fun seekTo(positionMs: Long) {
        audioPlayers.values.forEach { it.seekTo(positionMs) }
        _audioState.value = _audioState.value.copy(currentPosition = positionMs)
    }

    /**
     * Set master volume.
     */
    fun setMasterVolume(volume: Float) {
        _audioState.value = _audioState.value.copy(masterVolume = volume)
    }

    /**
     * Request audio focus.
     */
    fun requestAudioFocus(): Boolean {
        val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
                .build())
            .build()

        val result = audioManager.requestAudioFocus(request)
        isAudioSessionActive = result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        return isAudioSessionActive
    }

    /**
     * Abandon audio focus.
     */
    fun abandonAudioFocus() {
        audioManager.abandonAudioFocusRequest(
            AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).build()
        )
        isAudioSessionActive = false
    }

    /**
     * Release resources.
     */
    fun release() {
        stop()
        audioPlayers.values.forEach { it.release() }
        audioPlayers.clear()
        abandonAudioFocus()
    }
}

/**
 * Audio engine state.
 */
data class AudioEngineState(
    val isInitialized: Boolean = false,
    val isPlaying: Boolean = false,
    val trackCount: Int = 0,
    val currentPosition: Long = 0L,
    val masterVolume: Float = 1f
)