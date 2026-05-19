package com.app.clipsteronline.upload.editor.timeline.engine

import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sin

/**
 * Physics calculations for timeline animations.
 * Handles fling physics, easing, and momentum.
 */
object TimelinePhysics {

    /**
     * Apply friction to velocity.
     */
    fun applyFriction(velocity: Float, friction: Float = 0.95f): Float {
        return velocity * friction
    }

    /**
     * Calculate stop distance.
     */
    fun calculateStopDistance(velocity: Float, friction: Float = 0.95f): Float {
        var v = velocity
        var distance = 0f

        while (kotlin.math.abs(v) > 1f) {
            v = v * friction
            distance += v
        }

        return distance
    }

    /**
     * Calculate stop time.
     */
    fun calculateStopTime(velocity: Float, friction: Float = 0.95f): Float {
        var v = velocity
        var time = 0f

        while (kotlin.math.abs(v) > 1f) {
            v = v * friction
            time += 16f // 16ms steps
        }

        return time
    }

    /**
     * Ease in quad.
     */
    fun easeInQuad(t: Float): Float = t * t

    /**
     * Ease out quad.
     */
    fun easeOutQuad(t: Float): Float = 1 - (1 - t) * (1 - t)

    /**
     * Ease in out quad.
     */
    fun easeInOutQuad(t: Float): Float {
        return if (t < 0.5f) {
            2 * t * t
        } else {
            1 - (-2 * t + 2).let { it * it } / 2
        }
    }

    /**
     * Ease in cubic.
     */
    fun easeInCubic(t: Float): Float = t * t * t

    /**
     * Ease out cubic.
     */
    fun easeOutCubic(t: Float): Float = 1 - (1 - t).let { it * it * it }

    /**
     * Ease in out cubic.
     */
    fun easeInOutCubic(t: Float): Float {
        return if (t < 0.5f) {
            4 * t * t * t
        } else {
            1 - (-2 * t + 2).let { 4 * it * it * it } / 2
        }
    }

    /**
     * Ease out elastic.
     */
    fun easeOutElastic(t: Float): Float {
        if (t == 0f || t == 1f) return t

        val p = 0.3f
        return 2f.pow(-10 * t) * sin((t - p / 4) * (2 * PI).toFloat() / p) + 1
    }

    /**
     * Ease out bounce.
     */
    fun easeOutBounce(t: Float): Float {
        val n1 = 7.5625f
        val d1 = 2.75f

        return when {
            t < 1 / d1 -> n1 * t * t
            t < 2 / d1 -> {
                t -= 1.5f / d1
                n1 * t * t + 0.75f
            }
            t < 2.5f / d1 -> {
                t -= 2.25f / d1
                n1 * t * t + 0.9375f
            }
            else -> {
                t -= 2.625f / d1
                n1 * t * t + 0.984375f
            }
        }
    }

    /**
     * Linear interpolation.
     */
    fun lerp(start: Float, end: Float, t: Float): Float {
        return start + (end - start) * t.coerceIn(0f, 1f)
    }

    /**
     * Interpolate with ease.
     */
    fun interpolateEase(
        start: Float,
        end: Float,
        t: Float,
        easing: Easing = Easing.EASE_OUT_QUAD
    ): Float {
        return start + (end - start) * easing.apply(t)
    }

    /**
     * Smooth step.
     */
    fun smoothstep(edge0: Float, edge1: Float, x: Float): Float {
        val t = ((x - edge0) / (edge1 - edge0)).coerceIn(0f, 1f)
        return t * t * (3 - 2 * t)
    }

    /**
     * Smoother step (similar to GLSL).
     */
    fun smootherstep(edge0: Float, edge1: Float, x: Float): Float {
        val t = ((x - edge0) / (edge1 - edge0)).coerceIn(0f, 1f)
        return t * t * t * (t * (t * 6 - 15) + 10)
    }

    /**
     * Overshoot with clamp.
     */
    fun overshoot(value: Float, overshoot: Float = 0.1f): Float {
        return value * (1f + overshoot)
    }

    /**
     * Rubber band effect.
     */
    fun rubberband(
        offset: Float,
        maxOffset: Float,
        factor: Float = 0.55f
    ): Float {
        if (offset == 0f) return 0f
        val sign = if (offset > 0) 1f else -1f
        val clamped = kotlin.math.abs(offset).coerceAtMost(maxOffset)
        return sign * clamped * factor * (1 - clamped / maxOffset)
    }

    /**
     * Apply velocity decay.
     */
    fun velocityDecay(velocity: Float, dt: Float, decay: Float = 0.95f): Float {
        return velocity * decay.pow(dt / 16f)
    }

    /**
     * Calculate fling distance.
     */
    fun calculateFlingDistance(velocity: Float): Float {
        var v = velocity
        var distance = 0f
        val dt = 16f

        while (kotlin.math.abs(v) > 10f) {
            distance += v * dt
            v *= 0.95f
            dt += 16f
        }

        return distance
    }
}

/**
 * Easing functions.
 */
sealed class Easing {
    abstract fun apply(t: Float): Float

    object LINEAR : Easing() {
        override fun apply(t: Float): Float = t
    }

    object EASE_IN_QUAD : Easing() {
        override fun apply(t: Float): Float = t * t
    }

    object EASE_OUT_QUAD : Easing() {
        override fun apply(t: Float): Float = 1 - (1 - t) * (1 - t)
    }

    object EASE_IN_OUT_QUAD : Easing() {
        override fun apply(t: Float): Float = if (t < 0.5f) 2 * t * t else 1 - (-2 * t + 2).let { it * it } / 2
    }

    object EASE_IN_CUBIC : Easing() {
        override fun apply(t: Float): Float = t * t * t
    }

    object EASE_OUT_CUBIC : Easing() {
        override fun apply(t: Float): Float = 1 - (1 - t).let { it * it * it }
    }

    object EASE_IN_OUT_CUBIC : Easing() {
        override fun apply(t: Float): Float = if (t < 0.5f) 4 * t * t * t else 1 - (-2 * t + 2).let { 4 * it * it * it } / 2
    }

    object EASE_OUT_ELASTIC : Easing() {
        override fun apply(t: Float): Float = TimelinePhysics.easeOutElastic(t)
    }

    object EASE_OUT_BOUNCE : Easing() {
        override fun apply(t: Float): Float = TimelinePhysics.easeOutBounce(t)
    }
}