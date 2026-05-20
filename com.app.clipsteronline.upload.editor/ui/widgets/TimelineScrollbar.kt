package com.app.clipsteronline.upload.editor.ui.widgets

class TimelineScrollbar(
    private val viewportMs: Long,
    private val totalMs: Long,
) {
    init {
        require(viewportMs > 0)
        require(totalMs >= viewportMs)
    }

    fun thumbFraction(): Float = (viewportMs.toFloat() / totalMs.toFloat()).coerceIn(0f, 1f)

    fun scrollToFraction(fraction: Float): Long {
        val f = fraction.coerceIn(0f, 1f)
        val max = totalMs - viewportMs
        return (max * f).toLong()
    }
}
