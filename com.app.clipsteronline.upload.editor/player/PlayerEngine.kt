package com.app.clipsteronline.upload.editor.player

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Player engine.
 * Central player management, Media3 integration.
 */
class PlayerEngine(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
) : LifecycleEventObserver {

    private val _playerState = MutableStateFlow(PlayerEngineState())
    val playerState: StateFlow<PlayerEngineState> = _playerState.asStateFlow()

    private var videoPlayer: VideoPlayer? = null
    private var playbackController: PlaybackController? = null

    /**
     * Initialize player.
     */
    fun initialize() {
        videoPlayer = VideoPlayer(context)
        playbackController = PlaybackController(context)

        _playerState.value = _playerState.value.copy(isInitialized = true)
    }

    /**
     * Play.
     */
    fun play() {
        videoPlayer?.play()
        _playerState.value = _playerState.value.copy(isPlaying = true)
    }

    /**
     * Pause.
     */
    fun pause() {
        videoPlayer?.pause()
        _playerState.value = _playerState.value.copy(isPlaying = false)
    }

    /**
     * Seek to position.
     */
    fun seekTo(positionMs: Long) {
        videoPlayer?.seekTo(positionMs)
        _playerState.value = _playerState.value.copy(currentPosition = positionMs)
    }

    /**
     * Get current position.
     */
    fun getCurrentPosition(): Long = videoPlayer?.currentPosition ?: 0L

    /**
     * Get duration.
     */
    fun getDuration(): Long = videoPlayer?.duration ?: 0L

    /**
     * Set video URI.
     */
    fun setVideoSource(uri: android.net.Uri) {
        videoPlayer?.setSource(uri)
    }

    /**
     * Set playback speed.
     */
    fun setPlaybackSpeed(speed: Float) {
        videoPlayer?.setPlaybackSpeed(speed)
    }

    /**
     * Release resources.
     */
    fun release() {
        videoPlayer?.release()
        _playerState.value = PlayerEngineState()
    }

    /**
     * Lifecycle observer.
     */
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_PAUSE -> pause()
            Lifecycle.Event.ON_RESUME -> initialize()
            Lifecycle.Event.ON_DESTROY -> release()
            else -> {}
        }
    }
}

/**
 * Player engine state.
 */
data class PlayerEngineState(
    val isInitialized: Boolean = false,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val playbackSpeed: Float = 1f
)