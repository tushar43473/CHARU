package com.app.clipsteronline.upload.editor.core.manager

import android.net.Uri
import com.app.clipsteronline.upload.editor.audio.AudioEngine
import com.app.clipsteronline.upload.editor.core.model.AudioClip
import com.app.clipsteronline.upload.editor.player.PlaybackController
import com.app.clipsteronline.upload.editor.player.PlaybackState
import com.app.clipsteronline.upload.editor.player.PlayerController
import com.app.clipsteronline.upload.editor.player.PreviewSurface
import com.app.clipsteronline.upload.editor.player.VideoPlayer
import kotlinx.coroutines.flow.StateFlow

class PlayerManager {
    private val videoPlayer = VideoPlayer()
    private val previewSurface = PreviewSurface()
    val playbackController = PlaybackController()
    private val playerController = PlayerController(videoPlayer, playbackController, previewSurface)
    val audioEngine = AudioEngine()

    val playbackState: StateFlow<PlaybackState> = playbackController.state

    fun load(uri: Uri) = playerController.load(uri)
    fun loadAudioTrack(trackId: String, clip: AudioClip) = audioEngine.loadTrack(trackId, clip)
    fun play() {
        playerController.play()
        audioEngine.play()
    }

    fun pause() {
        playerController.pause()
        audioEngine.pause()
    }

    fun seekTo(positionUs: Long) {
        playerController.seekTo(positionUs)
        audioEngine.seekTo(positionUs / 1000L)
    }

    fun setPlaybackSpeed(speed: Float) = playerController.setSpeed(speed)
    fun setLoopEnabled(loopEnabled: Boolean) = playerController.setLoopEnabled(loopEnabled)
    fun setMasterVolume(volume: Float) = audioEngine.setMasterVolume(volume)

    fun onFrame(deltaUs: Long) {
        playerController.onFrame(deltaUs)
        val nowUs = playbackController.state.value.positionUs
        audioEngine.renderAt(nowUs / 1000L)
    }

    fun release() = playerController.release()
}
