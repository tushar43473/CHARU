package com.app.clipsteronline.upload.editor.gestures

import android.view.MotionEvent
import android.view.VelocityTracker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * Multi-touch handler.
 * Finger tracking, pinch, rotation, gesture smoothing.
 */
class MultiTouchHandler(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
) {
    private val pointers = mutableMapOf<Int, Pointer>()
    private var gestureState = MultiTouchState()

    // Touch history for smoothing
    private val historySamples = mutableListOf<TouchSample>()
    private var maxHistorySize = 5

    // Tracking state
    private var initialPinchDistance = 0f
    private var initialAngle = 0f
    private var centerX = 0f
    private var centerY = 0f

    /**
     * Process touch event.
     */
    fun onTouchEvent(event: MotionEvent): MultiTouchResult {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                onPointerDown(event, 0)
            }
            MotionEvent.ACTION_MOVE -> {
                for (i in 0 until event.pointerCount) {
                    onPointerMove(event, i)
                }
            }
            MotionEvent.ACTION_UP -> {
                onPointerUp(event, 0)
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                onMultiPointerDown(event)
            }
            MotionEvent.ACTION_POINTER_UP -> {
                onPointerUp(event, event.actionIndex)
            }
        }

        return calculateGesture()
    }

    /**
     * Handle pointer down.
     */
    private fun onPointerDown(event: MotionEvent, index: Int) {
        val pointerId = event.getPointerId(index)
        val x = event.getX(index)
        val y = event.getY(index)

        pointers[pointerId] = Pointer(pointerId, x, y, System.currentTimeMillis())
    }

    /**
     * Handle pointer move.
     */
    private fun onPointerMove(event: MotionEvent, index: Int) {
        val pointerId = event.getPointerId(index)
        val x = event.getX(index)
        val y = event.getY(index)

        pointers[pointerId]?.let {
            it.lastX = x
            it.lastY = y
            it.lastTime = System.currentTimeMillis()
        }
    }

    /**
     * Handle pointer up.
     */
    private fun onPointerUp(event: MotionEvent, index: Int) {
        val pointerId = event.getPointerId(index)
        pointers.remove(pointerId)
    }

    /**
     * Handle multi-pointer down.
     */
    private fun onMultiPointerDown(event: MotionEvent) {
        if (pointers.size == 2) {
            // Calculate initial pinch
            val p0 = pointers.values.elementAt(0)
            val p1 = pointers.values.elementAt(1)

            initialPinchDistance = distance(p0.x, p0.y, p1.x, p1.y)
            initialAngle = atan2((p1.y - p0.y).toDouble(), (p1.x - p0.x).toDouble()).toFloat()

            centerX = (p0.x + p1.x) / 2
            centerY = (p0.y + p1.y) / 2

            gestureState = gestureState.copy(n = 2)
        }
    }

    /**
     * Calculate gesture result.
     */
    private fun calculateGesture(): MultiTouchResult {
        if (pointers.size < 2) {
            gestureState = MultiTouchState(n = 1)
            return MultiTouchResult()
        }

        val ps = pointers.values.toList()
        val p0 = ps[0]
        val p1 = ps[1]

        // Current separation
        val distance = distance(p0.x, p0.y, p1.x, p1.y)
        val angle = atan2((p1.y - p0.y).toDouble(), (p1.x - p0.x).toDouble()).toFloat()

        // Scale from initial
        val scale = if (initialPinchDistance > 0) distance / initialPinchDistance else 1f

        // Rotation delta
        val rotationDelta = angle - initialAngle

        gestureState = gestureState.copy(
            n = ps.size,
            scale = scale,
            rotation = rotationDelta,
            centerX = centerX,
            centerY = centerY,
            isPinching = true,
            isRotating = true
        )

        return MultiTouchResult(
            scale = scale,
            rotation = rotationDelta,
            centerX = centerX,
            centerY = centerY,
            isTwoFinger = true
        )
    }

    /**
     * Calculate distance.
     */
    private fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val dx = x2 - x1
        val dy = y2 - y1
        return sqrt(dx * dx + dy * dy)
    }

    /**
     * Get current state.
     */
    fun getState(): MultiTouchState = gestureState

    /**
     * Clear state.
     */
    fun clear() {
        pointers.clear()
        gestureState = MultiTouchState()
    }
}

/**
 * Pointer data.
 */
data class Pointer(
    val id: Int,
    var x: Float,
    var y: Float,
    var lastX: Float = x,
    var lastY: Float = y,
    var lastTime: Long = 0
)

/**
 * Multi-touch state.
 */
data class MultiTouchState(
    val n: Int = 0,
    val scale: Float = 1f,
    val rotation: Float = 0f,
    val centerX: Float = 0f,
    val centerY: Float = 0f,
    val isPinching: Boolean = false,
    val isRotating: Boolean = false
)

/**
 * Multi-touch result.
 */
data class MultiTouchResult(
    val scale: Float = 1f,
    val rotation: Float = 0f,
    val centerX: Float = 0f,
    val centerY: Float = 0f,
    val isTwoFinger: Boolean = false
)

/**
 * Touch sample for averaging.
 */
data class TouchSample(
    val time: Long,
    val x: Float,
    val y: Float,
    val scale: Float,
    val rotation: Float
)