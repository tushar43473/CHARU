package com.app.clipsteronline.upload.editor.timeline.sync

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PlaybackPositionTracker {
    private val _state = MutableStateFlow(PlaybackPositionState())
    val state: StateFlow<PlaybackPositionState> = _state.asStateFlow()

    fun update(positionUs: Long, isPlaying: Boolean) {
        _state.value = _state.value.copy(positionUs = positionUs.coerceAtLeast(0L), isPlaying = isPlaying)
    }

    fun setFollowPlayhead(enabled: Boolean) {
        _state.value = _state.value.copy(followPlayhead = enabled)
    }

    fun setViewport(startUs: Long, endUs: Long) {
        _state.value = _state.value.copy(viewportStartUs = startUs.coerceAtLeast(0L), viewportEndUs = endUs.coerceAtLeast(0L))
    }
}

data class PlaybackPositionState(
    val positionUs: Long = 0L,
    val isPlaying: Boolean = false,
    val followPlayhead: Boolean = true,
    val viewportStartUs: Long = 0L,
    val viewportEndUs: Long = 0L,
)
