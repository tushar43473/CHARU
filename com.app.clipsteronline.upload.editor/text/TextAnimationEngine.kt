package com.app.clipsteronline.upload.editor.text

import com.app.clipsteronline.upload.editor.core.model.TextClip

class TextAnimationEngine {
    fun configure() = Unit

    fun evaluate(clip: TextClip, timeMs: Long): AnimationFrame {
        val duration = (clip.endMs - clip.startMs).coerceAtLeast(1L)
        val local = (timeMs - clip.startMs).coerceIn(0L, duration)
        val progress = local.toFloat() / duration
        val animation = clip.animation

        var alpha = 1f
        var scale = 1f
        var offsetY = 0f

        if (animation != null) {
            if (animation.inPreset != null && animation.inDurationMs > 0 && local <= animation.inDurationMs) {
                val p = (local.toFloat() / animation.inDurationMs).coerceIn(0f, 1f)
                applyPreset(animation.inPreset, p, entering = true).also {
                    alpha *= it.alpha
                    scale *= it.scale
                    offsetY += it.offsetY
                }
            }
            val outStart = duration - animation.outDurationMs
            if (animation.outPreset != null && animation.outDurationMs > 0 && local >= outStart) {
                val p = ((local - outStart).toFloat() / animation.outDurationMs).coerceIn(0f, 1f)
                applyPreset(animation.outPreset, p, entering = false).also {
                    alpha *= it.alpha
                    scale *= it.scale
                    offsetY += it.offsetY
                }
            }
        }
        return AnimationFrame(alpha.coerceIn(0f, 1f), scale.coerceIn(0.5f, 2f), offsetY, progress)
    }

    private fun applyPreset(preset: TextClip.TextAnimation.Preset, p: Float, entering: Boolean): AnimationFrame {
        return when (preset) {
            TextClip.TextAnimation.Preset.FADE -> AnimationFrame(if (entering) p else 1f - p, 1f, 0f, p)
            TextClip.TextAnimation.Preset.SLIDE_UP -> AnimationFrame(if (entering) p else 1f - p, 1f, (1f - p) * 0.08f, p)
            TextClip.TextAnimation.Preset.SLIDE_DOWN -> AnimationFrame(if (entering) p else 1f - p, 1f, -(1f - p) * 0.08f, p)
            TextClip.TextAnimation.Preset.SCALE -> AnimationFrame(if (entering) p else 1f - p, 0.8f + p * 0.2f, 0f, p)
            TextClip.TextAnimation.Preset.TYPEWRITER -> AnimationFrame(1f, 1f, 0f, p)
        }
    }

    data class AnimationFrame(val alpha: Float, val scale: Float, val offsetY: Float, val progress: Float)
}
