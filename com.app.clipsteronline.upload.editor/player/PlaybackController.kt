package com.app.clipsteronline.upload.editor.player

import kotlin.math.max
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class PlaybackController {
    private val _state = MutableStateFlow(PlaybackState())
    val state: StateFlow<PlaybackState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<PlaybackEvent>(extraBufferCapacity = 32)
    val events: SharedFlow<PlaybackEvent> = _events.asSharedFlow()

    fun play() {
        val s = _state.value
        _state.value = s.copy(status = PlaybackStatus.PLAYING, errorMessage = null)
        _events.tryEmit(PlaybackEvent.Play)
    }

    fun pause() {
        _state.value = _state.value.copy(status = PlaybackStatus.PAUSED)
        _events.tryEmit(PlaybackEvent.Pause)
    }

    fun seekTo(positionUs: Long) {
        val s = _state.value
        val safe = positionUs.coerceIn(0L, max(s.durationUs, 0L))
        _state.value = s.copy(status = PlaybackStatus.SEEKING, positionUs = safe)
        _events.tryEmit(PlaybackEvent.Seek(safe))
        _state.value = _state.value.copy(status = if (s.status == PlaybackStatus.PLAYING) PlaybackStatus.PLAYING else PlaybackStatus.PAUSED)
    }

    fun setDuration(durationUs: Long) {
        val safeDuration = durationUs.coerceAtLeast(0L)
        val s = _state.value
        _state.value = s.copy(durationUs = safeDuration, positionUs = s.positionUs.coerceAtMost(safeDuration))
    }

    fun setPlaybackSpeed(speed: Float) {
        _state.value = _state.value.copy(playbackSpeed = speed.coerceIn(0.25f, 4f))
    }

    fun setLoopEnabled(loop: Boolean) {
        _state.value = _state.value.copy(loopEnabled = loop)
    }

    fun setBufferedPosition(bufferedUs: Long) {
        _state.value = _state.value.copy(bufferedPositionUs = bufferedUs.coerceAtLeast(0L))
    }

    fun tick(deltaUs: Long) {
        val s = _state.value
        if (s.status != PlaybackStatus.PLAYING || deltaUs <= 0L) return
        val next = s.positionUs + (deltaUs * s.playbackSpeed).toLong()
        when {
            s.durationUs <= 0L -> _state.value = s.copy(positionUs = next.coerceAtLeast(0L))
            next < s.durationUs -> _state.value = s.copy(positionUs = next)
            s.loopEnabled -> {
                val looped = next % s.durationUs
                _state.value = s.copy(positionUs = looped)
                _events.tryEmit(PlaybackEvent.Loop)
            }
            else -> {
                _state.value = s.copy(positionUs = s.durationUs, status = PlaybackStatus.ENDED)
                _events.tryEmit(PlaybackEvent.Complete)
            }
        }
    }

    fun setError(message: String) {
        _state.value = _state.value.copy(status = PlaybackStatus.ERROR, errorMessage = message)
        _events.tryEmit(PlaybackEvent.Error(message))
    }
}

sealed class PlaybackEvent {
    data object Play : PlaybackEvent()
    data object Pause : PlaybackEvent()
    data class Seek(val positionUs: Long) : PlaybackEvent()
    data object Loop : PlaybackEvent()
    data object Complete : PlaybackEvent()
    data class Error(val message: String) : PlaybackEvent()
}
