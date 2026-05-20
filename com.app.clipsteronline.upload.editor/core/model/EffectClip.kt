package com.app.clipsteronline.upload.editor.core.model

data class EffectClip(
    override val clipId: String,
    override val startMs: Long,
    override val endMs: Long,
    override val layer: Int,
    val effectType: EffectType,
    val intensity: Float,
    val blendMode: BlendMode = BlendMode.NORMAL,
    val parameters: Map<String, Float> = emptyMap(),
    val shaderRef: String? = null,
) : TimelineTrack.TimelineClip {
    init {
        require(clipId.isNotBlank())
        require(startMs >= 0 && endMs >= startMs)
        require(intensity in 0f..1f)
        require(parameters.values.none { it.isNaN() || it.isInfinite() })
    }

    enum class EffectType { FILTER, TRANSITION, BEAUTY, BLUR, GLITCH, LUT, CUSTOM }
    enum class BlendMode { NORMAL, ADD, MULTIPLY, SCREEN, OVERLAY, SOFT_LIGHT }
}
