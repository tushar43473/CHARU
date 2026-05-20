package com.app.clipsteronline.upload.editor.timeline.gestures

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.VelocityTracker
import androidx.core.view.GestureDetectorCompat
import com.app.clipsteronline.upload.editor.timeline.engine.TimelineScrollEngine
import kotlin.math.abs

class TimelineScrollGesture(
    context: Context,
    private val scrollEngine: TimelineScrollEngine,
    private val listener: TimelineGesture.Listener,
) : GestureDetector.SimpleOnGestureListener() {

    val gestureDetector = GestureDetectorCompat(context, this).apply {
        setIsLongpressEnabled(false)
    }

    private var velocityTracker: VelocityTracker? = null

    override fun onDown(e: MotionEvent): Boolean {
        ensureVelocityTracker().apply {
            clear()
            addMovement(e)
        }
        scrollEngine.stop()
        return true
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float,
    ): Boolean {
        velocityTracker?.addMovement(e2)
        val next = scrollEngine.scrollBy(distanceX.toDouble(), distanceY.toDouble())
        listener.onScrollUpdated(next.horizontalPx, next.verticalPx)
        return true
    }

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float,
    ): Boolean {
        if (abs(velocityX) < 40f && abs(velocityY) < 40f) return false
        val next = scrollEngine.fling(velocityX.toDouble(), velocityY.toDouble())
        listener.onScrollUpdated(next.horizontalPx, next.verticalPx)
        return true
    }

    fun onGestureEnd(event: MotionEvent) {
        velocityTracker?.addMovement(event)
        velocityTracker?.computeCurrentVelocity(1000)
        velocityTracker?.recycle()
        velocityTracker = null
    }

    private fun ensureVelocityTracker(): VelocityTracker {
        return velocityTracker ?: VelocityTracker.obtain().also { velocityTracker = it }
    }
}
