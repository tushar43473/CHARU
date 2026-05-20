package com.app.clipsteronline.upload.editor.player

enum class PlaybackStatus {
    IDLE,
    BUFFERING,
    PLAYING,
    PAUSED,
    SEEKING,
    ENDED,
    ERROR,
}

data class PlaybackState(
    val status: PlaybackStatus = PlaybackStatus.IDLE,
    val positionUs: Long = 0L,
    val durationUs: Long = 0L,
    val playbackSpeed: Float = 1f,
    val loopEnabled: Boolean = false,
    val bufferedPositionUs: Long = 0L,
    val errorMessage: String? = null,
)
