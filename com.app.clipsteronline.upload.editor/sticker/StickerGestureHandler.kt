package com.app.clipsteronline.upload.editor.sticker

import android.view.MotionEvent
import kotlin.math.atan2

/**
 * Gesture handler for sticker manipulation.
 * Handles drag, scale, rotate, and multitouch.
 */
class StickerGestureHandler {

    private var activeStickerId: String? = null
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var initialScale = 1f
    private var initialRotation = 0f
    private var initialX = 0f
    private var initialY = 0f
    private var lastTouchX = 0f
    private var lastTouchY = 0f

    private var activePointerId = -1
    private var isScaling = false
    private var isRotating = false
    private var isDragging = false

    private val transformCallback: ((String, StickerTransform) -> Unit)? = null

    /**
     * Handle touch event.
     */
    fun onTouchEvent(
        stickerId: String,
        event: MotionEvent,
        sticker: Sticker,
        canvasWidth: Int,
        canvasHeight: Int
    ): Boolean {
        val action = event.actionMasked
        val pointerIndex = event.actionIndex
        val pointerId = event.getPointerId(pointerIndex)

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                activeStickerId = stickerId
                initialTouchX = event.x
                initialTouchY = event.y

                initialX = sticker.x
                initialY = sticker.y
                initialScale = sticker.scale
                initialRotation = sticker.rotation

                lastTouchX = event.x
                lastTouchY = event.y

                activePointerId = pointerId
                isDragging = true

                return true
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                val pointers = event.pointerCount
                if (pointers == 2) {
                    isScaling = true
                    isRotating = true

                    val dx = event.getX(1) - event.getX(0)
                    val dy = event.getY(1) - event.getY(0)
                    initialScale = sticker.scale
                    initialRotation = atan2(dy.toDouble(), dx.toDouble()).toFloat()
                }

                return true
            }

            MotionEvent.ACTION_MOVE -> {
                if (isDragging && activeStickerId == stickerId) {
                    if (event.pointerCount == 1) {
                        // Drag
                        val dx = event.x - initialTouchX
                        val dy = event.y - initialTouchY

                        val newX = initialX + dx / canvasWidth
                        val newY = initialY + dy / canvasHeight

                        transformCallback?.invoke(stickerId, StickerTransform(
                            x = newX.coerceIn(0f, 1f),
                            y = newY.coerceIn(0f, 1f),
                            scale = sticker.scale,
                            rotation = sticker.rotation
                        ))
                    } else if (event.pointerCount >= 2) {
                        // Scale and rotate
                        val dx = event.getX(1) - event.getX(0)
                        val dy = event.getY(1) - event.getY(0)
                        val currentAngle = atan2(dy.toDouble(), dx.toDouble()).toFloat()

                        val scaleFactor = kotlin.math.sqrt(
                            (dx * dx + dy * dy).toFloat()
                        ) / 100f

                        val newScale = (initialScale * scaleFactor).coerceIn(0.1f, 5f)
                        val newRotation = initialRotation + currentAngle - initialRotation

                        transformCallback?.invoke(stickerId, StickerTransform(
                            x = sticker.x,
                            y = sticker.y,
                            scale = newScale,
                            rotation = newRotation
                        ))
                    }
                }

                return true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isDragging = false
                isScaling = false
                isRotating = false
                activeStickerId = null

                return true
            }

            MotionEvent.ACTION_POINTER_UP -> {
                if (pointerId == activePointerId) {
                    isScaling = false
                    isRotating = false
                }

                return true
            }
        }

        return false
    }

    /**
     * Set transform callback.
     */
    fun setTransformCallback(callback: (String, StickerTransform) -> Unit) {
        transformCallback = callback
    }

    /**
     * Snap to grid.
     */
    fun snapToGrid(value: Float, gridSize: Float = 0.05f): Float {
        return kotlin.math.round(value / gridSize) * gridSize
    }

    /**
     * Snap to center.
     */
    fun snapToCenter(value: Float, threshold: Float = 0.02f): Float {
        return if (kotlin.math.abs(value - 0.5f) < threshold) 0.5f else value
    }

    /**
     * Snap position.
     */
    fun snapPosition(x: Float, y: Float): Pair<Float, Float> {
        return snapToCenter(x) to snapToCenter(y)
    }

    /**
     * Constrain to bounds.
     */
    fun constrainToBounds(
        x: Float,
        y: Float,
        scale: Float,
        canvasWidth: Int,
        canvasHeight: Int,
        stickerWidth: Int,
        stickerHeight: Int
    ): Pair<Float, Float> {
        val scaledWidth = stickerWidth * scale
        val scaledHeight = stickerHeight * scale

        val minX = scaledWidth / canvasWidth / 2
        val maxX = 1f - minX
        val minY = scaledHeight / canvasHeight / 2
        val maxY = 1f - minY

        return x.coerceIn(minX, maxX) to y.coerceIn(minY, maxY)
    }
}