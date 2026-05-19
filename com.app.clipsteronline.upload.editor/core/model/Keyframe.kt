package com.app.clipsteronline.upload.editor.core.model

/**
 * Keyframe model for animated properties.
 * Contains timeline position, value, and interpolation settings.
 */
data class Keyframe(
    val id: String,
    val timeMs: Long,
    val value: Float,
    val interpolation: Interpolation = Interpolation.LINEAR,
    val easeInStrength: Float = 0f,
    val easeOutStrength: Float = 0f,
    val bezierControlPoints: List<Float> = emptyList()
) {

    fun withValue(value: Float): Keyframe {
        return copy(value = value)
    }

    fun withInterpolation(interpolation: Interpolation): Keyframe {
        return copy(interpolation = interpolation)
    }

    fun withEaseStrength(easeIn: Float, easeOut: Float): Keyframe {
        return copy(easeInStrength = easeIn, easeOutStrength = easeOut)
    }

    fun withBezier(points: List<Float>): Keyframe {
        return copy(bezierControlPoints = points)
    }
}

/**
 * Interpolation types for keyframe animation.
 */
enum class Interpolation(
    val displayName: String,
    val hasControlPoints: Boolean
) {
    LINEAR("Linear", false),
    EASE_IN("Ease In", false),
    EASE_OUT("Ease Out", false),
    EASE_IN_OUT("Ease In/Out", false),
    BEZIER("Bezier", true),
    STEPPED("Stepped", false),
    CUBIC_BEZIER("Cubic Bezier", true);

    fun interpolate(startValue: Float, endValue: Float, progress: Float): Float {
        return when (this) {
            LINEAR -> lerp(startValue, endValue, progress)
            EASE_IN -> lerp(startValue, endValue, easeIn(progress))
            EASE_OUT -> lerp(startValue, endValue, easeOut(progress))
            EASE_IN_OUT -> lerp(startValue, endValue, easeInOut(progress))
            BEZIER, CUBIC_BEZIER -> {
                if (bezierControlPoints.size >= 4) {
                    bezierInterpolate(startValue, endValue, progress)
                } else {
                    lerp(startValue, endValue, progress)
                }
            }
            STEPPED -> startValue
        }
    }

    private fun lerp(start: Float, end: Float, t: Float): Float {
        return start + (end - start) * t
    }

    private fun easeIn(t: Float): Float {
        return t * t
    }

    private fun easeOut(t: Float): Float {
        return 1 - (1 - t) * (1 - t)
    }

    private fun easeInOut(t: Float): Float {
        return if (t < 0.5f) 2 * t * t else 1 - 2 * (1 - t) * (1 - t)
    }

    private fun bezierInterpolate(start: Float, end: Float, t: Float): Float {
        // Simplified bezier interpolation
        val adjustedT = bezierCurve(t, bezierControlPoints[0], bezierControlPoints[1])
        return lerp(start, end, adjustedT)
    }

    private fun bezierCurve(t: Float, cp1: Float, cp2: Float): Float {
        // Approximate bezier curve using cubic interpolation
        val t2 = t * t
        val t3 = t2 * t
        return (3 * cp1 - 3 * cp1 * t + t3 - 3 * cp2 + 3 * cp2 * t - t3 + t) /
            (3 * cp1 - 3 * cp2 + 1)
    }
}

/**
 * Keyframe animation curve.
 */
data class KeyframeCurve(
    val propertyName: String,
    val keyframes: List<Keyframe>
) {
    fun getValueAt(timeMs: Long, defaultValue: Float): Float {
        if (keyframes.isEmpty()) return defaultValue

        val sorted = keyframes.sortedBy { it.timeMs }

        // Before first keyframe
        if (timeMs <= sorted.first().timeMs) {
            return sorted.first().value
        }

        // After last keyframe
        if (timeMs >= sorted.last().timeMs) {
            return sorted.last().value
        }

        // Between keyframes
        for (i in 0 until sorted.size - 1) {
            val current = sorted[i]
            val next = sorted[i + 1]

            if (timeMs >= current.timeMs && timeMs < next.timeMs) {
                val progress = (timeMs - current.timeMs).toFloat() /
                    (next.timeMs - current.timeMs)
                return current.interpolation.interpolate(
                    current.value,
                    next.value,
                    progress
                )
            }
        }

        return defaultValue
    }

    fun addKeyframe(keyframe: Keyframe): KeyframeCurve {
        return copy(keyframes = keyframes + keyframe)
    }

    fun removeKeyframe(keyframeId: String): KeyframeCurve {
        return copy(keyframes = keyframes.filter { it.id != keyframeId })
    }

    fun updateKeyframe(keyframeId: String, update: (Keyframe) -> Keyframe): KeyframeCurve {
        return copy(keyframes = keyframes.map {
            if (it.id == keyframeId) update(it) else it
        })
    }

    companion object {
        fun create(propertyName: String): KeyframeCurve {
            return KeyframeCurve(propertyName, emptyList())
        }

        fun fromList(propertyName: String, keyframes: List<Keyframe>): KeyframeCurve {
            return KeyframeCurve(propertyName, keyframes.sortedBy { it.timeMs })
        }
    }
}

/**
 * Animated property types.
 */
enum class AnimatedProperty(val displayName: String) {
    POSITION_X("Position X"),
    POSITION_Y("Position Y"),
    SCALE_X("Scale X"),
    SCALE_Y("Scale Y"),
    ROTATION("Rotation"),
    OPACITY("Opacity"),
    VOLUME("Volume"),
    BRIGHTNESS("Brightness"),
    CONTRAST("Contrast"),
    SATURATION("Saturation"),
    FILTER_INTENSITY("Filter Intensity"),
    TEXT_SIZE("Text Size"),
    TEXT_ALPHA("Text Alpha")
}