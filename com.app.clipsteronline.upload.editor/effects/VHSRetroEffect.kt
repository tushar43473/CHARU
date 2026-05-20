package com.app.clipsteronline.upload.editor.effects

import kotlin.math.sin

class VHSRetroEffect : GpuEffectProcessor {
    fun configure() = Unit

    override fun uniformsFor(request: EffectEngine.EffectRequest, timeMs: Long): Map<String, Float> {
        val t = timeMs / 1000f
        val tracking = ((sin(t * 2.5f) + 1f) * 0.5f).toFloat()
        return mapOf(
            "uIntensity" to request.intensity,
            "uGrain" to ((request.params["grain"] ?: 0.18f) * request.intensity),
            "uChromaticAberration" to ((request.params["aberration"] ?: 0.015f) * request.intensity),
            "uTracking" to tracking,
            "uScanlineDensity" to (request.params["scanlineDensity"] ?: 320f),
            "uTime" to t,
        )
    }
}
