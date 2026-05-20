package com.app.clipsteronline.upload.editor.effects

import kotlin.math.sin

class GlitchEffect : GpuEffectProcessor {
    fun configure() = Unit

    override fun uniformsFor(request: EffectEngine.EffectRequest, timeMs: Long): Map<String, Float> {
        val speed = request.speed.coerceIn(0.2f, 4f)
        val t = (timeMs / 1000f) * speed
        val jitter = (sin(t * 9f) * 0.5f + 0.5f).toFloat()
        return mapOf(
            "uIntensity" to request.intensity,
            "uRgbSplit" to ((request.params["rgbSplit"] ?: 0.01f) * request.intensity),
            "uNoiseAmount" to ((request.params["noise"] ?: 0.2f) * request.intensity),
            "uScanline" to (request.params["scanline"] ?: 0.12f),
            "uGlitchJitter" to jitter,
            "uTime" to t,
        )
    }
}
