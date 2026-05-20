package com.app.clipsteronline.upload.editor.core.utils

import kotlin.math.PI
import kotlin.math.cos

object MathUtils {
    fun clamp(value: Float, min: Float, max: Float): Float = value.coerceIn(min, max)

    fun lerp(from: Float, to: Float, t: Float): Float = from + (to - from) * t.coerceIn(0f, 1f)

    fun inverseLerp(from: Float, to: Float, value: Float): Float {
        if (from == to) return 0f
        return ((value - from) / (to - from)).coerceIn(0f, 1f)
    }

    fun easeInOut(t: Float): Float {
        val x = t.coerceIn(0f, 1f)
        return ((1 - cos(x * PI)) * 0.5f).toFloat()
    }

    fun mapTimelineToPx(timeMs: Long, pixelsPerSecond: Float, zoom: Float): Float {
        require(pixelsPerSecond > 0f)
        require(zoom > 0f)
        return (timeMs / 1000f) * pixelsPerSecond * zoom
    }

    fun mapPxToTimeline(px: Float, pixelsPerSecond: Float, zoom: Float): Long {
        require(pixelsPerSecond > 0f)
        require(zoom > 0f)
        return ((px / (pixelsPerSecond * zoom)) * 1000f).toLong().coerceAtLeast(0L)
    }
}
