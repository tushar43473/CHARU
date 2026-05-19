package com.app.clipsteronline.upload.editor.timeline.sync

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.app.clipsteronline.upload.editor.player.VideoPlayer

/**
 * Tracks playback position from Media3.
 * Calculates frame and progress.
 */
class PlaybackPositionTracker(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main),
    private val player: VideoPlayer? = null
) {
    private val _positionState = MutableStateFlow(PositionState())
    val positionState: StateFlow<PositionState> = _positionState.asStateFlow()

    private var lastPosition = 0L
    private var frameRate = 30
    private var playbackSpeed = 1f

    /**
     * Update position from player.
     */
    fun updatePosition(position: Long, duration: Long) {
        lastPosition = position

        val frameNumber = calculateFrame(position)
        val progress = calculateProgress(position, duration)
        val isBuffering = player?.state?.value?.isBuffering ?: false

        _positionState.value = PositionState(
            positionMs = position,
            durationMs = duration,
            frameNumber = frameNumber,
            progress = progress,
            isBuffering = isBuffering,
            playbackSpeed = playbackSpeed
        )
    }

    /**
     * Calculate frame number.
     */
    fun calculateFrame(positionMs: Long): Int {
        return (positionMs * frameRate / 1000).toInt()
    }

    /**
     * Calculate progress (0-1).
     */
    fun calculateProgress(positionMs: Long, durationMs: Long): Float {
        if (durationMs <= 0) return 0f
        return (positionMs.toFloat() / durationMs).coerceIn(0f, 1f)
    }

    /**
     * Get frame for position.
     */
    fun getFrameForPosition(positionMs: Long): Int {
        return (positionMs * frameRate / 1000).toInt()
    }

    /**
     * Get position for frame.
     */
    fun getPositionForFrame(frameNumber: Int): Long {
        return frameNumber * 1000L / frameRate
    }

    /**
     * Set frame rate.
     */
    fun setFrameRate(rate: Int) {
        this.frameRate = rate.coerceIn(1, 120)
    }

    /**
     * Set playback speed.
     */
    fun setPlaybackSpeed(speed: Float) {
        this.playbackSpeed = speed.coerceIn(0.25f, 4f)
    }

    /**
     * Get current position.
     */
    fun getPosition(): Long = lastPosition

    /**
     * Get frame.
     */
    fun getCurrentFrame(): Int {
        return calculateFrame(lastPosition)
    }

    /**
     * Get progress.
     */
    fun getProgress(duration: Long): Float {
        return calculateProgress(lastPosition, duration)
    }
}

/**
 * Position state.
 */
data class PositionState(
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val frameNumber: Int = 0,
    val progress: Float = 0f,
    val isBuffering: Boolean = false,
    val playbackSpeed: Float = 1f
)