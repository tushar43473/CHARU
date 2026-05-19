package com.app.clipsteronline.upload.editor.timeline.gestures

/**
 * Zoom gesture handler for timeline.
 * Handles pinch zoom with smooth scaling.
 */
class TimelineZoomGesture(
    private val listener: ZoomListener
) {
    private var currentScale = 1f
    private var lastScale = 1f
    private var focusX = 0f
    private var focusY = 0f

    private var minScale = 0.1f
    private var maxScale = 10f

    private var isZooming = false
    private var scaleVelocity = 0f

    companion object {
        private const val SCALE_SMOOTHING = 0.2f
        private const val MIN_SCALE_VELOCITY = 0.01f
    }

    /**
     * Set zoom bounds.
     */
    fun setZoomBounds(min: Float, max: Float) {
        minScale = min
        maxScale = max
    }

    /**
     * On zoom start.
     */
    fun onZoomStart(focusX: Float, focusY: Float) {
        this.focusX = focusX
        this.focusY = focusY
        isZooming = true
        lastScale = currentScale
    }

    /**
     * On zoom.
     */
    fun onZoom(scaleFactor: Float, focusX: Float, focusY: Float) {
        this.focusX = focusX
        this.focusY = focusY

        // Apply scale factor
        val newScale = (currentScale * scaleFactor).coerceIn(minScale, maxScale)
        scaleVelocity = newScale - currentScale
        currentScale = currentScale + (newScale - currentScale) * SCALE_SMOOTHING

        listener.onZoomChanged(currentScale, focusX, focusY)
    }

    /**
     * On zoom end.
     */
    fun onZoomEnd() {
        isZooming = false

        // Apply any remaining velocity
        if (kotlin.math.abs(scaleVelocity) > MIN_SCALE_VELOCITY) {
            val targetScale = (currentScale + scaleVelocity).coerceIn(minScale, maxScale)
            listener.onZoomTo(targetScale, focusX)
        }

        scaleVelocity = 0f
    }

    /**
     * Get current scale.
     */
    fun getScaleFactor(): Float = currentScale

    /**
     * Set scale directly.
     */
    fun setScale(scale: Float) {
        currentScale = scale.coerceIn(minScale, maxScale)
        listener.onZoomChanged(currentScale, focusX, focusY)
    }

    /**
     * Reset zoom.
     */
    fun reset() {
        currentScale = 1f
        listener.onZoomTo(1f, focusX)
    }

    /**
     * Check if currently zooming.
     */
    fun isZooming(): Boolean = isZooming

    /**
     * Zoom listener.
     */
    interface ZoomListener {
        fun onZoomChanged(scale: Float, focusX: Float, focusY: Float)
        fun onZoomTo(scale: Float, focusX: Float)
    }
}