package com.app.clipsteronline.upload.editor.core.model

/**
 * Represents an effect clip in the timeline.
 * Contains effect properties like color grading, adjustment, and blur effects.
 */
data class EffectClip(
    val id: String,
    val effectType: EffectType,
    val timelineStartMs: Long,
    val timelineEndMs: Long,
    val intensity: Float = 1f,
    val blendMode: BlendMode = BlendMode.NORMAL,
    val parameters: EffectParameters = EffectParameters(),
    val keyframes: List<Keyframe> = emptyList()
) : Clip {

    val durationMs: Long
        get() = timelineEndMs - timelineStartMs

    fun getPlaybackRange(): LongRange {
        return timelineStartMs until timelineEndMs
    }

    fun containsTime(timeMs: Long): Boolean {
        return timeMs >= timelineStartMs && timeMs < timelineEndMs
    }

    fun withIntensity(intensity: Float): EffectClip {
        return copy(intensity = intensity.coerceIn(0f, 1f))
    }

    fun withParameters(params: EffectParameters): EffectClip {
        return copy(parameters = params)
    }

    fun updateParameter(key: String, value: Float): EffectClip {
        return copy(parameters = parameters.set(key, value))
    }

    fun calcIntensityAt(timeMs: Long): Float {
        val progress = if (durationMs > 0) {
            (timeMs - timelineStartMs).toFloat() / durationMs
        } else 0f

        // Check if there's keyframe animation
        val keyframe = keyframes.findLast { it.timeMs <= timeMs }
        return keyframe?.value ?: intensity * progress.coerceIn(0f, 1f)
    }
}

/**
 * Types of video effects.
 */
sealed class EffectType(val displayName: String, val category: EffectCategory) {
    // Color grading
    data class LUT(val lutId: String) : EffectType("LUT", EffectCategory.COLOR)
    data object Brightness : EffectType("Brightness", EffectCategory.COLOR)
    data object Contrast : EffectType("Contrast", EffectCategory.COLOR)
    data object Saturation : EffectType("Saturation", EffectCategory.COLOR)
    data object Temperature : EffectType("Temperature", EffectCategory.COLOR)
    data object Tint : EffectType("Tint", EffectCategory.COLOR)
    data object Highlights : EffectType("Highlights", EffectCategory.COLOR)
    data object Shadows : EffectType("Shadows", EffectCategory.COLOR)
    data object Vibrance : EffectType("Vibrance", EffectCategory.COLOR)

    // Blur/Sharpening
    data object GaussianBlur : EffectType("Gaussian Blur", EffectCategory.BLUR)
    data object RadialBlur : EffectType("Radial Blur", EffectCategory.BLUR)
    data object MotionBlur : EffectType("Motion Blur", EffectCategory.BLUR)
    data object Sharpen : EffectType("Sharpen", EffectCategory.ENHANCE)
    data object UnsharpMask : EffectType("Unsharp Mask", EffectCategory.ENHANCE)

    // Style effects
    data object Vintage : EffectType("Vintage", EffectCategory.STYLE)
    data object Noir : EffectType("Noir", EffectCategory.STYLE)
    data object Fade : EffectType("Fade", EffectCategory.STYLE)
    data object ChromaticAberration : EffectType("Chromatic Aberration", EffectCategory.STYLE)
    data object Vignette : EffectType("Vignette", EffectCategory.STYLE)
    data object Grain : EffectType("Grain", EffectCategory.STYLE)
    data object Scanline : EffectType("Scanline", EffectCategory.STYLE)

    // Distortion
    data object Fisheye : EffectType("Fisheye", EffectCategory.DISTORTION)
    data object Spherize : EffectType("Spherize", EffectCategory.DISTORTION)
    data object Mirror : EffectType("Mirror", EffectCategory.DISTORTION)
    data object Flip : EffectType("Flip", EffectCategory.DISTORTION)

    // Color key
    data class ChromaKey(val color: Int, val tolerance: Float = 30f) : EffectType("Chroma Key", EffectCategory.COLOR_KEY)
}

/**
 * Categories of effects.
 */
enum class EffectCategory {
    COLOR,
    BLUR,
    STYLE,
    ENHANCE,
    DISTORTION,
    COLOR_KEY
}

/**
 * Parameters for effect configuration.
 */
data class EffectParameters(
    val values: Map<String, Float> = emptyMap()
) {
    fun get(key: String): Float? = values[key]

    fun set(key: String, value: Float): EffectParameters {
        return copy(values = values + (key to value))
    }

    companion object {
        val EMPTY = EffectParameters()

        fun brightness(value: Float) = EffectParameters(mapOf("brightness" to value))
        fun contrast(value: Float) = EffectParameters(mapOf("contrast" to value))
        fun saturation(value: Float) = EffectParameters(mapOf("saturation" to value))
        fun temperature(value: Float) = EffectParameters(mapOf("temperature" to value))
        fun blurRadius(value: Float) = EffectParameters(mapOf("radius" to value))
    }
}