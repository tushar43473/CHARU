package com.app.clipsteronline.upload.editor.core.model

import kotlin.math.PI
import kotlin.math.cos

/**
 * Describes a clip-edge transition and its runtime controls.
 */
data class TransitionModel(
    val id: String,
    val type: Type,
    val durationMs: Long,
    val easing: Easing = Easing.EASE_IN_OUT,
    val direction: Direction = Direction.RIGHT,
    val speed: Float = 1f,
    val intensity: Float = 1f,
    val parameters: Map<String, Float> = emptyMap(),
    val gpuCompatible: Boolean = true,
) {
    init {
        require(id.isNotBlank())
        require(durationMs in MIN_DURATION_MS..MAX_DURATION_MS)
        require(speed in 0.1f..4f)
        require(intensity in 0f..2f)
        require(parameters.values.none { it.isNaN() || it.isInfinite() })
    }

    fun progressAt(elapsedMs: Long): Float {
        if (durationMs <= 0L) return 1f
        val scaled = (elapsedMs.coerceAtLeast(0L).toFloat() / durationMs) * speed
        val linear = scaled.coerceIn(0f, 1f)
        return easing.transform(linear)
    }

    enum class Type {
        CUT,
        FADE,
        DISSOLVE,
        SLIDE,
        ZOOM,
        BLUR,
        GLITCH,
        CINEMATIC_WIPE,
        CINEMATIC_FLASH
    }

    enum class Direction { LEFT, RIGHT, UP, DOWN, IN, OUT }

    enum class Easing {
        LINEAR,
        EASE_IN,
        EASE_OUT,
        EASE_IN_OUT,
        SMOOTH_STEP,
        CINEMATIC;

        fun transform(raw: Float): Float {
            val t = raw.coerceIn(0f, 1f)
            return when (this) {
                LINEAR -> t
                EASE_IN -> t * t
                EASE_OUT -> 1f - (1f - t) * (1f - t)
                EASE_IN_OUT -> if (t < 0.5f) 2f * t * t else 1f - ((-2f * t + 2f) * (-2f * t + 2f)) / 2f
                SMOOTH_STEP -> t * t * (3f - 2f * t)
                CINEMATIC -> ((1f - cos((t * PI).toFloat())) / 2f)
            }
        }
    }

    companion object {
        const val MIN_DURATION_MS = 50L
        const val MAX_DURATION_MS = 5_000L
    }
}
