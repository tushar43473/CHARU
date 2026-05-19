package com.app.clipsteronline.upload.editor.timeline.gestures

/**
 * Scroll gesture handler for timeline.
 * Handles smooth drag scrolling.
 */
class TimelineScrollGesture(
    private val listener: ScrollListener
) {
    private var isScrolling = false
    private var lastScrollX = 0f

    private var scrollOffset = 0f
    private var minScroll = 0f
    private var maxScroll = Float.MAX_VALUE

    private var edgeResistance = 0.5f

    companion object {
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
     * On scroll.
     */
    fun onScroll(distanceX: Float) {
        isScrolling = true

        val newOffset = scrollOffset - distanceX
        scrollOffset = applyEdgeResistance(newOffset)

        listener.onScroll(scrollOffset)
    }

    /**
     * On scroll start.
     */
    fun onScrollStart() {
        isScrolling = true
        listener.onScrollStart()
    }

    /**
     * On scroll end.
     */
    fun onScrollEnd() {
        isScrolling = false
        listener.onScrollEnd()
    }

    /**
     * Set scroll offset.
     */
    fun setScrollOffset(offset: Float) {
        scrollOffset = offset.coerceIn(minScroll, maxScroll)
        listener.onScroll(scrollOffset)
    }

    /**
     * Get scroll offset.
     */
    fun getScrollOffset(): Float = scrollOffset

    /**
     * Check if scrolling.
     */
    fun isScrolling(): Boolean = isScrolling

    /**
     * Apply edge resistance.
     */
    private fun applyEdgeResistance(offset: Float): Float {
        return when {
            offset < minScroll -> {
                val overscroll = minScroll - offset
                offset + overscroll * edgeResistance
            }
            offset > maxScroll -> {
                val overscroll = offset - maxScroll
                offset - overscroll * edgeResistance
            }
            else -> offset
        }
    }

    /**
     * Scroll listener.
     */
    interface ScrollListener {
        fun onScroll(scrollOffset: Float)
        fun onScrollStart()
        fun onScrollEnd()
    }
}