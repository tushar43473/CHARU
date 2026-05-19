package com.app.clipsteronline.upload.editor.core.manager

import android.content.Context
import android.net.Uri
import android.view.Surface
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Manager for media playback.
 * Handles ExoPlayer lifecycle, seek, and playback state.
 */
class PlayerManager(
    private val context: Context
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var player: ExoPlayer? = null
    private var surface: Surface? = null

    private val _playerState = MutableStateFlow<PlayerState>(PlayerState.Idle)
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val playerListeners = CopyOnWriteArrayList<Player.Listener>()

    private var currentMediaUri: Uri? = null

    /**
     * Initialize player.
     */
    fun initialize(): Boolean {
        if (player != null) return true

        player = ExoPlayer.Builder(context).build().apply {
            addListener(createPlayerListener())
        }

        _playerState.value = PlayerState.Ready
        return true
    }

    /**
     * Prepare media for playback.
     */
    fun prepare(uri: Uri) {
        initialize()

        currentMediaUri = uri
        val mediaItem = MediaItem.fromUri(uri)

        player?.apply {
            setMediaItem(mediaItem)
            prepare()
        }

        _playerState.value = PlayerState.Buffering
    }

    /**
     * Prepare media with surface.
     */
    fun prepareWithSurface(uri: Uri, surface: Surface) {
        this.surface = surface

        initialize()
        player?.setVideoSurface(surface)

        prepare(uri)
    }

    /**
     * Play.
     */
    fun play() {
        player?.play()
        _isPlaying.value = true
        _playerState.value = PlayerState.Playing
    }

    /**
     * Pause.
     */
    fun pause() {
        player?.pause()
        _isPlaying.value = false
        _playerState.value = PlayerState.Paused
    }

    /**
     * Stop.
     */
    fun stop() {
        player?.stop()
        _isPlaying.value = false
        _playerState.value = PlayerState.Stopped
    }

    /**
     * Seek to position.
     */
    fun seekTo(positionMs: Long) {
        player?.seekTo(positionMs)
        _currentPosition.value = positionMs
    }

    /**
     * Seek to frame.
     */
    fun seekToFrame(frameNumber: Int, frameRate: Int = 30) {
        val positionMs = (frameNumber * 1000L / frameRate)
        seekTo(positionMs)
    }

    /**
     * Step forward one frame.
     */
    fun stepForward(frameRate: Int = 30) {
        val frameDurationMs = 1000L / frameRate
        val newPosition = (_currentPosition.value + frameDurationMs).coerceAtMost(_duration.value)
        seekTo(newPosition)
    }

    /**
     * Step backward one frame.
     */
    fun stepBackward(frameRate: Int = 30) {
        val frameDurationMs = 1000L / frameRate
        val newPosition = (_currentPosition.value - frameDurationMs).coerceAtLeast(0L)
        seekTo(newPosition)
    }

    /**
     * Set playback speed.
     */
    fun setPlaybackSpeed(speed: Float) {
        val clampedSpeed = speed.coerceIn(0.25f, 4.0f)
        player?.playbackParameters = PlaybackParameters(clampedSpeed)
        _playbackSpeed.value = clampedSpeed
    }

    /**
     * Set repeat mode.
     */
    fun setRepeatMode(mode: Int) {
        player?.repeatMode = mode
    }

    /**
     * Set volume.
     */
    fun setVolume(volume: Float) {
        player?.volume = volume.coerceIn(0f, 1f)
    }

    /**
     * Get player.
     */
    fun getPlayer(): ExoPlayer? = player

    /**
     * Get current position.
     */
    fun getCurrentPosition(): Long = player?.currentPosition ?: 0L

    /**
     * Get duration.
     */
    fun getDuration(): Long = player?.duration ?: 0L

    /**
     * Check if playing.
     */
    fun isPlaying(): Boolean = player?.isPlaying ?: false

    /**
     * Check if ready.
     */
    fun isReady(): Boolean = player?.playbackState == Player.STATE_READY

    /**
     * Release player.
     */
    fun release() {
        player?.release()
        player = null
        surface = null
        _playerState.value = PlayerState.Idle
        _isPlaying.value = false
    }

    /**
     * Reset player.
     */
    fun reset() {
        release()
        initialize()
    }

    /**
     * Add player listener.
     */
    fun addListener(listener: Player.Listener) {
        player?.addListener(listener)
        playerListeners.add(listener)
    }

    /**
     * Remove player listener.
     */
    fun removeListener(listener: Player.Listener) {
        player?.removeListener(listener)
        playerListeners.remove(listener)
    }

    /**
     * Get buffered position.
     */
    fun getBufferedPosition(): Long = player?.bufferedPosition ?: 0L

    /**
     * Get playback state.
     */
    private fun createPlayerListener(): Player.Listener {
        return object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_IDLE -> _playerState.value = PlayerState.Idle
                    Player.STATE_BUFFERING -> _playerState.value = PlayerState.Buffering
                    Player.STATE_READY -> {
                        _duration.value = player?.duration ?: 0L
                        _playerState.value = PlayerState.Ready
                    }
                    Player.STATE_ENDED -> {
                        _playerState.value = PlayerState.Ended
                        _isPlaying.value = false
                    }
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
                if (isPlaying) {
                    _playerState.value = PlayerState.Playing
                    startPositionUpdates()
                }
            }

            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo
            ) {
                _currentPosition.value = newPosition.positionMs
            }
        }
    }

    /**
     * Start position updates.
     */
    private fun startPositionUpdates() {
        scope.launch {
            while (_isPlaying.value) {
                _currentPosition.value = player?.currentPosition ?: 0L
                kotlinx.coroutines.delay(16) // ~60fps updates
            }
        }
    }
}

/**
 * Player state.
 */
sealed class PlayerState {
    data object Idle : PlayerState()
    data object Buffering : PlayerState()
    data object Ready : PlayerState()
    data object Playing : PlayerState()
    data object Paused : PlayerState()
    data object Stopped : PlayerState()
    data object Ended : PlayerState()
    data class Error(val message: String) : PlayerState()
}