package com.app.clipsteronline.upload.editor.audio

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Audio track player.
 * Handles audio playback, mute, speed, loop.
 */
class AudioTrackPlayer(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
) {
    private val _playerState = MutableStateFlow(AudioPlayerState())
    val playerState: StateFlow<AudioPlayerState> = _playerState.asStateFlow()

    private var uri: Uri? = null
    private var isMuted = false
    private var volume = 1f
    private var playbackSpeed = 1f
    private var isLooping = false
    private var trackId: String = ""

    /**
     * Set track ID.
     */
    fun setTrackId(id: String) {
        this.trackId = id
    }

    /**
     * Load audio.
     */
    fun load(uri: Uri) {
        this.uri = uri
        _playerState.value = _playerState.value.copy(uri = uri.toString())
    }

    /**
     * Play.
     */
    fun play() {
        _playerState.value = _playerState.value.copy(isPlaying = true)
    }

    /**
     * Pause.
     */
    fun pause() {
        _playerState.value = _playerState.value.copy(isPlaying = false)
    }

    /**
     * Stop.
     */
    fun stop() {
        _playerState.value = _playerState.value.copy(
            isPlaying = false,
            position = 0
        )
    }

    /**
     * Seek to position.
     */
    fun seekTo(positionMs: Long) {
        _playerState.value = _playerState.value.copy(position = positionMs)
    }

    /**
     * Set volume.
     */
    fun setVolume(volume: Float) {
        this.volume = volume.coerceIn(0f, 1f)
        updateState()
    }

    /**
     * Set muted.
     */
    fun setMuted(muted: Boolean) {
        isMuted = muted
        updateState()
    }

    /**
     * Toggle mute.
     */
    fun toggleMute() {
        isMuted = !isMuted
        updateState()
    }

    /**
     * Set playback speed.
     */
    fun setPlaybackSpeed(speed: Float) {
        playbackSpeed = speed.coerceIn(0.25f, 4f)
        _playerState.value = _playerState.value.copy(playbackSpeed = playbackSpeed)
    }

    /**
     * Set loop.
     */
    fun setLooping(loop: Boolean) {
        isLooping = loop
        _playerState.value = _playerState.value.copy(isLooping = loop)
    }

    /**
     * Get volume.
     */
    fun getVolume(): Float = if (isMuted) 0f else volume

    /**
     * Get track ID.
     */
    fun getTrackId(): String = trackId

    /**
     * Release.
     */
    fun release() {
        stop()
    }

    /**
     * Update state.
     */
    private fun updateState() {
        _playerState.value = _playerState.value.copy(
            volume = getVolume(),
            isMuted = isMuted
        )
    }
}

/**
 * Audio player state.
 */
data class AudioPlayerState(
    val uri: String? = null,
    val isPlaying: Boolean = false,
    val position: Long = 0L,
    val duration: Long = 0L,
    val volume: Float = 1f,
    val isMuted: Boolean = false,
    val playbackSpeed: Float = 1f,
    val isLooping: Boolean = false
) {
    /**
     * Get progress.
     */
    val progress: Float
        get() = if (duration > 0) position.toFloat() / duration else 0f
}