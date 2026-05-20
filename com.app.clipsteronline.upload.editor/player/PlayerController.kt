package com.app.clipsteronline.upload.editor.player

import android.net.Uri

class PlayerController(
    private val videoPlayer: VideoPlayer,
    private val playbackController: PlaybackController,
    private val previewSurface: PreviewSurface,
) {
    fun load(uri: Uri) {
        videoPlayer.prepare(uri)
        playbackController.setDuration(0L)
    }

    fun play() = playbackController.play()
    fun pause() = playbackController.pause()
    fun seekTo(positionUs: Long) = playbackController.seekTo(positionUs)
    fun setSpeed(speed: Float) = playbackController.setPlaybackSpeed(speed)
    fun setLoopEnabled(enabled: Boolean) = playbackController.setLoopEnabled(enabled)

    fun onFrame(deltaUs: Long) {
        playbackController.tick(deltaUs)
    }

    fun release() {
        pause()
        videoPlayer.clearSurface()
        previewSurface.unbind()
        videoPlayer.release()
    }
}
