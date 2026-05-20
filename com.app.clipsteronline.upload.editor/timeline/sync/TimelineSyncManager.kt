package com.app.clipsteronline.upload.editor.timeline.sync

import com.app.clipsteronline.upload.editor.audio.AudioEngine
import com.app.clipsteronline.upload.editor.player.PlaybackController
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class TimelineSyncManager(
    private val playbackController: PlaybackController,
    private val tracker: PlaybackPositionTracker,
    private val frameSync: TimelineFrameSync,
    private val playheadSyncController: PlayheadSyncController,
    private val audioEngine: AudioEngine? = null,
) {
    private val _events = MutableSharedFlow<PlaybackEvent>(extraBufferCapacity = 32)
    val events: SharedFlow<PlaybackEvent> = _events.asSharedFlow()

    private val _playheadPx = MutableStateFlow(0.0)
    val playheadPx: StateFlow<Double> = _playheadPx.asStateFlow()

    fun play() {
        playbackController.play()
        audioEngine?.play()
        _events.tryEmit(PlaybackEvent.Play)
        frameSync.start()
    }

    fun pause() {
        playbackController.pause()
        audioEngine?.pause()
        _events.tryEmit(PlaybackEvent.Pause)
    }

    fun seekTo(positionUs: Long, viewportWidthPx: Double, autoCenter: Boolean = true, beatSnap: Boolean = false) {
        val targetUs = if (beatSnap) snapToBeatUs(positionUs) else positionUs
        playbackController.seekTo(targetUs)
        audioEngine?.seekTo(targetUs / 1000L)
        updateSync(viewportWidthPx, autoCenter)
        _events.tryEmit(PlaybackEvent.Seek(targetUs))
    }

    fun onFrame(viewportWidthPx: Double, autoCenter: Boolean = true) {
        updateSync(viewportWidthPx, autoCenter)
        val state = playbackController.state.value
        audioEngine?.renderAt(state.positionUs / 1000L)
        if (!state.isPlaying) frameSync.stop()
    }

    fun snapToBeatUs(positionUs: Long): Long {
        val beatMs = audioEngine?.nearestBeat(positionUs / 1000L) ?: return positionUs
        return beatMs * 1000L
    }

    private fun updateSync(viewportWidthPx: Double, autoCenter: Boolean) {
        val state = playbackController.state.value
        tracker.update(state.positionUs, state.isPlaying)
        _playheadPx.value = playheadSyncController.syncPlayheadToTimeline(state.positionUs, viewportWidthPx, autoCenter)
        _events.tryEmit(PlaybackEvent.Position(state.positionUs))
        if (!state.isPlaying && state.durationUs > 0 && state.positionUs >= state.durationUs) {
            _events.tryEmit(PlaybackEvent.Complete)
        }
    }
}

sealed class PlaybackEvent {
    data object Play : PlaybackEvent()
    data object Pause : PlaybackEvent()
    data class Seek(val positionUs: Long) : PlaybackEvent()
    data class Position(val positionUs: Long) : PlaybackEvent()
    data object Complete : PlaybackEvent()
}
