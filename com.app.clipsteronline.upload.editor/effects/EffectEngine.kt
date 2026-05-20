package com.app.clipsteronline.upload.editor.effects

class EffectEngine(
    private val blurEffect: BlurEffect = BlurEffect(),
    private val glitchEffect: GlitchEffect = GlitchEffect(),
    private val vhsRetroEffect: VHSRetroEffect = VHSRetroEffect(),
    private val beautyEffect: BeautyEffect = BeautyEffect(),
) {
    fun configure() = Unit

    fun buildRenderPasses(requests: List<EffectRequest>, timeMs: Long): List<EffectPass> {
        if (requests.isEmpty()) return emptyList()
        return requests.filter { it.enabled }
            .sortedBy { it.order }
            .map { request ->
                val processor = when (request.type) {
                    EffectType.BLUR -> blurEffect
                    EffectType.GLITCH -> glitchEffect
                    EffectType.VHS_RETRO -> vhsRetroEffect
                    EffectType.BEAUTY -> beautyEffect
                }
                val uniformMap = processor.uniformsFor(request, timeMs)
                EffectPass(request.id, request.type, uniformMap, request.intensity.coerceIn(0f, 1f))
            }
    }

    data class EffectRequest(
        val id: String,
        val type: EffectType,
        val intensity: Float,
        val speed: Float = 1f,
        val enabled: Boolean = true,
        val order: Int = 0,
        val params: Map<String, Float> = emptyMap(),
    )

    data class EffectPass(
        val id: String,
        val type: EffectType,
        val uniforms: Map<String, Float>,
        val intensity: Float,
    )

    enum class EffectType {
        BLUR,
        GLITCH,
        VHS_RETRO,
        BEAUTY,
    }
}

interface GpuEffectProcessor {
    fun uniformsFor(request: EffectEngine.EffectRequest, timeMs: Long): Map<String, Float>
}
