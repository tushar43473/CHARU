package com.app.clipsteronline.upload.editor.gestures

import android.view.MotionEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Preview gesture handler.
 * Preview drag, scale, rotate, overlay transforms.
 */
class PreviewGestureHandler(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
) {
    private var overlayX = 0.5f
    private var overlayY = 0.5f
    private var overlayScale = 1f
    private var overlayRotation = 0f
    private var overlayAlpha = 1f

    private var isDragging = false
    private var isScaling = false
    private var isRotating = false
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var initialScale = 1f
    private var initialRotation = 0f
    private var initialAngle = 0f
    private var initialDistance = 0f

    // Callback
    private var transformCallback: ((Float, Float, Float, Float, Float) -> Unit)? = null

    /**
     * Handle touch event.
     */
    fun onTouchEvent(event: MotionEvent, canvasWidth: Int, canvasHeight: Int) {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                onDragStart(event.x, event.y, canvasWidth, canvasHeight)
            }
            MotionEvent.ACTION_MOVE -> {
                when (event.pointerCount) {
                    1 -> if (isDragging) onDrag(event.x, event.y, canvasWidth, canvasHeight)
                    2 -> onTwoFinger(event, canvasWidth, canvasHeight)
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                onDragEnd()
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                if (event.pointerCount == 2) {
                    onTwoFingerStart(event)
                }
            }
        }
    }

    /**
     * Handle drag start.
     */
    private fun onDragStart(x: Float, y: Float, cw: Int, ch: Int) {
        isDragging = true
        lastTouchX = x
        lastTouchY = y
    }

    /**
     * Handle single finger drag.
     */
    private fun onDrag(x: Float, y: Float, canvasWidth: Int, canvasHeight: Int) {
        val deltaX = (x - lastTouchX) / canvasWidth
        val deltaY = (y - lastTouchY) / canvasHeight

        overlayX = (overlayX + deltaX).coerceIn(0f, 1f)
        overlayY = (overlayY + deltaY).coerceIn(0f, 1f)

        lastTouchX = x
        lastTouchY = y

        emitTransform()
    }

    /**
     * Two finger gesture start.
     */
    private fun onTwoFingerStart(event: MotionEvent) {
        isScaling = true
        isRotating = true
        initialScale = overlayScale
        initialRotation = overlayRotation

        val x0 = event.getX(0)
        val y0 = event.getY(0)
        val x1 = event.getX(1)
        val y1 = event.getY(1)

        initialDistance = kotlin.math.sqrt((x1 - x0) * (x1 - x0) + (y1 - y0) * (y1 - y0))
        initialAngle = kotlin.math.atan2((y1 - y0).toDouble(), (x1 - x0).toDouble()).toFloat()
    }

    /**
     * Two finger gesture move.
     */
    private fun onTwoFinger(event: MotionEvent, canvasWidth: Int, canvasHeight: Int) {
        if (!isScaling) return

        val x0 = event.getX(0)
        val y0 = event.getY(0)
        val x1 = event.getX(1)
        val y1 = event.getY(1)

        // Scale
        val distance = kotlin.math.sqrt((x1 - x0) * (x1 - x0) + (y1 - y0) * (y1 - y0))
        if (initialDistance > 0) {
            val scaleFactor = distance / initialDistance
            overlayScale = (initialScale * scaleFactor).coerceIn(0.1f, 5f)
        }

        // Rotation
        val angle = kotlin.math.atan2((y1 - y0).toDouble(), (x1 - x0).toDouble()).toFloat()
        val rotationDelta = angle - initialAngle
        overlayRotation = initialRotation + rotationDelta

        emitTransform()
    }

    /**
     * Handle drag end.
     */
    private fun onDragEnd() {
        isDragging = false
        isScaling = false
        isRotating = false
    }

    /**
     * Emit transform callback.
     */
    private fun emitTransform() {
        transformCallback?.invoke(
            overlayX,
            overlayY,
            overlayScale,
            overlayRotation,
            overlayAlpha
        )
    }

    /**
     * Set transform callback.
     */
    fun setTransformCallback(callback: (Float, Float, Float, Float, Float) -> Unit) {
        transformCallback = callback
    }

    /**
     * Set overlay position.
     */
    fun setOverlayPosition(x: Float, y: Float) {
        overlayX = x.coerceIn(0f, 1f)
        overlayY = y.coerceIn(0f, 1f)
    }

    /**
     * Set overlay.
     */
    fun setOverlay(scale: Float, rotation: Float, alpha: Float) {
        overlayScale = scale.coerceIn(0.1f, 5f)
        overlayRotation = rotation
        overlayAlpha = alpha.coerceIn(0f, 1f)
    }
}