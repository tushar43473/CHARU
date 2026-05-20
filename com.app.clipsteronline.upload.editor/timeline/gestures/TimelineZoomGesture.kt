package com.app.clipsteronline.upload.editor.timeline.gestures

import android.content.Context
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import com.app.clipsteronline.upload.editor.timeline.engine.TimelineScrollEngine
import com.app.clipsteronline.upload.editor.timeline.engine.TimelineZoomEngine

class TimelineZoomGesture(
    context: Context,
    private val zoomEngine: TimelineZoomEngine,
    private val scrollEngine: TimelineScrollEngine,
    private val listener: TimelineGesture.Listener,
) {
    private val scaleDetector = ScaleGestureDetector(context, ScaleListener())

    fun onTouchEvent(event: MotionEvent): Boolean {
        val consumed = scaleDetector.onTouchEvent(event)
        if (event.actionMasked == MotionEvent.ACTION_CANCEL) {
            listener.onInteractionStateChanged(interacting = false)
        }
        return consumed
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            listener.onInteractionStateChanged(interacting = true)
            scrollEngine.stop()
            return true
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val scrollState = scrollEngine.state.value
            val transform = zoomEngine.applyPinch(
                scaleFactor = detector.scaleFactor.toDouble(),
                pivotPx = detector.focusX.toDouble(),
                currentHorizontalOffsetPx = scrollState.horizontalPx,
            )
            val nextState = scrollEngine.scrollTo(transform.adjustedHorizontalOffsetPx, scrollState.verticalPx)
            listener.onZoomUpdated(transform.pixelsPerSecond, transform.density, nextState.horizontalPx)
            listener.onScrollUpdated(nextState.horizontalPx, nextState.verticalPx)
            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector) {
            listener.onInteractionStateChanged(interacting = false)
        }
    }
}
