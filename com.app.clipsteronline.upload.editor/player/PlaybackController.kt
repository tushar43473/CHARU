package com.app.clipsteronline.upload.editor.player

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
 * Controller for playback timeline synchronization.
 * Provides frame-accurate updates and scheduling.
 */
class PlaybackController(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
) {
    private val _playbackState = MutableStateFlow(PlaybackTimelineState())
    val playbackState: StateFlow<PlaybackTimelineState> = _playbackState.asStateFlow()

    private var playbackJob: Job? = null
    private var isScheduled = false

    private var videoPlayer: VideoPlayer? = null

    companion object {
        private const val UPDATE_INTERVAL_MS = 16L // ~60fps
        private const val FRAME_DURATION_MS = 33L // ~30fps frame
    }

    /**
     * Attach video player.
     */
    fun attachPlayer(player: VideoPlayer) {
        this.videoPlayer = player
    }

    /**
     * Detach player.
     */
    fun detachPlayer() {
        stopPlaybackLoop()
        this.videoPlayer = null
    }

    /**
     * Start playback loop.
     */
    fun startPlayback() {
        if (playbackJob?.isActive == true) return

        playbackJob = scope.launch {
            while (isActive) {
                videoPlayer?.let { player ->
                    if (player.isPlaying()) {
                        val position = player.getCurrentPosition()
                        val duration = player.getDuration()

                        _playbackState.value = _playbackState.value.copy(
                            currentPosition = position,
                            isPlaying = true,
                            progress = if (duration > 0) position.toFloat() / duration else 0f
                        )
                    }
                }
                delay(UPDATE_INTERVAL_MS)
            }
        }

        _playbackState.value = _playbackState.value.copy(isPlaying = true)
    }

    /**
     * Stop playback loop.
     */
    fun stopPlayback() {
        playbackJob?.cancel()
        playbackJob = null

        _playbackState.value = _playbackState.value.copy(isPlaying = false)
    }

    /**
     * Pause playback.
     */
    fun pause() {
        videoPlayer?.pause()
        stopPlayback()
    }

    /**
     * Resume playback.
     */
    fun resume() {
        videoPlayer?.play()
        startPlayback()
    }

    /**
     * Seek to position.
     */
    fun seekTo(positionMs: Long) {
        videoPlayer?.seekTo(positionMs)

        _playbackState.value = _playbackState.value.copy(
            currentPosition = positionMs,
            seekRequested = true
        )
    }

    /**
     * Seek to frame.
     */
    fun seekToFrame(frameNumber: Int, frameRate: Int = 30) {
        val positionMs = frameNumber * 1000L / frameRate
        seekTo(positionMs)
    }

    /**
     * Seek to percentage.
     */
    fun seekToPercent(percent: Float) {
        videoPlayer?.let { player ->
            val duration = player.getDuration()
            if (duration > 0) {
                val position = (duration * percent.coerceIn(0f, 1f)).toLong()
                seekTo(position)
            }
        }
    }

    /**
     * Schedule playback at specific time.
     */
    fun schedulePlayback(startTimeMs: Long, durationMs: Long) {
        _playbackState.value = _playbackState.value.copy(
            scheduledStart = startTimeMs,
            scheduledDuration = durationMs,
            isScheduled = true
        )
        isScheduled = true
    }

    /**
     * Cancel scheduled playback.
     */
    fun cancelScheduled() {
        _playbackState.value = _playbackState.value.copy(
            scheduledStart = 0,
            scheduledDuration = 0,
            isScheduled = false
        )
        isScheduled = false
    }

    /**
     * Check if scheduled playback ready.
     */
    fun isScheduledReady(): Boolean {
        return isScheduled &&
            _playbackState.value.currentPosition >= _playbackState.value.scheduledStart
    }

    /**
     * Get current position.
     */
    fun getCurrentPosition(): Long = _playbackState.value.currentPosition

    /**
     * Get duration.
     */
    fun getDuration(): Long = videoPlayer?.getDuration() ?: 0L

    /**
     * Get progress (0-1).
     */
    fun getProgress(): Float {
        val duration = getDuration()
        return if (duration > 0) getCurrentPosition().toFloat() / duration else 0f
    }

    /**
     * Get frame number at current position.
     */
    fun getCurrentFrame(frameRate: Int = 30): Int {
        return (getCurrentPosition() * frameRate / 1000).toInt()
    }

    /**
     * Step to next frame.
     */
    fun stepForward(frameRate: Int = 30) {
        stepToFrame(getCurrentFrame(frameRate) + 1, frameRate)
    }

    /**
     * Step to previous frame.
     */
    fun stepBackward(frameRate: Int = 30) {
        val currentFrame = getCurrentFrame(frameRate)
        if (currentFrame > 0) {
            stepToFrame(currentFrame - 1, frameRate)
        }
    }

    /**
     * Step to specific frame.
     */
    fun stepToFrame(frameNumber: Int, frameRate: Int = 30) {
        val positionMs = (frameNumber * 1000L / frameRate).toLong()
        seekTo(positionMs)
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
        val duration = getDuration()
        if (duration > 0) {
            seekTo(duration - FRAME_DURATION_MS)
        }
    }

    /**
     * Skip forward.
     */
    fun skipForward(ms: Long = 5000L) {
        val current = getCurrentPosition()
        val duration = getDuration()
        seekTo((current + ms).coerceAtMost(duration))
    }

    /**
     * Skip backward.
     */
    fun skipBackward(ms: Long = 5000L) {
        val current = getCurrentPosition()
        seekTo((current - ms).coerceAtLeast(0L))
    }

    /**
     * Is playing.
     */
    fun isPlaying(): Boolean = _playbackState.value.isPlaying

    /**
     * Release resources.
     */
    fun release() {
        stopPlayback()
        videoPlayer = null
    }
}

/**
 * Playback timeline state.
 */
data class PlaybackTimelineState(
    val currentPosition: Long = 0L,
    val isPlaying: Boolean = false,
    val progress: Float = 0f,
    val scheduledStart: Long = 0L,
    val scheduledDuration: Long = 0L,
    val isScheduled: Boolean = false,
    val seekRequested: Boolean = false
)