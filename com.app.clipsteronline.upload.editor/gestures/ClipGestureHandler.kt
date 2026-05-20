package com.app.clipsteronline.upload.editor.gestures

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.core.view.GestureDetectorCompat

class ClipGestureHandler(
    context: Context,
    private val listener: Listener,
) : GestureDetector.SimpleOnGestureListener() {

    private val detector = GestureDetectorCompat(context, this).apply { setIsLongpressEnabled(true) }

    fun onTouchEvent(event: MotionEvent): Boolean = detector.onTouchEvent(event)

    override fun onDown(e: MotionEvent): Boolean = true

    override fun onLongPress(e: MotionEvent) {
        listener.onLongPressSelect(e.x, e.y)
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        listener.onSelect(e.x, e.y)
        return true
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
        listener.onDrag(-distanceX, -distanceY)
        return true
    }

    interface Listener {
        fun onSelect(x: Float, y: Float)
        fun onLongPressSelect(x: Float, y: Float)
        fun onDrag(dx: Float, dy: Float)
    }
}
