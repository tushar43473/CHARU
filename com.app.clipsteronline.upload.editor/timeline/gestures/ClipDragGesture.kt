package com.app.clipsteronline.upload.editor.timeline.gestures

/**
 * Drag gesture handler for clips.
 * Handles moving clips between tracks.
 */
class ClipDragGesture(
    private val listener: ClipDragListener
) {
    private var isDragging = false
    private var activeClipId: String? = null
    private var activeTrackId: String? = null

    private var startX = 0f
    private var startY = 0f
    private var currentX = 0f
    private var currentY = 0f

    private var clipStartMs = 0L
    private var trackIndex = 0

    /**
     * Start drag.
     */
    fun onDragStart(clipId: String, trackId: String, x: Float, y: Float, startTimeMs: Long) {
        isDragging = true
        activeClipId = clipId
        activeTrackId = trackId

        startX = x
        startY = y
        currentX = x
        currentY = y

        clipStartMs = startTimeMs

        listener.onClipDragStart(clipId, trackId)
    }

    /**
     * On drag.
     */
    fun onDrag(x: Float, y: Float, timeDeltaMs: Long) {
        if (!isDragging) return

        currentX = x
        currentY = y

        val timeDelta = timeDeltaMs

        listener.onClipDrag(activeClipId ?: return, timeDelta, getTrackDelta(y))
    }

    /**
     * On drag end.
     */
    fun onDragEnd() {
        if (!isDragging) return

        val clipId = activeClipId ?: return
        val trackId = activeTrackId

        listener.onClipDragEnd(clipId, trackId, clipStartMs, getTrackDelta(currentY))

        isDragging = false
        activeClipId = null
        activeTrackId = null
    }

    /**
     * Cancel drag.
     */
    fun cancel() {
        isDragging = false
        activeClipId = null
        activeTrackId = null
    }

    /**
     * Get track delta.
     */
    private fun getTrackDelta(y: Float): Int {
        val trackHeight = 80f // Default track height
        val delta = ((y - startY) / trackHeight).toInt()
        return delta
    }

    /**
     * Check if dragging.
     */
    fun isDragging(): Boolean = isDragging

    /**
     * Get active clip ID.
     */
    fun getActiveClipId(): String? = activeClipId

    /**
     * Clip drag listener.
     */
    interface ClipDragListener {
        fun onClipDragStart(clipId: String, trackId: String)
        fun onClipDrag(clipId: String, timeDeltaMs: Long, trackDelta: Int)
        fun onClipDragEnd(clipId: String, trackId: String?, startTimeMs: Long, trackDelta: Int)
    }
}