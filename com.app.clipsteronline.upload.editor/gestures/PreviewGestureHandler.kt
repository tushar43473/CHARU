package com.app.clipsteronline.upload.editor.gestures

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.core.view.GestureDetectorCompat

class PreviewGestureHandler(
    context: Context,
    private val multiTouchHandler: MultiTouchHandler,
    private val listener: Listener,
) {
    private val scaleDetector = ScaleGestureDetector(context, ScaleListener())
    private val tapDetector = GestureDetectorCompat(context, TapListener())

    fun onTouchEvent(event: MotionEvent): Boolean {
        val mt = multiTouchHandler.onTouchEvent(event)
        val scaled = scaleDetector.onTouchEvent(event)
        val tapped = tapDetector.onTouchEvent(event)
        if (mt.active && kotlin.math.abs(mt.rotationDeltaDegrees) > 0.5f) {
            listener.onRotate(mt.rotationDeltaDegrees)
        }
        return scaled || tapped || mt.active
    }

    interface Listener {
        fun onScale(scaleFactor: Float)
        fun onPan(dx: Float, dy: Float)
        fun onRotate(deltaDegrees: Float)
        fun onDoubleTapReset()
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            listener.onScale(detector.scaleFactor.coerceIn(0.5f, 4f))
            return true
        }
    }

    private inner class TapListener : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            listener.onPan(-distanceX, -distanceY)
            return true
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            listener.onDoubleTapReset()
            return true
        }
    }
}
