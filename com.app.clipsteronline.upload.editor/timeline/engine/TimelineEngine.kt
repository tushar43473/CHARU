package com.app.clipsteronline.upload.editor.timeline.engine

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TimelineEngine(
    private val calculator: TimelineCalculator = TimelineCalculator(),
) {
    private val _state = MutableStateFlow(TimelineState())
    val state: StateFlow<TimelineState> = _state.asStateFlow()

    private val tracks = linkedMapOf<String, MutableList<ClipLayout>>()

    fun setDuration(durationMs: Long) {
        val current = _state.value
        _state.value = current.copy(
            durationMs = durationMs.coerceAtLeast(0L),
            playheadMs = calculator.clampPlayhead(current.playheadMs, durationMs),
        )
    }

    fun setViewport(viewportWidthPx: Int) {
        _state.value = _state.value.copy(viewportWidthPx = viewportWidthPx.coerceAtLeast(0))
    }

    fun seekTo(timeMs: Long) {
        val s = _state.value
        _state.value = s.copy(playheadMs = calculator.clampPlayhead(timeMs, s.durationMs))
    }

    fun setZoom(zoom: Float) {
        val normalized = zoom.coerceIn(TimelineState.MIN_ZOOM, TimelineState.MAX_ZOOM)
        _state.value = _state.value.copy(zoom = normalized)
    }

    fun scrollTo(scrollPx: Float) {
        _state.value = _state.value.copy(scrollPx = scrollPx.coerceAtLeast(0f))
    }

    fun putTrack(trackId: String, clips: List<ClipLayout>) {
        require(trackId.isNotBlank())
        tracks[trackId] = clips.sortedBy { it.startMs }.toMutableList()
    }

    fun visibleClips(): List<ClipLayout> {
        val s = _state.value
        val visible = calculator.visibleRange(s.scrollPx, s.viewportWidthPx, s.zoom)
        return tracks.values.flatten().filter { calculator.isOverlapping(it.startMs, it.endMs, visible.first, visible.last) }
    }

    data class ClipLayout(
        val clipId: String,
        val trackId: String,
        val startMs: Long,
        val endMs: Long,
        val layer: Int = 0,
    ) {
        init {
            require(clipId.isNotBlank())
            require(trackId.isNotBlank())
            require(startMs >= 0 && endMs >= startMs)
        }
    }
}
