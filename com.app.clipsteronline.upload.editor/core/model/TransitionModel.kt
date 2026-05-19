package com.app.clipsteronline.upload.editor.core.model

/**
 * Transition model for clip transitions.
 * Contains transition type, duration, and parameters.
 */
data class TransitionModel(
    val id: String,
    val type: TransitionType,
    val durationMs: Long = 500L,
    val easing: Easing = Easing.EASE_IN_OUT,
    val parameters: TransitionParameters = TransitionParameters(),
    val reverseDirection: Boolean = false
) {

    fun withDuration(durationMs: Long): TransitionModel {
        return copy(durationMs = durationMs.coerceIn(100L, 3000L))
    }

    fun withEasing(easing: Easing): TransitionModel {
        return copy(easing = easing)
    }

    fun withParameters(params: TransitionParameters): TransitionModel {
        return copy(parameters = params)
    }

    fun reversed(): TransitionModel {
        return copy(reverseDirection = !reverseDirection)
    }
}

/**
 * Transition types.
 */
enum class TransitionType(
    val displayName: String,
    val category: TransitionCategory
) {
    // None
    NONE("None", TransitionCategory.NONE),

    // Fade
    FADE("Fade", TransitionCategory.FADE),
    FADE_TO_WHITE("Fade to White", TransitionCategory.FADE),
    FADE_TO_BLACK("Fade to Black", TransitionCategory.FADE),

    // Dissolve
    CROSS_DISSOLVE("Cross Dissolve", TransitionCategory.DISSOLVE),
    DISSOLVE("Dissolve", TransitionCategory.DISSOLVE),

    // Wipe
    WIPE_LEFT("Wipe Left", TransitionCategory.WIPE),
    WIPE_RIGHT("Wipe Right", TransitionCategory.WIPE),
    WIPE_UP("Wipe Up", TransitionCategory.WIPE),
    WIPE_DOWN("Wipe Down", TransitionCategory.WIPE),

    // Slide
    SLIDE_LEFT("Slide Left", TransitionCategory.SLIDE),
    SLIDE_RIGHT("Slide Right", TransitionCategory.SLIDE),
    SLIDE_UP("Slide Up", TransitionCategory.SLIDE),
    SLIDE_DOWN("Slide Down", TransitionCategory.SLIDE),

    // Push
    PUSH_LEFT("Push Left", TransitionCategory.PUSH),
    PUSH_RIGHT("Push Right", TransitionCategory.PUSH),

    // Zoom
    ZOOM_IN("Zoom In", TransitionCategory.ZOOM),
    ZOOM_OUT("Zoom Out", TransitionCategory.ZOOM),
    ZOOM_BLUR("Zoom Blur", TransitionCategory.ZOOM),

    // Spin
    SPIN_LEFT("Spin Left", TransitionCategory.SPIN),
    SPIN_RIGHT("Spin Right", TransitionCategory.SPIN),

    // Custom
    CUSTOM("Custom", TransitionCategory.CUSTOM);

    fun isNoTransition(): Boolean = this == NONE
    fun isFade(): Boolean = category == TransitionCategory.FADE
    fun isDissolve(): Boolean = category == TransitionCategory.DISSOLVE
    fun isWipe(): Boolean = category == TransitionCategory.WIPE

    companion object {
        val DEFAULT = NONE
        val COMMON = listOf(
            NONE,
            CROSS_DISSOLVE,
            FADE_TO_BLACK,
            WIPE_LEFT,
            WIPE_RIGHT,
            SLIDE_LEFT,
            SLIDE_RIGHT,
            ZOOM_IN,
            ZOOM_OUT
        )
    }
}

/**
 * Transition categories.
 */
enum class TransitionCategory {
    NONE,
    FADE,
    DISSOLVE,
    WIPE,
    SLIDE,
    PUSH,
    ZOOM,
    SPIN,
    CUSTOM
}

/**
 * Transition parameters.
 */
data class TransitionParameters(
    val values: Map<String, Float> = emptyMap()
) {
    fun get(key: String): Float? = values[key]

    fun set(key: String, value: Float): TransitionParameters {
        return copy(values = values + (key to value))
    }

    companion object {
        val EMPTY = TransitionParameters()

        fun direction(angle: Float) = TransitionParameters(mapOf("direction" to angle))
        fun strength(value: Float) = TransitionParameters(mapOf("strength" to value))
        fun blur(value: Float) = TransitionParameters(mapOf("blur_radius" to value))
    }
}

/**
 * Transition presets.
 */
object TransitionPresets {
    val DEFAULT_DURATION_MS = 500L
    val MIN_DURATION_MS = 100L
    val MAX_DURATION_MS = 3000L

    val QUICK_PRESETS = listOf(
        TransitionModel("quick_dissolve", TransitionType.CROSS_DISSOLVE, 250L),
        TransitionModel("quick_fade", TransitionType.FADE, 250L),
        TransitionModel("quick_wipe", TransitionType.WIPE_LEFT, 250L)
    )

    val NORMAL_PRESETS = listOf(
        TransitionModel("normal_dissolve", TransitionType.CROSS_DISSOLVE, 500L),
        TransitionModel("normal_fade", TransitionType.FADE, 500L),
        TransitionModel("normal_wipe", TransitionType.WIPE_LEFT, 500L)
    )

    val SLOW_PRESETS = listOf(
        TransitionModel("slow_dissolve", TransitionType.CROSS_DISSOLVE, 1000L),
        TransitionModel("slow_fade", TransitionType.FADE, 1000L),
        TransitionModel("slow_wipe", TransitionType.WIPE_LEFT, 1000L)
    )
}