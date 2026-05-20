package com.app.clipsteronline.upload.editor.effects

class BeautyEffect : GpuEffectProcessor {
    fun configure() = Unit

    override fun uniformsFor(request: EffectEngine.EffectRequest, timeMs: Long): Map<String, Float> {
        return mapOf(
            "uIntensity" to request.intensity,
            "uSmooth" to ((request.params["smooth"] ?: 0.65f) * request.intensity),
            "uTone" to (request.params["tone"] ?: 0.1f),
            "uBrightness" to (request.params["brightness"] ?: 0.05f),
            "uSaturation" to (request.params["saturation"] ?: 0.08f),
        )
    }
}
