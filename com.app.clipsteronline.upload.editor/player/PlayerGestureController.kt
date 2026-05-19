package com.app.clipsteronline.upload.editor.player

import android.view.MotionEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Controller for player gestures on preview.
 * Handles pinch zoom, double-tap, and pan gestures.
 */
class PlayerGestureController(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
) {
    private val _scale = MutableStateFlow(1f)
    val scale: StateFlow<Float> = _scale.asStateFlow()

    private val _offsetX = MutableStateFlow(0f)
    val offsetX: StateFlow<Float> = _offsetX.asStateFlow()

    private val _offsetY = MutableStateFlow(0f)
    val offsetY: StateFlow<Float> = _offsetY.asStateFlow()

    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var lastSpan = 0f
    private var isScaling = false

    private var gestureListener: GestureListener? = null

    companion object {
        private const val MIN_SCALE = 0.5f
        private const val MAX_SCALE = 5f
        private const val DOUBLE_TAP_TIMEOUT = 300
        private const val DOUBLE_TAP_SLOP = 100f
    }

    /**
     * Set gesture listener.
     */
    fun setGestureListener(listener: GestureListener?) {
        this.gestureListener = listener
    }

    /**
     * Handle touch event.
     */
    fun onTouch(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = event.x
                lastTouchY = event.y
                return true
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                if (event.pointerCount == 2) {
                    isScaling = true
                    lastSpan = getSpan(event)
                }
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                if (isScaling && event.pointerCount >= 2) {
                    handlePinch(event)
                } else {
                    handlePan(event)
                }
                return true
            }

            MotionEvent.ACTION_UP -> {
                if (isScaling) {
                    isScaling = false
                } else {
                    handleTap(event)
                }
                return true
            }

            MotionEvent.ACTION_POINTER_UP -> {
                if (event.pointerCount <= 2) {
                    isScaling = false
                }
                return true
            }
        }
        return false
    }

    /**
     * Handle pinch zoom.
     */
    private fun handlePinch(event: MotionEvent) {
        val span = getSpan(event)
        val scaleFactor = span / lastSpan

        val newScale = (_scale.value * scaleFactor).coerceIn(MIN_SCALE, MAX_SCALE)
        _scale.value = newScale

        lastSpan = span
    }

    /**
     * Handle pan/drag.
     */
    private fun handlePan(event: MotionEvent) {
        val dx = event.x - lastTouchX
        val dy = event.y - lastTouchY

        _offsetX.value += dx
        _offsetY.value += dy

        lastTouchX = event.x
        lastTouchY = event.y
    }

    /**
     * Handle tap.
     */
    private fun handleTap(event: MotionEvent) {
        gestureListener?.onTap(event.x, event.y)
    }

    /**
     * Handle double tap.
     */
    private fun handleDoubleTap(x: Float, y: Float) {
        if (_scale.value > 1f) {
            // Reset zoom
            resetZoom()
            gestureListener?.onDoubleTapReset()
        } else {
            // Zoom in
            _scale.value = 2f
            gestureListener?.onDoubleTapZoom(x, y)
        }
    }

    /**
     * Reset zoom to default.
     */
    fun resetZoom() {
        _scale.value = 1f
        _offsetX.value = 0f
        _offsetY.value = 0f
    }

    /**
     * Set zoom level.
     */
    fun setZoom(scale: Float) {
        _scale.value = scale.coerceIn(MIN_SCALE, MAX_SCALE)
    }

    /**
     * Set pan position.
     */
    fun setOffset(x: Float, y: Float) {
        _offsetX.value = x
        _offsetY.value = y
    }

    /**
     * Get current transform values.
     */
    fun getTransform(): GestureTransform {
        return GestureTransform(
            scale = _scale.value,
            offsetX = _offsetX.value,
            offsetY = _offsetY.value
        )
    }

    /**
     * Apply constraints to transform.
     */
    fun constrainToBounds(maxOffsetX: Float, maxOffsetY: Float) {
        _offsetX.value = _offsetX.value.coerceIn(-maxOffsetX, maxOffsetX)
        _offsetY.value = _offsetY.value.coerceIn(-maxOffsetY, maxOffsetY)
    }

    /**
     * Get span between two pointers.
     */
    private fun getSpan(event: MotionEvent): Float {
        if (event.pointerCount < 2) return 0f

        val x0 = event.getX(0)
        val y0 = event.getY(0)
        val x1 = event.getX(1)
        val y1 = event.getY(1)

        val dx = x1 - x0
        val dy = y1 - y0

        return kotlin.math.sqrt(dx * dx + dy * dy)
    }

    /**
     * Velocity for fling.
     */
    fun calculateVelocity(history: List<MotionEvent>): Float {
        if (history.size < 2) return 0f

        val first = history.first()
        val last = history.last()

        val dx = last.x - first.x
        val dt = (last.eventTime - first.eventTime).toFloat()

        return if (dt > 0) dx / dt else 0f
    }
}

/**
 * Gesture listener interface.
 */
interface GestureListener {
    fun onTap(x: Float, y: Float)
    fun onDoubleTapZoom(x: Float, y: Float)
    fun onDoubleTapReset()
    fun onScaleChanged(scale: Float)
    fun onPanChanged(offsetX: Float, offsetY: Float)
}

/**
 * Gesture transform data.
 */
data class GestureTransform(
    val scale: Float = 1f,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f
)