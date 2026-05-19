package com.app.clipsteronline.upload.editor.timeline.engine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

/**
 * Zoom engine for timeline.
 * Handles pinch zoom and smooth animations.
 */
class TimelineZoomEngine(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
) {
    private val _zoom = MutableStateFlow(1f)
    val zoom: StateFlow<Float> = _zoom.asStateFlow()

    private var zoomJob: Job? = null

    companion object {
        const val MIN_ZOOM = 0.1f
        const val MAX_ZOOM = 10f
        const val DEFAULT_ZOOM = 1f

        private const val ZOOM_FRICTION = 0.9f
        private const val ZOOM_MIN_VELOCITY = 0.01f
    }

    /**
     * Set zoom level.
     */
    fun setZoom(zoom: Float, animate: Boolean = false) {
        val clampedZoom = zoom.coerceIn(MIN_ZOOM, MAX_ZOOM)

        if (animate) {
            smoothZoomTo(clampedZoom)
        } else {
            _zoom.value = clampedZoom
            zoomJob?.cancel()
        }
    }

    /**
     * Smooth zoom.
     */
    private fun smoothZoomTo(target: Float) {
        zoomJob?.cancel()
        zoomJob = scope.launch {
            var current = _zoom.value

            while (kotlin.math.abs(current - target) > 0.01f) {
                current += (target - current) * 0.15f
                _zoom.value = current
                kotlinx.coroutines.delay(16)
            }

            _zoom.value = target
        }
    }

    /**
     * Handle pinch zoom.
     */
    fun onPinch(
        scaleFactor: Float,
        focusX: Float,
        viewWidth: Float,
        currentScroll: Float
    ) {
        zoomJob?.cancel()

        val newZoom = (_zoom.value * scaleFactor).coerceIn(MIN_ZOOM, MAX_ZOOM)

        // Adjust scroll to keep focus point centered
        val focusRatio = focusX / viewWidth
        val currentVisible = viewWidth / _zoom.value
        val newVisible = viewWidth / newZoom

        // Calculate new scroll to keep focus point stable
        val focusTime = currentScroll + focusX
        val newScroll = (focusTime / _zoom.value * newZoom) - focusX

        _zoom.value = newZoom
    }

    /**
     * Zoom in.
     */
    fun zoomIn(factor: Float = 1.5f) {
        setZoom(_zoom.value * factor)
    }

    /**
     * Zoom out.
     */
    fun zoomOut(factor: Float = 1.5f) {
        setZoom(_zoom.value / factor)
    }

    /**
     * Reset zoom.
     */
    fun resetZoom() {
        setZoom(DEFAULT_ZOOM)
    }

    /**
     * Zoom to fit content.
     */
    fun zoomToFit(contentWidth: Float, viewWidth: Float) {
        if (contentWidth > 0) {
            val fitZoom = viewWidth / contentWidth
            setZoom(fitZoom.coerceIn(MIN_ZOOM, MAX_ZOOM))
        }
    }

    /**
     * Zoom to show duration.
     */
    fun zoomToDuration(durationMs: Long, viewWidth: Float, pixelsPerSecond: Float = 100f) {
        if (durationMs > 0) {
            val timelineWidth = durationMs * pixelsPerSecond / 1000f
            val zoomLevel = if (timelineWidth > viewWidth) viewWidth / timelineWidth else DEFAULT_ZOOM
            setZoom(zoomLevel.coerceIn(MIN_ZOOM, MAX_ZOOM))
        }
    }

    /**
     * Get zoom level.
     */
    fun getZoom(): Float = _zoom.value

    /**
     * Get zoom level as pixels per second.
     */
    fun getPixelsPerSecond(pixelsPerSecond: Float): Float {
        return _zoom.value * pixelsPerSecond
    }

    /**
     * Check if at minimum.
     */
    fun isAtMin(): Boolean = _zoom.value <= MIN_ZOOM

    /**
     * Check if at maximum.
     */
    fun isAtMax(): Boolean = _zoom.value >= MAX_ZOOM

    /**
     * Get zoom density label.
     */
    fun getZoomLabel(): String {
        return when {
            _zoom.value < 0.25f -> "5s"
            _zoom.value < 0.5f -> "10s"
            _zoom.value < 1f -> "30s"
            _zoom.value < 2f -> "1m"
            _zoom.value < 5f -> "2m"
            else -> "5m"
        }
    }
}