package upload.editor.core.model

data class TransitionModel(
    val id: String,
    val type: Type,
    val durationMs: Long,
    val easing: Easing = Easing.EASE_IN_OUT,
    val params: Map<String, Double> = emptyMap(),
) {
    init {
        require(id.isNotBlank())
        require(durationMs in 50..5000)
        require(params.values.none { it.isNaN() || it.isInfinite() })
    }

    enum class Type { CUT, DISSOLVE, FADE, SLIDE, ZOOM, BLUR, SPIN, WIPE }

    enum class Easing { LINEAR, EASE_IN, EASE_OUT, EASE_IN_OUT }
}
