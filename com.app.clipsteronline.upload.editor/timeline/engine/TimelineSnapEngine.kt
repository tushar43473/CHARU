package com.app.clipsteronline.upload.editor.timeline.engine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.math.abs

/**
 * Snap engine for timeline clips.
 * Handles magnetic snapping and alignment.
 */
class TimelineSnapEngine(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
) {
    private var isSnapEnabled = true
    private var snapThresholdMs = 100L
    private var magneticSnap = true

    private var snapPoints = mutableListOf<Long>()

    companion object {
        private const val DEFAULT_THRESHOLD = 100L
        private const val PLAYHEAD_SNAP_DISTANCE = 500L
    }

    /**
     * Set snap threshold.
     */
    fun setSnapThreshold(thresholdMs: Long) {
        snapThresholdMs = thresholdMs.coerceAtLeast(10L)
    }

    /**
     * Enable/disable snapping.
     */
    fun setSnapEnabled(enabled: Boolean) {
        isSnapEnabled = enabled
    }

    /**
     * Enable magnetic snapping.
     */
    fun setMagneticSnap(enabled: Boolean) {
        magneticSnap = enabled
    }

    /**
     * Add snap point.
     */
    fun addSnapPoint(timeMs: Long) {
        if (!snapPoints.contains(timeMs)) {
            snapPoints.add(timeMs)
            snapPoints.sort()
        }
    }

    /**
     * Add multiple snap points.
     */
    fun addSnapPoints(points: Collection<Long>) {
        points.forEach { addSnapPoint(it) }
    }

    /**
     * Remove snap point.
     */
    fun removeSnapPoint(timeMs: Long) {
        snapPoints.remove(timeMs)
    }

    /**
     * Clear snap points.
     */
    fun clearSnapPoints() {
        snapPoints.clear()
    }

    /**
     * Set snap points.
     */
    fun setSnapPoints(points: List<Long>) {
        snapPoints = points.sorted().toMutableList()
    }

    /**
     * Calculate snap position.
     */
    fun calculateSnap(positionMs: Long, includePlayhead: Boolean = true): Long {
        if (!isSnapEnabled) return positionMs

        val pointsToCheck = mutableListOf<Long>()

        // Add configured snap points
        pointsToCheck.addAll(snapPoints)

        // Include playhead if enabled
        if (includePlayhead) {
            pointsToCheck.add(0L) // Start
        }

        var closestPoint = positionMs
        var closestDistance = snapThresholdMs

        for (point in pointsToCheck) {
            val distance = abs(positionMs - point)
            if (distance < closestDistance) {
                closestDistance = distance
                closestPoint = point
            }
        }

        return closestPoint
    }

    /**
     * Snap clip start to playhead.
     */
    fun snapToPlayhead(clipStartMs: Long, playheadMs: Long): Long {
        if (!isSnapEnabled || !magneticSnap) return clipStartMs

        val distance = abs(clipStartMs - playheadMs)
        return if (distance < PLAYHEAD_SNAP_DISTANCE) playheadMs else clipStartMs
    }

    /**
     * Snap to grid.
     */
    fun snapToGrid(positionMs: Long, gridSizeMs: Long): Long {
        if (!isSnapEnabled) return positionMs

        val remainder = positionMs % gridSizeMs

        return when {
            remainder < snapThresholdMs -> positionMs - remainder
            remainder > gridSizeMs - snapThresholdMs -> positionMs + (gridSizeMs - remainder)
            else -> positionMs
        }
    }

    /**
     * Get nearest snap point.
     */
    fun getNearestSnapPoint(positionMs: Long): Long? {
        if (!isSnapEnabled || snapPoints.isEmpty()) return null

        var closest: Long? = null
        var closestDistance = Long.MAX_VALUE

        for (point in snapPoints) {
            val distance = abs(positionMs - point)
            if (distance < closestDistance && distance <= snapThresholdMs) {
                closestDistance = distance
                closest = point
            }
        }

        return closest
    }

    /**
     * Get all active snap points.
     */
    fun getSnapPoints(): List<Long> = snapPoints.toList()

    /**
     * Check if position is near snap point.
     */
    fun isNearSnapPoint(positionMs: Long): Boolean {
        return getNearestSnapPoint(positionMs) != null
    }

    /**
     * Get snap direction.
     */
    fun getSnapDirection(positionMs: Long, targetMs: Long): SnapDirection {
        return when {
            targetMs < positionMs -> SnapDirection.BACKWARD
            targetMs > positionMs -> SnapDirection.FORWARD
            else -> SnapDirection.NONE
        }
    }

    /**
     * Calculate clip snap points.
     */
    fun calculateClipSnapPoints(
        clipStartMs: Long,
        clipEndMs: Long,
        otherClipStarts: List<Long>,
        otherClipEnds: List<Long>
    ) {
        clearSnapPoints()

        // Add clip edges
        otherClipStarts.forEach { addSnapPoint(it) }
        otherClipEnds.forEach { addSnapPoint(it) }

        // Add current clip edges
        addSnapPoint(clipStartMs)
        addSnapPoint(clipEndMs)
    }

    /**
     * Get snap threshold.
     */
    fun getThreshold(): Long = snapThresholdMs

    /**
     * Is snapping enabled.
     */
    fun isEnabled(): Boolean = isSnapEnabled
}

/**
 * Snap direction.
 */
enum class SnapDirection {
    FORWARD,
    BACKWARD,
    NONE
}