package com.app.clipsteronline.upload.editor.timeline.gestures

import android.view.MotionEvent
import kotlin.math.abs

/**
 * Central gesture coordinator for timeline.
 * Manages touch states and dispatches to appropriate handlers.
 */
class TimelineGesture(
    private val listener: TimelineGestureListener
) {
    private var activePointerId = INVALID_POINTER_ID
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var touchStartX = 0f
    private var touchStartY = 0f
    private var touchSlopExceeded = false

    private var currentMode = GestureMode.NONE

    private var scrollGesture: TimelineScrollGesture? = null
    private var zoomGesture: TimelineZoomGesture? = null
    private var flingGesture: TimelineFlingGesture? = null

    companion object {
        private const val INVALID_POINTER_ID = -1
        private const val TOUCH_SLOP = 24f
    }

    /**
     * Initialize scroll gesture handler.
     */
    fun setScrollGesture(gesture: TimelineScrollGesture) {
        this.scrollGesture = gesture
    }

    /**
     * Initialize zoom gesture handler.
     */
    fun setZoomGesture(gesture: TimelineZoomGesture) {
        this.zoomGesture = gesture
    }

    /**
     * Initialize fling gesture handler.
     */
    fun setFlingGesture(gesture: TimelineFlingGesture) {
        this.flingGesture = gesture
    }

    /**
     * Handle touch event.
     */
    fun onTouch(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> return handleActionDown(event)
            MotionEvent.ACTION_POINTER_DOWN -> return handleActionPointerDown(event)
            MotionEvent.ACTION_MOVE -> return handleActionMove(event)
            MotionEvent.ACTION_POINTER_UP -> return handleActionPointerUp(event)
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> return handleActionUp(event)
        }
        return false
    }

    /**
     * Handle action down.
     */
    private fun handleActionDown(event: MotionEvent): Boolean {
        activePointerId = event.getPointerId(0)
        lastTouchX = event.x
        lastTouchY = event.y
        touchStartX = event.x
        touchStartY = event.y
        touchSlopExceeded = false
        currentMode = GestureMode.NONE

        flingGesture?.cancel()
        return true
    }

    /**
     * Handle pointer down.
     */
    private fun handleActionPointerDown(event: MotionEvent): Boolean {
        if (event.pointerCount == 2 && currentMode == GestureMode.NONE) {
            currentMode = GestureMode.ZOOM
            activePointerId = event.getPointerId(event.actionIndex)
            val focusX = getFocusX(event)
            val focusY = getFocusY(event)
            zoomGesture?.onZoomStart(focusX, focusY)
        }
        return true
    }

    /**
     * Handle move.
     */
    private fun handleActionMove(event: MotionEvent): Boolean {
        if (currentMode == GestureMode.NONE && !touchSlopExceeded) {
            val dx = abs(event.x - touchStartX)
            val dy = abs(event.y - touchStartY)
            if (dx > TOUCH_SLOP || dy > TOUCH_SLOP) {
                touchSlopExceeded = true

                // Determine if horizontal or vertical
                if (dx > dy) {
                    currentMode = GestureMode.SCROLL
                } else {
                    currentMode = GestureMode.IGNORE
                }
            }
        }

        when (currentMode) {
            GestureMode.SCROLL -> {
                val dx = event.x - lastTouchX
                scrollGesture?.onScroll(dx)
            }
            GestureMode.ZOOM -> {
                if (event.pointerCount >= 2) {
                    val scale = getCurrentScale(event)
                    val focusX = getFocusX(event)
                    val focusY = getFocusY(event)
                    zoomGesture?.onZoom(scale, focusX, focusY)
                }
            }
            GestureMode.NONE, GestureMode.IGNORE -> { /* Do nothing */ }
        }

        lastTouchX = event.x
        lastTouchY = event.y
        return true
    }

    /**
     * Handle pointer up.
     */
    private fun handleActionPointerUp(event: MotionEvent): Boolean {
        if (currentMode == GestureMode.ZOOM) {
            currentMode = GestureMode.NONE
            activePointerId = INVALID_POINTER_ID
        }
        return true
    }

    /**
     * Handle action up.
     */
    private fun handleActionUp(event: MotionEvent): Boolean {
        when (currentMode) {
            GestureMode.SCROLL -> {
                val velocityX = event.x - touchStartX
                flingGesture?.onFling(velocityX)
            }
            GestureMode.ZOOM -> {
                zoomGesture?.onZoomEnd()
            }
            GestureMode.NONE -> {
                listener.onTap(event.x, event.y)
            }
            GestureMode.IGNORE -> { /* Do nothing */ }
        }

        currentMode = GestureMode.NONE
        activePointerId = INVALID_POINTER_ID
        return true
    }

    /**
     * Get focus X between two pointers.
     */
    private fun getFocusX(event: MotionEvent): Float {
        if (event.pointerCount < 2) return event.x
        return (event.getX(0) + event.getX(1)) / 2
    }

    /**
     * Get focus Y between two pointers.
     */
    private fun getFocusY(event: MotionEvent): Float {
        if (event.pointerCount < 2) return event.y
        return (event.getY(0) + event.getY(1)) / 2
    }

    /**
     * Get current scale between pointers.
     */
    private fun getCurrentScale(event: MotionEvent): Float {
        if (event.pointerCount < 2) return 1f

        val x0 = event.getX(0)
        val y0 = event.getY(0)
        val x1 = event.getX(1)
        val y1 = event.getY(1)

        val oldDistance = kotlin.math.sqrt((x1 - x0) * (x1 - x0) + (y1 - y0) * (y1 - y0))
        return zoomGesture?.getScaleFactor() ?: 1f
    }

    /**
     * Cancel current gesture.
     */
    fun cancel() {
        currentMode = GestureMode.NONE
        flingGesture?.cancel()
    }
}

/**
 * Gesture modes.
 */
enum class GestureMode {
    NONE,
    SCROLL,
    ZOOM,
    IGNORE
}

/**
 * Timeline gesture listener.
 */
interface TimelineGestureListener {
    fun onTap(x: Float, y: Float)
    fun onDoubleTap(x: Float, y: Float)
    fun onLongPress(x: Float, y: Float)
}