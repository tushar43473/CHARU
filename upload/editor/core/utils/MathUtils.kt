package upload.editor.core.utils

import kotlin.math.PI
import kotlin.math.cos

object MathUtils {
    fun clamp(value: Float, min: Float, max: Float): Float = value.coerceIn(min, max)

    fun lerp(start: Float, end: Float, fraction: Float): Float = start + (end - start) * fraction.coerceIn(0f, 1f)

    fun inverseLerp(start: Float, end: Float, value: Float): Float {
        if (start == end) return 0f
        return ((value - start) / (end - start)).coerceIn(0f, 1f)
    }

    fun easeInOut(t: Float): Float {
        val x = t.coerceIn(0f, 1f)
        return ((1 - cos(x * PI)) * 0.5).toFloat()
    }

    fun smoothDamp(current: Float, target: Float, velocity: Float, smoothTime: Float, deltaTime: Float): Pair<Float, Float> {
        val omega = 2f / smoothTime.coerceAtLeast(0.0001f)
        val x = omega * deltaTime
        val exp = 1f / (1f + x + 0.48f * x * x + 0.235f * x * x * x)
        val change = current - target
        val temp = (velocity + omega * change) * deltaTime
        val newVelocity = (velocity - omega * temp) * exp
        val newValue = target + (change + temp) * exp
        return newValue to newVelocity
    }

    fun scaleToFit(srcWidth: Int, srcHeight: Int, dstWidth: Int, dstHeight: Int): Pair<Int, Int> {
        require(srcWidth > 0 && srcHeight > 0 && dstWidth > 0 && dstHeight > 0)
        val ratio = minOf(dstWidth.toFloat() / srcWidth, dstHeight.toFloat() / srcHeight)
        return (srcWidth * ratio).toInt() to (srcHeight * ratio).toInt()
    }
}
