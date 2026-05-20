package upload.editor.core.model

data class EffectClip(
    override val id: String,
    override val startTimeMs: Long,
    override val endTimeMs: Long,
    override val zIndex: Int = 0,
    val effectType: EffectType,
    val intensity: Float,
    val blendMode: BlendMode = BlendMode.NORMAL,
    val parameters: Map<String, Double> = emptyMap(),
) : TimelineTrack.TimelineClip {
    init {
        require(id.isNotBlank())
        require(startTimeMs >= 0 && endTimeMs >= startTimeMs)
        require(intensity in 0f..1f)
        require(parameters.values.none { it.isNaN() || it.isInfinite() })
    }

    enum class EffectType { FILTER, TRANSITION, BEAUTY, BLUR, GLITCH, VHS, LUT, CUSTOM }

    enum class BlendMode { NORMAL, ADD, MULTIPLY, SCREEN, OVERLAY, SOFT_LIGHT }
}
