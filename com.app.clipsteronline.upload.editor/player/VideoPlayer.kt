package com.app.clipsteronline.upload.editor.player

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.view.Surface
import android.view.SurfaceView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Video player wrapper using Media3 ExoPlayer.
 * Provides video playback with cinematic black background.
 */
class VideoPlayer(
    private val context: Context
) {
    private var player: ExoPlayer? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _state = MutableStateFlow(VideoPlayerState())
    val state: StateFlow<VideoPlayerState> = _state.asStateFlow()

    private var listener: PlayerListener? = null

    /**
     * Initialize player.
     */
    fun initialize(): ExoPlayer {
        if (player != null) return player!!

        player = ExoPlayer.Builder(context)
            .build()
            .apply {
                playWhenReady = false
                repeatMode = Player.REPEAT_MODE_OFF

                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        updateStateFromPlayer()
                    }

                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        updateStateFromPlayer()
                    }

                    override fun onPositionDiscontinuity(
                        oldPosition: Player.PositionInfo,
                        newPosition: Player.PositionInfo
                    ) {
                        updatePosition(newPosition.positionMs)
                    }

                    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
                        updateStateFromPlayer()
                    }
                })
            }

        updateStateFromPlayer()
        return player!!
    }

    /**
     * Initialize with surface.
     */
    fun initializeWithSurface(surface: Surface): ExoPlayer {
        val p = initialize()
        p.setVideoSurface(surface)
        return p
    }

    /**
     * Load video URI.
     */
    fun loadVideo(uri: Uri) {
        val mediaItem = MediaItem.fromUri(uri)
        player?.apply {
            setMediaItem(mediaItem)
            prepare()
        }
        updateStateFromPlayer()
    }

    /**
     * Load video from path.
     */
    fun loadVideo(path: String) {
        loadVideo(Uri.parse(path))
    }

    /**
     * Play.
     */
    fun play() {
        player?.play()
    }

    /**
     * Pause.
     */
    fun pause() {
        player?.pause()
    }

    /**
     * Play or pause toggle.
     */
    fun playPause() {
        player?.let {
            if (it.isPlaying) pause() else play()
        }
    }

    /**
     * Stop playback.
     */
    fun stop() {
        player?.stop()
    }

    /**
     * Seek to position in milliseconds.
     */
    fun seekTo(positionMs: Long) {
        player?.seekTo(positionMs.coerceAtLeast(0L))
        updatePosition(positionMs)
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
        val currentPosition = player?.currentPosition ?: 0L
        val duration = player?.duration ?: Long.MAX_VALUE
        seekTo((currentPosition + frameDurationMs).coerceAtMost(duration))
    }

    /**
     * Step backward one frame.
     */
    fun stepBackward(frameRate: Int = 30) {
        val frameDurationMs = 1000L / frameRate
        val currentPosition = player?.currentPosition ?: 0L
        seekTo((currentPosition - frameDurationMs).coerceAtLeast(0L))
    }

    /**
     * Set playback speed.
     */
    fun setPlaybackSpeed(speed: Float) {
        val clampedSpeed = speed.coerceIn(0.25f, 4.0f)
        player?.playbackParameters = PlaybackParameters(clampedSpeed)
    }

    /**
     * Set loop mode.
     */
    fun setLoopMode(enabled: Boolean) {
        player?.repeatMode = if (enabled) {
            Player.REPEAT_MODE_ONE
        } else {
            Player.REPEAT_MODE_OFF
        }
    }

    /**
     * Set mute state.
     */
    fun setMuted(muted: Boolean) {
        player?.volume = if (muted) 0f else 1f
    }

    /**
     * Toggle mute.
     */
    fun toggleMute() {
        player?.let {
            it.volume = if (it.volume > 0) 0f else 1f
        }
    }

    /**
     * Set volume.
     */
    fun setVolume(volume: Float) {
        player?.volume = volume.coerceIn(0f, 1f)
    }

    /**
     * Set video surface.
     */
    fun setVideoSurface(surface: Surface?) {
        surface?.let { player?.setVideoSurface(it) }
    }

    /**
     * Set video surface view.
     */
    fun setVideoSurfaceView(surfaceView: SurfaceView?) {
        surfaceView?.holder?.let { player?.setVideoSurface(it.surface) }
    }

    /**
     * Get player instance.
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
     * Get buffered position.
     */
    fun getBufferedPosition(): Long = player?.bufferedPosition ?: 0L

    /**
     * Check if playing.
     */
    fun isPlaying(): Boolean = player?.isPlaying ?: false

    /**
     * Check if ready.
     */
    fun isReady(): Boolean = player?.playbackState == Player.STATE_READY

    /**
     * Check if muted.
     */
    fun isMuted(): Boolean = (player?.volume ?: 1f) == 0f

    /**
     * Release player.
     */
    fun release() {
        player?.release()
        player = null
    }

    /**
     * Reset player state.
     */
    fun reset() {
        player?.seekTo(0)
        player?.pause()
    }

    /**
     * Update state from player.
     */
    private fun updateStateFromPlayer() {
        player?.let { p ->
            _state.value = VideoPlayerState(
                isPlaying = p.isPlaying,
                isBuffering = p.playbackState == Player.STATE_BUFFERING,
                isReady = p.playbackState == Player.STATE_READY,
                duration = p.duration,
                currentPosition = p.currentPosition,
                bufferedPosition = p.bufferedPosition,
                playbackSpeed = p.playbackParameters.speed,
                repeatMode = p.repeatMode
            )
        }
    }

    /**
     * Update position.
     */
    private fun updatePosition(positionMs: Long) {
        _state.value = _state.value.copy(currentPosition = positionMs)
    }
}

/**
 * Video player state.
 */
data class VideoPlayerState(
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val isReady: Boolean = false,
    val duration: Long = 0L,
    val currentPosition: Long = 0L,
    val bufferedPosition: Long = 0L,
    val playbackSpeed: Float = 1f,
    val repeatMode: Int = Player.REPEAT_MODE_OFF
)

/**
 * Player listener interface.
 */
interface PlayerListener {
    fun onPlaybackStarted()
    fun onPlaybackPaused()
    fun onPlaybackStopped()
    fun onPlaybackCompleted()
    fun onSeekTo(positionMs: Long)
    fun onError(error: String)
}