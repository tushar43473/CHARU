package com.app.clipsteronline.upload.editor.player

/**
 * Immutable playback state model.
 * Contains all playback information.
 */
data class PlaybackState(
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val isReady: Boolean = false,
    val duration: Long = 0L,
    val currentPosition: Long = 0L,
    val bufferedPosition: Long = 0L,
    val playbackSpeed: Float = 1f,
    val playbackMode: PlaybackMode = PlaybackMode.NORMAL,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val isMuted: Boolean = false,
    val volume: Float = 1f,
    val seekRequested: Boolean = false
) {

    /**
     * Progress as 0-1 float.
     */
    val progress: Float
        get() = if (duration > 0) currentPosition.toFloat() / duration else 0f

    /**
     * Remaining time in milliseconds.
     */
    val remainingTime: Long
        get() = (duration - currentPosition).coerceAtLeast(0L)

    /**
     * Check if at start.
     */
    val isAtStart: Boolean
        get() = currentPosition < 100

    /**
     * Check if at end.
     */
    val isAtEnd: Boolean
        get() = duration > 0 && currentPosition >= duration - 100

    /**
     * Check if can seek forward.
     */
    val canSeekForward: Boolean
        get() = !isAtEnd

    /**
     * Check if can seek backward.
     */
    val canSeekBackward: Boolean
        get() = !isAtStart

    /**
     * With playing state.
     */
    fun withPlaying(isPlaying: Boolean): PlaybackState {
        return copy(isPlaying = isPlaying)
    }

    /**
     * With buffering state.
     */
    fun withBuffering(isBuffering: Boolean): PlaybackState {
        return copy(isBuffering = isBuffering)
    }

    /**
     * With position.
     */
    fun withPosition(position: Long): PlaybackState {
        return copy(currentPosition = position)
    }

    /**
     * With duration.
     */
    fun withDuration(duration: Long): PlaybackState {
        return copy(duration = duration)
    }

    /**
     * With speed.
     */
    fun withSpeed(speed: Float): PlaybackState {
        return copy(playbackSpeed = speed)
    }

    /**
     * With muted state.
     */
    fun withMuted(isMuted: Boolean): PlaybackState {
        return copy(isMuted = isMuted)
    }

    /**
     * With volume.
     */
    fun withVolume(volume: Float): PlaybackState {
        return copy(volume = volume)
    }

    /**
     * With repeat mode.
     */
    fun withRepeatMode(repeatMode: RepeatMode): PlaybackState {
        return copy(repeatMode = repeatMode)
    }

    /**
     * With playback mode.
     */
    fun withPlaybackMode(playbackMode: PlaybackMode): PlaybackState {
        return copy(playbackMode = playbackMode)
    }

    companion object {
        val IDLE = PlaybackState()
    }
}

/**
 * Playback mode.
 */
enum class PlaybackMode {
    NORMAL,
    REVERSE,
    RAMP_DOWN,
    RAMP_UP,
    BOUNCE
}

/**
 * Repeat mode.
 */
enum class RepeatMode {
    OFF,
    ONE,
    ALL
}

/**
 * Playback direction.
 */
enum class PlaybackDirection {
    FORWARD,
    BACKWARD
}

/**
 * Seek mode for precision seeking.
 */
enum class SeekMode {
    CLOSEST,
    PREVIOUS_SYNC,
    NEXT_SYNC,
    CLOSEST_SYNC
}

/**
 * Playback event.
 */
sealed class PlaybackEvent {
    data object Play : PlaybackEvent()
    data object Pause : PlaybackEvent()
    data object Stop : PlaybackEvent()
    data class Seek(val positionMs: Long) : PlaybackEvent()
    data class SpeedChanged(val speed: Float) : PlaybackEvent()
    data class Error(val message: String) : PlaybackEvent()
    data object Completed : PlaybackEvent()
    data class Buffering(val isBuffering: Boolean) : PlaybackEvent()
}