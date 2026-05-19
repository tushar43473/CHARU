package com.app.clipsteronline.upload.editor.player

import android.content.Context
import android.view.Surface
import android.view.SurfaceView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Controller for managing player actions and callbacks.
 * Handles playback callbacks and buffering.
 */
class PlayerController(
    private val context: Context
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _state = MutableStateFlow(PlayerControllerState())
    val state: StateFlow<PlayerControllerState> = _state.asStateFlow()

    private var player: VideoPlayer? = null
    private var updateJob: Job? = null

    private var playbackListener: PlaybackCallback? = null

    companion object {
        private const val UPDATE_INTERVAL_MS = 16L // ~60fps
        private const val BUFFER_CHECK_INTERVAL_MS = 100L
    }

    /**
     * Attach player.
     */
    fun attachPlayer(videoPlayer: VideoPlayer) {
        this.player = videoPlayer
        startPositionUpdates()
    }

    /**
     * Detach player.
     */
    fun detachPlayer() {
        stopPositionUpdates()
        player = null
    }

    /**
     * Set playback callback.
     */
    fun setPlaybackCallback(callback: PlaybackCallback?) {
        this.playbackListener = callback
    }

    /**
     * Play.
     */
    fun play(): Boolean {
        player?.play() ?: return false
        _state.value = _state.value.copy(isPlaying = true)
        playbackListener?.onPlaybackStarted()
        return true
    }

    /**
     * Pause.
     */
    fun pause(): Boolean {
        player?.pause() ?: return false
        _state.value = _state.value.copy(isPlaying = false)
        playbackListener?.onPlaybackPaused()
        return true
    }

    /**
     * Toggle play/pause.
     */
    fun togglePlayPause() {
        if (player?.isPlaying() == true) {
            pause()
        } else {
            play()
        }
    }

    /**
     * Seek to.
     */
    fun seekTo(positionMs: Long) {
        player?.seekTo(positionMs)
        _state.value = _state.value.copy(currentPosition = positionMs)
        playbackListener?.onSeekTo(positionMs)
    }

    /**
     * Seek to frame.
     */
    fun seekToFrame(frameNumber: Int, frameRate: Int = 30) {
        player?.seekToFrame(frameNumber, frameRate)
    }

    /**
     * Step forward.
     */
    fun stepForward(frameRate: Int = 30) {
        player?.stepForward(frameRate)
    }

    /**
     * Step backward.
     */
    fun stepBackward(frameRate: Int = 30) {
        player?.stepBackward(frameRate)
    }

    /**
     * Set speed.
     */
    fun setSpeed(speed: Float) {
        player?.setPlaybackSpeed(speed)
        _state.value = _state.value.copy(speed = speed)
    }

    /**
     * Go to start.
     */
    fun goToStart() {
        seekTo(0)
    }

    /**
     * Go to end.
     */
    fun goToEnd() {
        player?.let {
            seekTo(it.getDuration())
        }
    }

    /**
     * Skip forward.
     */
    fun skipForward(ms: Long = 5000L) {
        player?.let {
            val current = it.getCurrentPosition()
            val duration = it.getDuration()
            seekTo((current + ms).coerceAtMost(duration))
        }
    }

    /**
     * Skip backward.
     */
    fun skipBackward(ms: Long = 5000L) {
        player?.let {
            val current = it.getCurrentPosition()
            seekTo((current - ms).coerceAtLeast(0L))
        }
    }

    /**
     * Go to percentage.
     */
    fun seekToPercent(percent: Float) {
        player?.let {
            val duration = it.getDuration()
            val position = (duration * percent.coerceIn(0f, 1f)).toLong()
            seekTo(position)
        }
    }

    /**
     * Set loop mode.
     */
    fun setLoopMode(enabled: Boolean) {
        player?.setLoopMode(enabled)
        _state.value = _state.value.copy(loopEnabled = enabled)
    }

    /**
     * Toggle loop.
     */
    fun toggleLoop() {
        val current = _state.value.loopEnabled
        setLoopMode(!current)
    }

    /**
     * Set mute.
     */
    fun setMuted(muted: Boolean) {
        player?.setMuted(muted)
        _state.value = _state.value.copy(isMuted = muted)
    }

    /**
     * Toggle mute.
     */
    fun toggleMute() {
        player?.toggleMute()
        _state.value = _state.value.copy(isMuted = !_state.value.isMuted)
    }

    /**
     * Set volume.
     */
    fun setVolume(volume: Float) {
        player?.setVolume(volume)
    }

    /**
     * Get current position.
     */
    fun getCurrentPosition(): Long = player?.getCurrentPosition() ?: 0L

    /**
     * Get duration.
     */
    fun getDuration(): Long = player?.getDuration() ?: 0L

    /**
     * Get buffered position.
     */
    fun getBufferedPosition(): Long = player?.getBufferedPosition() ?: 0L

    /**
     * Is playing.
     */
    fun isPlaying(): Boolean = _state.value.isPlaying

    /**
     * Check if end reached.
     */
    fun isAtEnd(): Boolean {
        val position = player?.getCurrentPosition() ?: 0L
        val duration = player?.getDuration() ?: 0L
        return duration > 0 && position >= duration - 100
    }

    /**
     * Check if at start.
     */
    fun isAtStart(): Boolean = getCurrentPosition() < 100

    /**
     * Release.
     */
    fun release() {
        stopPositionUpdates()
        player = null
    }

    /**
     * Start position updates.
     */
    private fun startPositionUpdates() {
        updateJob?.cancel()
        updateJob = scope.launch {
            while (isActive) {
                player?.let { p ->
                    val position = p.getCurrentPosition()
                    _state.value = _state.value.copy(
                        currentPosition = position,
                        isPlaying = p.isPlaying()
                    )
                }
                delay(UPDATE_INTERVAL_MS)
            }
        }
    }

    /**
     * Stop position updates.
     */
    private fun stopPositionUpdates() {
        updateJob?.cancel()
        updateJob = null
    }
}

/**
 * Player controller state.
 */
data class PlayerControllerState(
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val currentPosition: Long = 0L,
    val bufferedPosition: Long = 0L,
    val speed: Float = 1f,
    val loopEnabled: Boolean = false,
    val isMuted: Boolean = false
)

/**
 * Playback callback interface.
 */
interface PlaybackCallback {
    fun onPlaybackStarted()
    fun onPlaybackPaused()
    fun onPlaybackStopped()
    fun onPlaybackCompleted()
    fun onSeekTo(positionMs: Long)
    fun onBufferingStarted()
    fun onBufferingEnded()
    fun onError(error: String)
}