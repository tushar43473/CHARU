package com.app.clipsteronline.upload.editor.sticker

import com.app.clipsteronline.upload.editor.core.model.StickerClip
import kotlin.math.sin

class AnimatedSticker {
    fun configure() = Unit

    fun evaluate(clip: StickerClip, timeMs: Long): AnimationState {
        val animation = clip.animation ?: return AnimationState(clip.scale, clip.rotationDegrees, clip.opacity)
        val duration = (clip.endMs - clip.startMs).coerceAtLeast(1L)
        val elapsed = (timeMs - clip.startMs).coerceAtLeast(0L)
        val phase = if (animation.loop) (elapsed % duration).toFloat() / duration else (elapsed.toFloat() / duration).coerceIn(0f, 1f)
        val t = (phase * animation.speed).coerceIn(0f, 4f)

        return when (animation.preset) {
            StickerClip.StickerAnimation.Preset.BOUNCE -> AnimationState(clip.scale * (1f + 0.12f * sin((t * 6.28f).toDouble()).toFloat()), clip.rotationDegrees, clip.opacity)
            StickerClip.StickerAnimation.Preset.POP -> AnimationState(clip.scale * (0.85f + 0.15f * t.coerceIn(0f, 1f)), clip.rotationDegrees, clip.opacity)
            StickerClip.StickerAnimation.Preset.WIGGLE -> AnimationState(clip.scale, clip.rotationDegrees + (sin((t * 12f).toDouble()) * 8f).toFloat(), clip.opacity)
            StickerClip.StickerAnimation.Preset.ROTATE -> AnimationState(clip.scale, clip.rotationDegrees + t * 360f, clip.opacity)
            StickerClip.StickerAnimation.Preset.PULSE -> AnimationState(clip.scale * (1f + 0.08f * sin((t * 10f).toDouble()).toFloat()), clip.rotationDegrees, clip.opacity)
        }
    }

    data class AnimationState(
        val scale: Float,
        val rotationDegrees: Float,
        val opacity: Float,
    )
}
