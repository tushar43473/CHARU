package upload.editor.core.utils

import kotlin.math.hypot

object GestureUtils {
    fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float =
        hypot((x2 - x1).toDouble(), (y2 - y1).toDouble()).toFloat()

    fun velocity(deltaPx: Float, deltaTimeMs: Long): Float {
        require(deltaTimeMs > 0)
        return deltaPx / deltaTimeMs * 1000f
    }

    fun pinchScale(initialDistance: Float, currentDistance: Float): Float {
        require(initialDistance > 0f)
        return (currentDistance / initialDistance).coerceIn(0.1f, 10f)
    }

    fun flingDistance(initialVelocityPxPerSec: Float, friction: Float = 0.92f, minVelocity: Float = 5f): Float {
        var velocity = initialVelocityPxPerSec
        var distance = 0f
        while (kotlin.math.abs(velocity) > minVelocity) {
            distance += velocity / 60f
            velocity *= friction
        }
        return distance
    }

    fun snap(value: Float, anchors: List<Float>, threshold: Float): Float {
        if (anchors.isEmpty()) return value
        val nearest = anchors.minByOrNull { kotlin.math.abs(it - value) } ?: return value
        return if (kotlin.math.abs(nearest - value) <= threshold) nearest else value
    }

    fun smooth(current: Float, target: Float, factor: Float = 0.15f): Float {
        val alpha = factor.coerceIn(0f, 1f)
        return current + (target - current) * alpha
    }
}
