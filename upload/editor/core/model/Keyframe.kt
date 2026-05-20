package upload.editor.core.model

data class Keyframe(
    val timeMs: Long,
    val value: Double,
    val interpolation: Interpolation = Interpolation.LINEAR,
    val tangentIn: Double? = null,
    val tangentOut: Double? = null,
) {
    init {
        require(timeMs >= 0) { "timeMs must be non-negative" }
        require(!value.isNaN() && !value.isInfinite()) { "value must be finite" }
    }

    enum class Interpolation {
        STEP,
        LINEAR,
        EASE_IN,
        EASE_OUT,
        EASE_IN_OUT,
        CUBIC_BEZIER,
    }
}
