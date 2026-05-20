package com.app.clipsteronline.upload.editor.core.utils

import kotlin.math.atan2
import kotlin.math.hypot

object GestureUtils {
    fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float =
        hypot((x2 - x1).toDouble(), (y2 - y1).toDouble()).toFloat()

    fun velocity(deltaPx: Float, deltaTimeMs: Long): Float {
        require(deltaTimeMs > 0)
        return (deltaPx / deltaTimeMs) * 1000f
    }

    fun scaleFactor(initialDistance: Float, currentDistance: Float): Float {
        require(initialDistance > 0f)
        return (currentDistance / initialDistance).coerceIn(0.1f, 10f)
    }

    fun rotationDegrees(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        return Math.toDegrees(atan2((y2 - y1).toDouble(), (x2 - x1).toDouble())).toFloat()
    }

    fun snap(value: Float, targets: List<Float>, threshold: Float): Float {
        if (targets.isEmpty()) return value
        val nearest = targets.minByOrNull { kotlin.math.abs(it - value) } ?: return value
        return if (kotlin.math.abs(nearest - value) <= threshold) nearest else value
    }
}
