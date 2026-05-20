package com.app.clipsteronline.upload.editor.core.utils

import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.hypot

object GestureUtils {
    fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float =
        hypot((x2 - x1).toDouble(), (y2 - y1).toDouble()).toFloat()

    fun velocity(deltaPx: Float, deltaTimeMs: Long): Float {
        if (deltaTimeMs <= 0L) return 0f
        return (deltaPx / deltaTimeMs) * 1000f
    }

    fun safeScaleFactor(initialDistance: Float, currentDistance: Float): Float {
        if (initialDistance <= 0f) return 1f
        return (currentDistance / initialDistance).coerceIn(0.2f, 6f)
    }

    fun rotationDegrees(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        return Math.toDegrees(atan2((y2 - y1).toDouble(), (x2 - x1).toDouble())).toFloat()
    }

    fun normalizeDegrees(angle: Float): Float {
        var normalized = angle
        while (normalized > 180f) normalized -= 360f
        while (normalized < -180f) normalized += 360f
        return normalized
    }

    fun snap(value: Float, targets: List<Float>, threshold: Float): Float {
        if (targets.isEmpty()) return value
        val nearest = targets.minByOrNull { abs(it - value) } ?: return value
        return if (abs(nearest - value) <= threshold) nearest else value
    }
}
