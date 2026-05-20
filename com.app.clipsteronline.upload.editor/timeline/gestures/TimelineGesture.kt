package com.app.clipsteronline.upload.editor.timeline.gestures

import android.content.Context
import android.view.MotionEvent
import androidx.core.view.GestureDetectorCompat
import com.app.clipsteronline.upload.editor.timeline.engine.TimelineScrollEngine
import com.app.clipsteronline.upload.editor.timeline.engine.TimelineZoomEngine

class TimelineGesture(
    context: Context,
    private val scrollEngine: TimelineScrollEngine,
    private val zoomEngine: TimelineZoomEngine,
    private val listener: Listener,
) {
    private val scrollGesture = TimelineScrollGesture(context, scrollEngine, listener)
    private val zoomGesture = TimelineZoomGesture(context, zoomEngine, scrollEngine, listener)

    private val gestureDetector: GestureDetectorCompat = scrollGesture.gestureDetector

    fun onTouchEvent(event: MotionEvent): Boolean {
        val consumedByZoom = zoomGesture.onTouchEvent(event)
        val consumedByScroll = gestureDetector.onTouchEvent(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                scrollGesture.onGestureEnd(event)
                listener.onInteractionStateChanged(interacting = false)
            }
            MotionEvent.ACTION_DOWN -> listener.onInteractionStateChanged(interacting = true)
        }

        return consumedByZoom || consumedByScroll
    }

    interface Listener {
        fun onScrollUpdated(horizontalPx: Double, verticalPx: Double)
        fun onZoomUpdated(pixelsPerSecond: Double, density: Double, horizontalOffsetPx: Double)
        fun onInteractionStateChanged(interacting: Boolean)
    }
}
