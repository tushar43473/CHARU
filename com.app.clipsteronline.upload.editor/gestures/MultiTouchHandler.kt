package com.app.clipsteronline.upload.editor.gestures

import android.view.MotionEvent
import com.app.clipsteronline.upload.editor.core.utils.GestureUtils

class MultiTouchHandler {
    private var firstPointerId = INVALID_POINTER
    private var secondPointerId = INVALID_POINTER
    private var initialDistance = 0f
    private var initialAngle = 0f

    fun onTouchEvent(event: MotionEvent): MultiTouchSnapshot {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> firstPointerId = event.getPointerId(0)
            MotionEvent.ACTION_POINTER_DOWN -> if (event.pointerCount >= 2) {
                firstPointerId = event.getPointerId(0)
                secondPointerId = event.getPointerId(1)
                initialDistance = distance(event)
                initialAngle = angle(event)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> reset()
        }
        if (firstPointerId == INVALID_POINTER || secondPointerId == INVALID_POINTER || event.pointerCount < 2) {
            return MultiTouchSnapshot()
        }

        val dist = distance(event)
        val ang = angle(event)
        val scale = if (initialDistance > 0f) GestureUtils.safeScaleFactor(initialDistance, dist) else 1f
        val rotationDelta = GestureUtils.normalizeDegrees(ang - initialAngle)
        return MultiTouchSnapshot(true, scale, rotationDelta)
    }

    fun reset() {
        firstPointerId = INVALID_POINTER
        secondPointerId = INVALID_POINTER
        initialDistance = 0f
        initialAngle = 0f
    }

    private fun distance(event: MotionEvent): Float {
        return GestureUtils.distance(event.getX(0), event.getY(0), event.getX(1), event.getY(1))
    }

    private fun angle(event: MotionEvent): Float {
        return GestureUtils.rotationDegrees(event.getX(0), event.getY(0), event.getX(1), event.getY(1))
    }

    companion object { private const val INVALID_POINTER = -1 }
}

data class MultiTouchSnapshot(
    val active: Boolean = false,
    val scaleFactor: Float = 1f,
    val rotationDeltaDegrees: Float = 0f,
)
