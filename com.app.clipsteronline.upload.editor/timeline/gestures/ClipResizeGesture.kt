package com.app.clipsteronline.upload.editor.timeline.gestures

/**
 * Resize gesture handler for clips.
 * Handles trim start/end with snapping.
 */
class ClipResizeGesture(
    private val listener: ClipResizeListener
) {
    private var isResizing = false
    private var activeClipId: String? = null
    private var resizeEdge: ResizeEdge = ResizeEdge.NONE

    private var startX = 0f
    private var currentX = 0f

    private var originalStartMs = 0L
    private var originalEndMs = 0L
    private var currentDeltaMs = 0L

    private var minDurationMs = 100L // Minimum clip duration

    /**
     * Start resize.
     */
    fun onResizeStart(clipId: String, edge: ResizeEdge, x: Float, startMs: Long, endMs: Long) {
        isResizing = true
        activeClipId = clipId
        resizeEdge = edge

        startX = x
        currentX = x

        originalStartMs = startMs
        originalEndMs = endMs
        currentDeltaMs = 0L

        listener.onResizeStart(clipId, edge)
    }

    /**
     * On resize.
     */
    fun onResize(x: Float, pixelsToMs: (Float) -> Long) {
        if (!isResizing) return

        currentX = x
        val deltaX = currentX - startX
        currentDeltaMs = pixelsToMs(deltaX)

        val (newStartMs, newEndMs) = calculateNewBounds()

        listener.onResize(activeClipId ?: return, newStartMs, newEndMs)
    }

    /**
     * On resize end.
     */
    fun onResizeEnd() {
        if (!isResizing) return

        val (newStartMs, newEndMs) = calculateNewBounds()

        listener.onResizeEnd(activeClipId ?: return, newStartMs, newEndMs)

        isResizing = false
        activeClipId = null
        resizeEdge = ResizeEdge.NONE
    }

    /**
     * Cancel resize.
     */
    fun cancel() {
        if (!isResizing) return

        listener.onResizeCancel(activeClipId ?: return)

        isResizing = false
        activeClipId = null
        resizeEdge = ResizeEdge.NONE
    }

    /**
     * Calculate new bounds.
     */
    private fun calculateNewBounds(): Pair<Long, Long> {
        return when (resizeEdge) {
            ResizeEdge.START -> {
                val newStart = (originalStartMs + currentDeltaMs).coerceAtLeast(0)
                val duration = originalEndMs - originalStartMs
                val minStart = originalEndMs - minDurationMs
                newStart.coerceAtMost(minStart) to originalEndMs
            }
            ResizeEdge.END -> {
                val newEnd = (originalEndMs + currentDeltaMs).coerceAtLeast(originalStartMs + minDurationMs)
                originalStartMs to newEnd
            }
            ResizeEdge.NONE -> originalStartMs to originalEndMs
        }
    }

    /**
     * Check if resizing.
     */
    fun isResizing(): Boolean = isResizing

    /**
     * Get active clip ID.
     */
    fun getActiveClipId(): String? = activeClipId

    /**
     * Set minimum duration.
     */
    fun setMinDuration(durationMs: Long) {
        minDurationMs = durationMs
    }

    /**
     * Clip resize listener.
     */
    interface ClipResizeListener {
        fun onResizeStart(clipId: String, edge: ResizeEdge)
        fun onResize(clipId: String, newStartMs: Long, newEndMs: Long)
        fun onResizeEnd(clipId: String, newStartMs: Long, newEndMs: Long)
        fun onResizeCancel(clipId: String)
    }
}

/**
 * Resize edge.
 */
enum class ResizeEdge {
    NONE,
    START,
    END
}