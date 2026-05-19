package com.app.clipsteronline.upload.editor.timeline.engine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

/**
 * Smooth horizontal scrolling for timeline.
 * Handles fling, inertia, and edge resistance.
 */
class TimelineScrollEngine(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
) {
    private val _scroll = MutableStateFlow(0f)
    val scroll: StateFlow<Float> = _scroll.asStateFlow()

    private var scrollJob: Job? = null

    private var minScroll = 0f
    private var maxScroll = Float.MAX_VALUE
    private var isFlinging = false

    companion object {
        private const val SCROLL_FRICTION = 0.95f
        private const val SCROLL_MIN_VELOCITY = 50f
        private const val EDGE_RESISTANCE = 0.5f
        private const val EDGE_MARGIN = 50f
    }

    /**
     * Set scroll bounds.
     */
    fun setScrollBounds(min: Float, max: Float) {
        minScroll = min
        maxScroll = max
    }

    /**
     * Scroll to position.
     */
    fun scrollTo(position: Float, animate: Boolean = false) {
        val clampedPosition = position.coerceIn(minScroll, maxScroll)

        if (animate) {
            smoothScrollTo(clampedPosition)
        } else {
            _scroll.value = clampedPosition
            scrollJob?.cancel()
        }
    }

    /**
     * Smooth scroll to position.
     */
    private fun smoothScrollTo(target: Float) {
        scrollJob?.cancel()
        scrollJob = scope.launch {
            val start = _scroll.value
            var current = start

            while (kotlin.math.abs(current - target) > 1f) {
                current += (target - current) * 0.2f
                _scroll.value = current
                kotlinx.coroutines.delay(16)
            }

            _scroll.value = target
        }
    }

    /**
     * Handle touch scroll.
     */
    fun onScroll(distanceX: Float) {
        scrollJob?.cancel()
        isFlinging = false

        val newScroll = _scroll.value - distanceX
        _scroll.value = applyEdgeResistance(newScroll)
    }

    /**
     * Start fling.
     */
    fun fling(velocityX: Float) {
        if (kotlin.math.abs(velocityX) < SCROLL_MIN_VELOCITY) return

        isFlinging = true
        scrollJob?.cancel()

        scrollJob = scope.launch {
            var velocity = velocityX
            var current = _scroll.value

            while (isActive && kotlin.math.abs(velocity) > SCROLL_MIN_VELOCITY) {
                velocity *= SCROLL_FRICTION
                current -= velocity * 0.016f // 16ms frame

                current = applyEdgeResistance(current)
                _scroll.value = current

                kotlinx.coroutines.delay(16)
            }

            isFlinging = false

            // Snap to edge
            if (current < minScroll) {
                _scroll.value = minScroll
            } else if (current > maxScroll) {
                _scroll.value = maxScroll
            }
        }
    }

    /**
     * Stop fling.
     */
    fun stopFling() {
        isFlinging = false
        scrollJob?.cancel()
    }

    /**
     * Apply edge resistance.
     */
    private fun applyEdgeResistance(scroll: Float): Float {
        return when {
            scroll < minScroll -> {
                val overscroll = minScroll - scroll
                scroll + overscroll * EDGE_RESISTANCE
            }
            scroll > maxScroll -> {
                val overscroll = scroll - maxScroll
                scroll - overscroll * EDGE_RESISTANCE
            }
            else -> scroll
        }
    }

    /**
     * Scroll to make position visible.
     */
    fun ensureVisible(
        positionX: Float,
        viewWidth: Float,
        zoom: Float = 1f
    ) {
        val current = _scroll.value

        when {
            positionX < current -> scrollTo(positionX - EDGE_MARGIN)
            positionX > current + viewWidth -> scrollTo(positionX - viewWidth + EDGE_MARGIN)
        }
    }

    /**
     * Get current scroll.
     */
    fun getScroll(): Float = _scroll.value

    /**
     * Check if scrolling.
     */
    fun isScrolling(): Boolean = isFlinging || scrollJob?.isActive == true

    /**
     * Get available scroll range.
     */
    fun getScrollRange(): Float = maxScroll - minScroll
}