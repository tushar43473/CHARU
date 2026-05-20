package com.app.clipsteronline.upload.editor.effects

import kotlin.math.abs
import kotlin.math.max

class BlurEffect : GpuEffectProcessor {
    fun configure() = Unit

    override fun uniformsFor(request: EffectEngine.EffectRequest, timeMs: Long): Map<String, Float> {
        val baseRadius = max(0.5f, request.params["radius"] ?: 4f)
        val directional = request.params["directional"] ?: 0f
        val zoom = request.params["zoom"] ?: 0f
        val pulse = 0.85f + 0.15f * abs(kotlin.math.sin(timeMs / 240.0)).toFloat()
        return mapOf(
            "uIntensity" to request.intensity,
            "uBlurRadius" to (baseRadius * request.intensity * pulse),
            "uDirectional" to directional,
            "uZoomBlur" to zoom,
        )
    }
}
