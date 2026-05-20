package com.app.clipsteronline.upload.editor.gestures

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.core.view.GestureDetectorCompat

class TimelineGestureHandler(
    context: Context,
    private val listener: Listener,
) : GestureDetector.SimpleOnGestureListener() {

    private val gestureDetector = GestureDetectorCompat(context, this)
    private val scaleDetector = ScaleGestureDetector(context, ScaleListener())

    fun onTouchEvent(event: MotionEvent): Boolean {
        val scaled = scaleDetector.onTouchEvent(event)
        val scrolled = gestureDetector.onTouchEvent(event)
        if (event.actionMasked == MotionEvent.ACTION_UP) listener.onInteractionEnd()
        return scaled || scrolled
    }

    override fun onDown(e: MotionEvent): Boolean = true

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
        listener.onTimelineScroll(distanceX, distanceY)
        return true
    }

    override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        listener.onTimelineFling(velocityX, velocityY)
        return true
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            listener.onTimelineScale(detector.scaleFactor, detector.focusX)
            return true
        }
    }

    interface Listener {
        fun onTimelineScroll(distanceX: Float, distanceY: Float)
        fun onTimelineScale(scaleFactor: Float, pivotX: Float)
        fun onTimelineFling(velocityX: Float, velocityY: Float)
        fun onInteractionEnd()
    }
}
