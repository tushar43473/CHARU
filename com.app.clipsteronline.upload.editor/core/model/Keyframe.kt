package com.app.clipsteronline.upload.editor.core.model

data class Keyframe(
    val timeMs: Long,
    val value: Float,
    val interpolation: Interpolation = Interpolation.LINEAR,
    val bezierControl: BezierControl? = null,
) {
    init {
        require(timeMs >= 0) { "timeMs must be >= 0" }
        require(!value.isNaN() && !value.isInfinite()) { "value must be finite" }
        require(interpolation != Interpolation.CUBIC_BEZIER || bezierControl != null) {
            "bezierControl is required for cubic-bezier interpolation"
        }
    }

    enum class Interpolation { STEP, LINEAR, EASE_IN, EASE_OUT, EASE_IN_OUT, CUBIC_BEZIER }

    data class BezierControl(
        val x1: Float,
        val y1: Float,
        val x2: Float,
        val y2: Float,
    ) {
        init {
            require(x1 in 0f..1f && x2 in 0f..1f) { "bezier x controls must be in 0..1" }
        }
    }
}
