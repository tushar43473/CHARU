package com.app.clipsteronline.upload.editor.gestures

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Snap guide engine.
 * Magnetic snapping for playhead/clips/edges.
 */
class SnapGuideEngine(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
) {
    private val snapPoints = mutableListOf<SnapPoint>()
    private val guides = mutableListOf<SnapGuide>()

    private var threshold = 20f // pixels

    /**
     * Add snap point.
     */
    fun addSnapPoint(point: SnapPoint) {
        snapPoints.add(point)
    }

    /**
     * Remove snap point.
     */
    fun removeSnapPoint(id: String) {
        snapPoints.removeAll { it.id == id }
    }

    /**
     * Clear points.
     */
    fun clearPoints() {
        snapPoints.clear()
    }

    /**
     * Snap position.
     */
    fun snapPosition(position: Float): Float {
        var snappedPosition = position

        for (snapPoint in snapPoints) {
            val delta = kotlin.math.abs(position - snapPoint.position)
            if (delta < threshold) {
                snappedPosition = snapPoint.position
                snapPoint.isActive = true
            } else {
                snapPoint.isActive = false
            }
        }

        return snappedPosition
    }

    /**
     * Snap timestamp.
     */
    fun snapTimestamp(timestampMs: Long, pixelsPerMs: Float): Long {
        val position = timestampMs.toFloat() * pixelsPerMs
        val snapped = snapPosition(position)
        return (snapped / pixelsPerMs).toLong()
    }

    /**
     * Add guide.
     */
    fun addGuide(guide: SnapGuide) {
        guides.add(guide)
    }

    /**
     * Remove guide.
     */
    fun removeGuide(id: String) {
        guides.removeAll { it.id == id }
    }

    /**
     * Get guides.
     */
    fun getGuides(): List<SnapGuide> = guides.toList()

    /**
     * Set threshold.
     */
    fun setThreshold(thresholdPx: Float) {
        threshold = thresholdPx.coerceIn(5f, 50f)
    }

    /**
     * Enable/disable snapping.
     */
    fun setEnabled(enabled: Boolean) {
        if (!enabled) {
            for (sp in snapPoints) {
                sp.isActive = false
            }
        }
    }

    /**
     * Snap clip edges.
     */
    fun snapClipEdges(
        startPosition: Float,
        endPosition: Float,
        otherClips: List<ClipBounds>
    ): Pair<Float, Float> {
        var snappedStart = startPosition
        var snappedEnd = endPosition

        // Snap to other clip edges
        for (other in otherClips) {
            val deltaStart = kotlin.math.abs(startPosition - other.startPosition)
            val deltaEnd = kotlin.math.abs(endPosition - other.endPosition)

            if (deltaStart < threshold) {
                snappedStart = other.startPosition
            }
            if (deltaEnd < threshold) {
                snappedEnd = other.endPosition
            }
        }

        return snappedStart to snappedEnd
    }

    /**
     * Snap playhead.
     */
    fun snapPlayhead(positionMs: Long, frameMs: Long): Long {
        if (frameMs <= 0) return positionMs

        // Snap to nearest frame
        val frames = positionMs / frameMs
        val snappedFrames = kotlin.math.round(frames)

        return snappedFrames * frameMs
    }

    /**
     * Register clip bounds for snapping.
     */
    fun registerClip(id: String, startPosition: Float, endPosition: Float) {
        removeSnapPoint(id)

        addSnapPoint(SnapPoint(id + "_start", startPosition, SnapType.EDGE_START))
        addSnapPoint(SnapPoint(id + "_end", endPosition, SnapType.EDGE_END))
    }
}

/**
 * Snap point.
 */
data class SnapPoint(
    val id: String,
    val position: Float,
    val type: SnapType = SnapType.GENERAL,
    var isActive: Boolean = false
)

/**
 * Snap guide (visual).
 */
data class SnapGuide(
    val id: String,
    val position: Float,
    val isVertical: Boolean = true,
    val color: Int = 0xFFFFD700.toInt()
)

/**
 * Snap type.
 */
enum class SnapType {
    GENERAL,
    EDGE_START,
    EDGE_END,
    CENTER,
    PLAYHEAD,
    TRACK
}

/**
 * Clip bounds.
 */
data class ClipBounds(
    val id: String,
    val startPosition: Float,
    val endPosition: Float
)