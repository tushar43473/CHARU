package com.app.clipsteronline.upload.editor.core.model

/**
 * Filter model for video/image filters.
 * Contains all filter parameters and intensity settings.
 */
data class FilterModel(
    val id: String,
    val type: FilterType,
    val intensity: Float = 1f,
    val parameters: FilterParameters = FilterParameters(),
    val isEnabled: Boolean = true,
    val keyframes: List<Keyframe> = emptyList()
) {

    fun withIntensity(intensity: Float): FilterModel {
        return copy(intensity = intensity.coerceIn(0f, 1f))
    }

    fun withParameters(params: FilterParameters): FilterModel {
        return copy(parameters = params)
    }

    fun enabled(): FilterModel {
        return copy(isEnabled = true)
    }

    fun disabled(): FilterModel {
        return copy(isEnabled = false)
    }

    fun toggle(): FilterModel {
        return copy(isEnabled = !isEnabled)
    }

    fun calcValueAt(timeMs: Long, paramName: String): Float {
        val baseValue = parameters.get(paramName) ?: return 0f

        // Check keyframes for animation
        val keyframe = keyframes.findLast { it.timeMs <= timeMs }
        return keyframe?.value ?: baseValue * intensity
    }
}

/**
 * Filter types.
 */
sealed class FilterType(val displayName: String) {
    // Color adjustments
    data object Brightness : FilterType("Brightness")
    data object Contrast : FilterType("Contrast")
    data object Saturation : FilterType("Saturation")
    data object Temperature : FilterType("Temperature")
    data object Tint : FilterType("Tint")
    data object Vibrance : FilterType("Vibrance")

    // Color grading
    data class LUT(val lutId: String) : FilterType("LUT")
    data object Vintage : FilterType("Vintage")
    data object Fade : FilterType("Fade")
    data object Noir : FilterType("Noir")

    // Stylized
    data object Vignette : FilterType("Vignette")
    data object Grain : FilterType("Grain")
    data object ChromaticAberration : FilterType("Chromatic Aberration")
    data object Glitch : FilterType("Glitch")
    data object Scanline : FilterType("Scanline")

    // Blur effects
    data object GaussianBlur : FilterType("Blur")
    data object RadialBlur : FilterType("Radial Blur")

    // Enhance
    data object Sharpen : FilterType("Sharpen")
    data object AutoEnhance : FilterType("Auto Enhance")

    // Preset filters
    data class Preset(val presetId: String) : FilterType("Preset")

    companion object {
        val DEFAULT_FILTERS = listOf(
            Brightness, Contrast, Saturation, Temperature,
            Vignette, Sharpen, Vintage
        )
    }
}

/**
 * Filter parameters.
 */
data class FilterParameters(
    val values: Map<String, Float> = emptyMap()
) {
    fun get(key: String): Float? = values[key]

    fun set(key: String, value: Float): FilterParameters {
        return copy(values = values + (key to value))
    }

    companion object {
        val EMPTY = FilterParameters()

        fun brightness(value: Float) = FilterParameters(mapOf("brightness" to value))
        fun contrast(value: Float) = FilterParameters(mapOf("contrast" to value))
        fun saturation(value: Float) = FilterParameters(mapOf("saturation" to value))
        fun temperature(value: Float) = FilterParameters(mapOf("temperature" to value))
        fun vignette(intensity: Float, radius: Float = 1f) = FilterParameters(
            mapOf("intensity" to intensity, "radius" to radius)
        )
    }
}

/**
 * Preset filter collections.
 */
object FilterPresets {
    val LUT_PACKS = mapOf(
        "cinematic" to listOf("teal_orange", "blue_orange", "gold"),
        "vintage" to listOf("faded", "70s", "old_movie"),
        "moody" to listOf("dark_moody", "misty", "foggy"),
        "portrait" to listOf("soft", "warm", "cool")
    )

    fun getPresetById(id: String): FilterModel? {
        return when (id) {
            "brightness_boost" -> FilterModel(
                id = id,
                type = FilterType.Brightness,
                parameters = FilterParameters.brightness(0.1f)
            )
            "high_contrast" -> FilterModel(
                id = id,
                type = FilterType.Contrast,
                parameters = FilterParameters.contrast(1.3f)
            )
            "warm_glow" -> FilterModel(
                id = id,
                type = FilterType.Temperature,
                parameters = FilterParameters.temperature(0.15f)
            )
            "cool_blue" -> FilterModel(
                id = id,
                type = FilterType.Temperature,
                parameters = FilterParameters.temperature(-0.15f)
            )
            "vintage_faded" -> FilterModel(
                id = id,
                type = FilterType.Vintage,
                parameters = FilterParameters(mapOf(
                    "contrast" to 0.9f,
                    "saturation" to 0.7f,
                    "brightness" to 0.05f
                ))
            )
            else -> null
        }
    }
}