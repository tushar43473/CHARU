package com.app.clipsteronline.upload.editor.core.model

data class TransitionModel(
    val id: String,
    val type: Type,
    val durationMs: Long,
    val easing: Easing = Easing.EASE_IN_OUT,
    val parameters: Map<String, Float> = emptyMap(),
    val gpuCompatible: Boolean = true,
) {
    init {
        require(id.isNotBlank())
        require(durationMs in 50..5000)
        require(parameters.values.none { it.isNaN() || it.isInfinite() })
    }

    enum class Type { CUT, DISSOLVE, FADE, SLIDE, ZOOM, BLUR, WIPE, SPIN }
    enum class Easing { LINEAR, EASE_IN, EASE_OUT, EASE_IN_OUT }
}
