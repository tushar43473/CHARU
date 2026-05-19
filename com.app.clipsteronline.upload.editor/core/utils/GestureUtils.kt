package com.app.clipsteronline.upload.editor.core.utils

import android.view.MotionEvent
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Gesture utilities for touch interaction calculations.
 * Provides velocity, zoom, fling, and smoothing calculations.
 */
object GestureUtils {

    private const val VELOCITY_HISTORY_SIZE = 10
    private const val DEFAULT_FLING_THRESHOLD = 50f
    private const val DEFAULT_DOUBLE_TAP_SLOP = 100f

    /**
     * Calculate distance between two touch points.
     */
    fun getDistance(event: MotionEvent): Float {
        if (event.pointerCount < 2) return 0f

        val x0 = event.getX(0)
        val y0 = event.getY(0)
        val x1 = event.getX(1)
        val y1 = event.getY(1)

        return sqrt((x1 - x0).pow(2) + (y1 - y0).pow(2))
    }

    /**
     * Calculate angle between two touch points.
     */
    fun getAngle(event: MotionEvent): Float {
        if (event.pointerCount < 2) return 0f

        val x0 = event.getX(0)
        val y0 = event.getY(0)
        val x1 = event.getX(1)
        val y1 = event.getY(1)

        return Math.toDegrees(atan2((y1 - y0).toDouble(), (x1 - x0).toDouble())).toFloat()
    }

    /**
     * Calculate midpoint between two touch points.
     */
    fun getMidpoint(event: MotionEvent): Pair<Float, Float> {
        if (event.pointerCount < 2) {
            return event.x to event.y
        }

        val x = (event.getX(0) + event.getX(1)) / 2
        val y = (event.getY(0) + event.getY(1)) / 2

        return x to y
    }

    /**
     * Calculate velocity from motion events.
     */
    fun calculateVelocity(events: List<MotionEvent>): Float {
        if (events.size < 2) return 0f

        val first = events.first()
        val last = events.last()

        val dx = last.x - first.x
        val dy = last.y - first.y
        val dt = (last.eventTime - first.eventTime).toFloat()

        if (dt <= 0) return 0f

        // Convert to pixels per second
        return sqrt(dx * dx + dy * dy) / dt * 1000f
    }

    /**
     * Calculate x velocity.
     */
    fun calculateVelocityX(events: List<MotionEvent>): Float {
        if (events.size < 2) return 0f

        val first = events.first()
        val last = events.last()

        val dx = last.x - first.x
        val dt = (last.eventTime - first.eventTime).toFloat()

        return if (dt > 0) dx / dt * 1000f else 0f
    }

    /**
     * Calculate y velocity.
     */
    fun calculateVelocityY(events: List<MotionEvent>): Float {
        if (events.size < 2) return 0f

        val first = events.first()
        val last = events.last()

        val dy = last.y - first.y
        val dt = (last.eventTime - first.eventTime).toFloat()

        return if (dt > 0) dy / dt * 1000f else 0f
    }

    /**
     * Get velocity from velocity tracker.
     */
    fun getVelocity(tracker: android.view.VelocityTracker, pointerId: Int): Pair<Float, Float> {
        val xVelocity = tracker.getXVelocity(pointerId)
        val yVelocity = tracker.getYVelocity(pointerId)
        return xVelocity to yVelocity
    }

    /**
     * Calculate pinch scale factor.
     */
    fun getScaleFactor(oldDistance: Float, newDistance: Float): Float {
        return if (oldDistance > 0) newDistance / oldDistance else 1f
    }

    /**
     * Determine if gesture is a fling.
     */
    fun isFling(velocityX: Float, velocityY: Float, minVelocity: Float = DEFAULT_FLING_THRESHOLD): Boolean {
        return abs(velocityX) > minVelocity || abs(velocityY) > minVelocity
    }

    /**
     * Calculate fling distance with friction.
     */
    fun calculateFlingDistance(velocity: Float, friction: Float = 0.15f): Float {
        if (velocity == 0f) return 0f
        return velocity - velocity * friction
    }

    /**
     * Get focus point for pinch zoom.
     */
    fun getPinchFocus(event: MotionEvent): Pair<Float, Float> {
        return getMidpoint(event)
    }

    /**
     * Check if double tap occurred.
     */
    fun isDoubleTap(
        downEvent: MotionEvent?,
        upEvent: MotionEvent?,
        doubleTapEvent: MotionEvent?,
        doubleTapSlop: Float = DEFAULT_DOUBLE_TAP_SLOP
    ): Boolean {
        if (downEvent == null || upEvent == null || doubleTapEvent == null) return false

        val interval = upEvent.eventTime - downEvent.eventTime
        if (interval > android.view.ViewConfiguration.getDoubleTapTimeout()) return false

        val dx = doubleTapEvent.x - downEvent.x
        val dy = doubleTapEvent.y - downEvent.y

        return dx * dx + dy * dy < doubleTapSlop * doubleTapSlop
    }

    /**
     * Smooth scroll value using exponential moving average.
     */
    fun smoothScroll(currentValue: Float, targetValue: Float, smoothing: Float = 0.2f): Float {
        return currentValue + (targetValue - currentValue) * smoothing
    }

    /**
     * Smooth velocity with averaging.
     */
    fun smoothVelocity(velocityHistory: List<Float>, smoothing: Float = 0.5f): Float {
        if (velocityHistory.isEmpty()) return 0f
        if (velocityHistory.size == 1) return velocityHistory.last()

        var smoothed = velocityHistory.last()
        for (i in velocityHistory.size - 2 downTo 0) {
            smoothed = velocityHistory[i] * smoothing + smoothed * (1 - smoothing)
        }

        return smoothed
    }

    /**
     * Calculate deceleration for fling.
     */
    fun calculateDeceleration(velocity: Float, friction: Float = 0.015f): Float {
        return velocity * friction
    }

    /**
     * Get stop time for fling.
     */
    fun getStopTime(velocity: Float, deceleration: Float): Float {
        return if (deceleration > 0 && velocity > 0) velocity / deceleration else 0f
    }

    /**
     * Calculate scroll offset with bounds.
     */
    fun clampScroll(scroll: Float, min: Float, max: Float): Float {
        return scroll.coerceIn(min, max)
    }

    /**
     * Apply overscroll effect.
     */
    fun applyOverscroll(scroll: Float, overscroll: Float, rubberbandFactor: Float = 0.5f): Float {
        if (overscroll == 0f) return scroll
        return scroll + overscroll * rubberbandFactor / (abs(overscroll) + 1)
    }

    /**
     * Calculate rubberband offset.
     */
    fun rubberbandOffset(offset: Float, maxOffset: Float, rubberbandFactor: Float = 0.55f): Float {
        val clamped = offset.coerceIn(-maxOffset, maxOffset)
        return clamped * rubberbandFactor * (1 - (abs(clamped) / maxOffset).coerceIn(0f, 1f))
    }

    /**
     * Clamp zoom level.
     */
    fun clampZoom(zoom: Float, minZoom: Float = 0.1f, maxZoom: Float = 10f): Float {
        return zoom.coerceIn(minZoom, maxZoom)
    }

    /**
     * Calculate zoom bounds for scroll.
     */
    fun getZoomBounds(
        scrollSize: Float,
        contentSize: Float,
        zoom: Float
    ): Pair<Float, Float> {
        val scaledContentSize = contentSize * zoom
        val maxScroll = (scaledContentSize - scrollSize).coerceAtLeast(0f)
        return 0f to maxScroll
    }

    /**
     * Check if scroll is at edge.
     */
    fun isAtEdge(scroll: Float, minScroll: Float, maxScroll: Float, edgeThreshold: Float = 0f): Boolean {
        return scroll <= minScroll + edgeThreshold || scroll >= maxScroll - edgeThreshold
    }

    /**
     * Calculate snap point for scrolling.
     */
    fun getSnapPoint(currentScroll: Float, snapPoints: List<Float>): Float {
        if (snapPoints.isEmpty()) return currentScroll

        var closest = snapPoints.first()
        var closestDistance = abs(currentScroll - closest)

        for (point in snapPoints) {
            val distance = abs(currentScroll - point)
            if (distance < closestDistance) {
                closestDistance = distance
                closest = point
            }
        }

        return closest
    }

    /**
     * Apply inertia to scroll with damping.
     */
    fun applyInertia(
        scroll: Float,
        velocity: Float,
        dt: Float,
        friction: Float = 0.95f
    ): Pair<Float, Float> {
        val newVelocity = velocity * friction
        val newScroll = scroll + newVelocity * dt

        return newScroll to newVelocity
    }

    /**
     * Interpolate between scroll positions.
     */
    fun interpolateScroll(start: Float, end: Float, progress: Float): Float {
        return start + (end - start) * progress.coerceIn(0f, 1f)
    }

    /**
     * Calculate time delta between events.
     */
    fun getTimeDelta(event: MotionEvent?, previousEvent: MotionEvent?): Float {
        return if (event != null && previousEvent != null) {
            (event.eventTime - previousEvent.eventTime).toFloat()
        } else 0f
    }
}