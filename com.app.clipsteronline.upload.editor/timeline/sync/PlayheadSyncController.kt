package com.app.clipsteronline.upload.editor.timeline.sync

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Controller for playhead synchronization.
 * Handles fixed center playhead behavior.
 */
class PlayheadSyncController(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
) {
    private val _playheadState = MutableStateFlow(PlayheadState())
    val playheadState: StateFlow<PlayheadState> = _playheadState.asStateFlow()

    private var positionMs = 0L
    private var scrollOffset = 0f
    private var viewWidth = 0f
    private var centerX = 0f

    /**
     * Update position.
     */
    fun updatePosition(positionMs: Long) {
        this.positionMs = positionMs

        _playheadState.value = _playheadState.value.copy(
            positionMs = positionMs,
            xPosition = calculateX(positionMs)
        )
    }

    /**
     * Set timeline dimensions.
     */
    fun setDimensions(viewWidth: Float, zoom: Float, scrollX: Float) {
        this.viewWidth = viewWidth
        this.centerX = viewWidth / 2
        this.scrollOffset = scrollX

        _playheadState.value = _playheadState.value.copy(
            centerX = centerX,
            viewWidth = viewWidth,
            zoom = zoom,
            scrollOffset = scrollX
        )
    }

    /**
     * Get playhead X position.
     */
    private fun calculateX(positionMs: Long): Float {
        val zoom = _playheadState.value.zoom
        val timeToX = positionMs * zoom * 100 / 1000f
        return timeToX - scrollOffset
    }

    /**
     * Update scroll offset.
     */
    fun updateScroll(scrollX: Float) {
        this.scrollOffset = scrollX
        _playheadState.value = _playheadState.value.copy(
            scrollOffset = scrollX,
            xPosition = calculateX(positionMs)
        )
    }

    /**
     * Get position.
     */
    fun getPosition(): Long = positionMs

    /**
     * Force update.
     */
    fun forceUpdate() {
        _playheadState.value = _playheadState.value.copy(
            xPosition = calculateX(positionMs)
        )
    }
}

/**
 * Playhead state.
 */
data class PlayheadState(
    val positionMs: Long = 0L,
    val xPosition: Float = 0f,
    val centerX: Float = 0f,
    val viewWidth: Float = 0f,
    val zoom: Float = 1f,
    val scrollOffset: Float = 0f
)