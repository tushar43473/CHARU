package com.app.clipsteronline.upload.editor.timeline.gestures

import android.view.MotionEvent
import android.view.VelocityTracker
import com.app.clipsteronline.upload.editor.timeline.engine.TimelinePhysics

class TimelineFlingGesture(
    private val physics: TimelinePhysics = TimelinePhysics(),
    private val minFlingVelocityPxPerSecond: Double = 120.0,
) {
    private var velocityTracker: VelocityTracker? = null

    fun onDown(event: MotionEvent) {
        val tracker = velocityTracker ?: VelocityTracker.obtain().also { velocityTracker = it }
        tracker.clear()
        tracker.addMovement(event)
    }

    fun onMove(event: MotionEvent) {
        velocityTracker?.addMovement(event)
    }

    fun onUp(event: MotionEvent): FlingResult {
        val tracker = velocityTracker ?: return FlingResult.None
        tracker.addMovement(event)
        tracker.computeCurrentVelocity(1000)

        val x = physics.sanitizeVelocity(tracker.xVelocity.toDouble())
        val y = physics.sanitizeVelocity(tracker.yVelocity.toDouble())
        tracker.recycle()
        velocityTracker = null

        return if (kotlin.math.abs(x) < minFlingVelocityPxPerSecond && kotlin.math.abs(y) < minFlingVelocityPxPerSecond) {
            FlingResult.None
        } else {
            FlingResult.Fling(x, y)
        }
    }

    fun cancel() {
        velocityTracker?.recycle()
        velocityTracker = null
    }
}

sealed class FlingResult {
    data object None : FlingResult()
    data class Fling(val velocityX: Double, val velocityY: Double) : FlingResult()
}
