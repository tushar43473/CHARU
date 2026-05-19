package com.app.clipsteronline.upload.editor.core.utils

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Math utilities for animation and interpolation calculations.
 * Provides easing, clamping, lerping, and scaling functions.
 */
object MathUtils {

    private const val PI_F = PI.toFloat()
    private const val TWO_PI_F = (2 * PI).toFloat()
    private const val HALF_PI_F = (PI / 2).toFloat()

    /**
     * Linear interpolation between two values.
     */
    fun lerp(start: Float, end: Float, t: Float): Float {
        return start + (end - start) * t.coerceIn(0f, 1f)
    }

    /**
     * Linear interpolation with clamping.
     */
    fun lerpClamped(start: Float, end: Float, t: Float): Float {
        return start + (end - start) * t.coerceIn(0f, 1f)
    }

    /**
     * Inverse linear interpolation.
     */
    fun inverseLerp(start: Float, end: Float, value: Float): Float {
        return if (end == start) 0f else ((value - start) / (end - start)).coerceIn(0f, 1f)
    }

    /**
     * Lerp for integers.
     */
    fun lerpInt(start: Int, end: Int, t: Float): Int {
        return lerp(start.toFloat(), end.toFloat(), t).toInt()
    }

    /**
     * Lerp for long values.
     */
    fun lerpLong(start: Long, end: Long, t: Float): Long {
        return lerp(start.toFloat(), end.toFloat(), t).toLong()
    }

    /**
     * Clamp value between min and max.
     */
    fun clamp(value: Float, min: Float, max: Float): Float {
        return value.coerceIn(min, max)
    }

    /**
     * Clamp value between 0 and 1.
     */
    fun clamp01(value: Float): Float {
        return value.coerceIn(0f, 1f)
    }

    /**
     * Clamp integer.
     */
    fun clampInt(value: Int, min: Int, max: Int): Int {
        return value.coerceIn(min, max)
    }

    /**
     * Remap value from one range to another.
     */
    fun remap(value: Float, inMin: Float, inMax: Float, outMin: Float, outMax: Float): Float {
        val t = inverseLerp(inMin, inMax, value)
        return lerp(outMin, outMax, t)
    }

    /**
     * Remap with clamping.
     */
    fun remapClamped(value: Float, inMin: Float, inMax: Float, outMin: Float, outMax: Float): Float {
        val t = inverseLerp(inMin, inMax, value)
        return if (t < 0f || t > 1f) {
            if (t < 0f) outMin else outMax
        } else {
            lerp(outMin, outMax, t)
        }
    }

    /**
     * Ease in - quadratic.
     */
    fun easeInQuad(t: Float): Float {
        return t * t
    }

    /**
     * Ease out - quadratic.
     */
    fun easeOutQuad(t: Float): Float {
        return 1 - (1 - t) * (1 - t)
    }

    /**
     * Ease in out - quadratic.
     */
    fun easeInOutQuad(t: Float): Float {
        return if (t < 0.5f) {
            2 * t * t
        } else {
            1 - (-2 * t + 2).let { it * it } / 2
        }
    }

    /**
     * Ease in - cubic.
     */
    fun easeInCubic(t: Float): Float {
        return t * t * t
    }

    /**
     * Ease out - cubic.
     */
    fun easeOutCubic(t: Float): Float {
        return 1 - (1 - t).let { it * it * it }
    }

    /**
     * Ease in out - cubic.
     */
    fun easeInOutCubic(t: Float): Float {
        return if (t < 0.5f) {
            4 * t * t * t
        } else {
            1 - (-2 * t + 2).let { 4 * it * it * it } / 2
        }
    }

    /**
     * Ease in - quartic.
     */
    fun easeInQuart(t: Float): Float {
        return t * t * t * t
    }

    /**
     * Ease out - quartic.
     */
    fun easeOutQuart(t: Float): Float {
        return 1 - (1 - t).let { it * it * it * it }
    }

    /**
     * Ease in out - quartic.
     */
    fun easeInOutQuart(t: Float): Float {
        return if (t < 0.5f) {
            8 * t * t * t * t
        } else {
            1 - (-2 * t + 2).let { 8 * it * it * it * it } / 2
        }
    }

    /**
     * Ease in - sine.
     */
    fun easeInSine(t: Float): Float {
        return 1 - cos(t * HALF_PI_F)
    }

    /**
     * Ease out - sine.
     */
    fun easeOutSine(t: Float): Float {
        return sin(t * HALF_PI_F)
    }

    /**
     * Ease in out - sine.
     */
    fun easeInOutSine(t: Float): Float {
        return -(cos(PI_F * t) - 1) / 2
    }

    /**
     * Ease in - exponential.
     */
    fun easeInExpo(t: Float): Float {
        return if (t == 0f) 0f else 2f.pow(10 * t - 10)
    }

    /**
     * Ease out - exponential.
     */
    fun easeOutExpo(t: Float): Float {
        return if (t == 1f) 1f else 1 - 2f.pow(-10 * t)
    }

    /**
     * Ease in out - exponential.
     */
    fun easeInOutExpo(t: Float): Float {
        return when {
            t == 0f -> 0f
            t == 1f -> 1f
            t < 0.5f -> 2f.pow(20 * t - 11) / 2
            else -> (2 - 2f.pow(-20 * t + 10)) / 2
        }
    }

    /**
     * Ease in - circular.
     */
    fun easeInCirc(t: Float): Float {
        return 1 - sqrt(1 - t * t)
    }

    /**
     * Ease out - circular.
     */
    fun easeOutCirc(t: Float): Float {
        return sqrt(1 - (1 - t) * (1 - t))
    }

    /**
     * Ease in out - circular.
     */
    fun easeInOutCirc(t: Float): Float {
        return if (t < 0.5f) {
            (1 - sqrt(1 - 4 * t * t)) / 2
        } else {
            (sqrt(1 - (-2 * t + 2) * (-2 * t + 2)) / 2 + 0.5f
        }
    }

    /**
     * Bounce out easing.
     */
    fun easeOutBounce(t: Float): Float {
        val n1 = 7.5625f
        val d1 = 2.75f

        return when {
            t < 1 / d1 -> n1 * t * t
            t < 2 / d1 -> n1 * (t - 1.5f / d1).let { it * it } + 0.75f
            t < 2.5f / d1 -> n1 * (t - 2.25f / d1).let { it * it } + 0.9375f
            else -> n1 * (t - 2.625f / d1).let { it * it } + 0.984375f
        }
    }

    /**
     * Bounce in easing.
     */
    fun easeInBounce(t: Float): Float {
        return 1 - easeOutBounce(1 - t)
    }

    /**
     * Elastic out easing.
     */
    fun easeOutElastic(t: Float): Float {
        return if (t == 0f || t == 1f) t else {
            val p = 0.3f
            val s = p / 4f
            2f.pow(-10 * t) * sin((t - s) * TWO_PI_F / p) + 1
        }
    }

    /**
     * Elastic in easing.
     */
    fun easeInElastic(t: Float): Float {
        return if (t == 0f || t == 1f) t else {
            val p = 0.3f
            val s = p / 4f
            -2f.pow(10 * t) * sin((t - s) * TWO_PI_F / p - 1f)
        }
    }

    /**
     * Back out easing.
     */
    fun easeOutBack(t: Float): Float {
        val c1 = 1.70158f
        val c3 = c1 + 1f
        return 1 + c3 * (t - 1).let { it * it * it } + c1 * (t - 1).let { it * it }
    }

    /**
     * Back in easing.
     */
    fun easeInBack(t: Float): Float {
        val c1 = 1.70158f
        val c3 = c1 + 1f
        return c3 * t * t * t - c1 * t * t
    }

    /**
     * Back in out easing.
     */
    fun easeInOutBack(t: Float): Float {
        val c1 = 1.70158f
        val c2 = c1 * 1.525f

        return if (t < 0.5f) {
            (2 * t).let { it * it * ((c2 + 1) * it - c2) } / 2
        } else {
            (2 * t - 2).let { it * it * ((c2 + 1) * it + c2) + 2 } / 2
        }
    }

    /**
     * Smooth step interpolation.
     */
    fun smoothStep(edge0: Float, edge1: Float, x: Float): Float {
        val t = clamp((x - edge0) / (edge1 - edge0), 0f, 1f)
        return t * t * (3 - 2 * t)
    }

    /**
     * Smoother step interpolation.
     */
    fun smootherStep(edge0: Float, edge1: Float, x: Float): Float {
        val t = clamp((x - edge0) / (edge1 - edge0), 0f, 1f)
        return t * t * t * (t * (t * 6 - 15) + 10)
    }

    /**
     * Normalize value to 0-1 range.
     */
    fun normalize(value: Float, min: Float, max: Float): Float {
        return if (max > min) clamp((value - min) / (max - min), 0f, 1f) else 0f
    }

    /**
     * Round to decimal places.
     */
    fun round(value: Float, decimalPlaces: Int): Float {
        val multiplier = 10f.pow(decimalPlaces)
        return kotlin.math.round(value * multiplier) / multiplier
    }

    /**
     * Calculate distance between two points.
     */
    fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        return sqrt((x2 - x1).pow(2) + (y2 - y1).pow(2))
    }

    /**
     * Calculate magnitude of vector.
     */
    fun magnitude(x: Float, y: Float): Float {
        return sqrt(x * x + y * y)
    }

    /**
     * Calculate angle between points.
     */
    fun angle(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        return kotlin.math.atan2((y2 - y1).toDouble(), (x2 - x1).toDouble()).toFloat()
    }

    /**
     * Rotate point around origin.
     */
    fun rotate(x: Float, y: Float, angle: Float): Pair<Float, Float> {
        val cos = cos(angle)
        val sin = sin(angle)
        return (x * cos - y * sin) to (x * sin + y * cos)
    }

    /**
     * Wrap value to range.
     */
    fun wrap(value: Float, min: Float, max: Float): Float {
        val range = max - min
        return if (range <= 0) min else {
            var wrapped = value - min
            wrapped -= (wrapped / range).let { kotlin.math.floor(it) } * range
            wrapped + min
        }
    }

    /**
     * Ping-pong (yoyo) value within range.
     */
    fun pingPong(value: Float, min: Float, max: Float): Float {
        val range = max - min
        return if (range <= 0) min else {
            val cycle = (value - min) / range
            val phase = kotlin.math.floor(cycle).toInt()
            val t = cycle - phase
            if (phase % 2 == 0) {
                lerp(min, max, t)
            } else {
                lerp(max, min, t)
            }
        }
    }

    /**
     * Bezier curve calculation.
     */
    fun bezier(t: Float, p0: Float, p1: Float, p2: Float, p3: Float): Float {
        val u = 1 - t
        val tt = t * t
        val uu = u * u
        val uuu = uu * u
        val ttt = tt * t

        return uuu * p0 + 3 * uu * t * p1 + 3 * u * tt * p2 + ttt * p3
    }

    /**
     * Catmull-Rom spline interpolation.
     */
    fun catmullRom(t: Float, p0: Float, p1: Float, p2: Float, p3: Float): Float {
        val t2 = t * t
        val t3 = t2 * t

        return 0.5f * (
            2 * p1 +
            (-p0 + p2) * t +
            (2 * p0 - 5 * p1 + 4 * p2 - p3) * t2 +
            (-p0 + 3 * p1 - 3 * p2 + p3) * t3
        )
    }
}