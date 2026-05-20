package com.app.clipsteronline.upload.editor.timeline.engine

import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min

/** Stateless helper for damping, clamping and velocity sanitization. */
class TimelinePhysics(
    private val baseFrictionPerSecond: Double = 4.6,
    private val velocityCapPxPerSecond: Double = 18_000.0,
    private val springStiffness: Double = 24.0,
) {
    fun sanitizeVelocity(velocityPxPerSecond: Double): Double {
        if (!velocityPxPerSecond.isFinite()) return 0.0
        return velocityPxPerSecond.coerceIn(-velocityCapPxPerSecond, velocityCapPxPerSecond)
    }

    fun applyFriction(velocityPxPerSecond: Double, frameDeltaMs: Long): Double {
        val dtSec = max(0.0, frameDeltaMs / 1_000.0)
        val damp = exp(-baseFrictionPerSecond * dtSec)
        val output = sanitizeVelocity(velocityPxPerSecond * damp)
        return if (abs(output) < 3.0) 0.0 else output
    }

    fun dampDelta(deltaPx: Double, viewportPx: Double): Double {
        if (viewportPx <= 0.0) return deltaPx
        val normalized = (abs(deltaPx) / viewportPx).coerceIn(0.0, 1.0)
        val dampRatio = 1.0 - (normalized * 0.35)
        return deltaPx * dampRatio
    }

    fun resolveBounds(positionPx: Double, minPx: Double, maxPx: Double): BoundResult {
        val clamped = positionPx.coerceIn(minPx, maxPx)
        val overscroll = positionPx - clamped
        return BoundResult(clamped, overscroll)
    }

    fun springBackVelocity(overscrollPx: Double): Double {
        if (abs(overscrollPx) < 0.5) return 0.0
        return -overscrollPx * springStiffness
    }

    fun blendVelocity(primary: Double, secondary: Double, secondaryWeight: Double = 0.22): Double {
        val weight = secondaryWeight.coerceIn(0.0, 1.0)
        return sanitizeVelocity((primary * (1.0 - weight)) + (secondary * weight))
    }

    fun framePrediction(positionPx: Double, velocityPxPerSecond: Double, frameDeltaMs: Long): Double {
        val dtSec = min(0.05, max(0.0, frameDeltaMs / 1_000.0))
        return positionPx + (sanitizeVelocity(velocityPxPerSecond) * dtSec)
    }
}

data class BoundResult(
    val clampedPositionPx: Double,
    val overscrollPx: Double,
)
