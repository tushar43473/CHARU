package com.app.clipsteronline.upload.editor.timeline.gestures

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Fling gesture handler for timeline.
 * Handles velocity-based scrolling with damping.
 */
class TimelineFlingGesture(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main),
    private val listener: FlingListener
) {
    private var flingJob: Job? = null
    private var isFlinging = false

    private var velocity = 0f
    private var currentPosition = 0f
    private var targetPosition = 0f

    private var minPosition = 0f
    private var maxPosition = Float.MAX_VALUE

    private var friction = 0.95f
    private var minVelocity = 50f
    private var stopThreshold = 1f

    companion object {
        private const val FRAME_TIME_MS = 16L
    }

    /**
     * Set position bounds.
     */
    fun setBounds(min: Float, max: Float) {
        minPosition = min
        maxPosition = max
    }

    /**
     * On fling.
     */
    fun onFling(velocityX: Float) {
        if (kotlin.math.abs(velocityX) < minVelocity) return

        cancel()

        velocity = velocityX
        isFlinging = true
        currentPosition = listener.getCurrentPosition()

        flingJob = scope.launch {
            while (isActive && isFlinging && kotlin.math.abs(velocity) > minVelocity) {
                // Apply velocity
                velocity *= friction

                // Update position
                currentPosition -= velocity * (FRAME_TIME_MS / 1000f)

                // Apply bounds
                currentPosition = currentPosition.coerceIn(minPosition, maxPosition)

                // Notify listener
                listener.onFlingUpdate(currentPosition)

                // Check if should stop
                if (kotlin.math.abs(velocity) < minVelocity) {
                    break
                }

                kotlinx.coroutines.delay(FRAME_TIME_MS)
            }

            // Snap to bounds
            currentPosition = currentPosition.coerceIn(minPosition, maxPosition)
            listener.onFlingEnd(currentPosition)

            isFlinging = false
        }
    }

    /**
     * Cancel fling.
     */
    fun cancel() {
        flingJob?.cancel()
        flingJob = null
        isFlinging = false
        velocity = 0f
    }

    /**
     * Stop fling.
     */
    fun stop() {
        cancel()
        listener.onFlingEnd(currentPosition)
    }

    /**
     * Set friction.
     */
    fun setFriction(friction: Float) {
        this.friction = friction.coerceIn(0.8f, 0.99f)
    }

    /**
     * Set minimum velocity.
     */
    fun setMinVelocity(velocity: Float) {
        this.minVelocity = velocity
    }

    /**
     * Check if flinging.
     */
    fun isFlinging(): Boolean = isFlinging

    /**
     * Fling listener.
     */
    interface FlingListener {
        fun onFlingUpdate(position: Float)
        fun onFlingEnd(position: Float)
        fun getCurrentPosition(): Float
    }
}