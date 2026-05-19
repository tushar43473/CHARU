package com.app.clipsteronline.upload.editor.core.model

import android.net.Uri

/**
 * Represents a sticker clip in the timeline.
 * Contains sticker asset, animation, and transform properties.
 */
data class StickerClip(
    val id: String,
    val assetUri: Uri,
    val assetWidth: Int,
    val assetHeight: Int,
    val timelineStartMs: Long,
    val timelineEndMs: Long,
    val positionX: Float = 0.5f,
    val positionY: Float = 0.5f,
    val scale: Float = 1f,
    val rotation: Float = 0f,
    val opacity: Float = 1f,
    val flipHorizontal: Boolean = false,
    val flipVertical: Boolean = false,
    val animation: StickerAnimation? = null,
    val transform: StickerTransform = StickerTransform(),
    val keyframes: List<Keyframe> = emptyList()
) : Clip {

    val durationMs: Long
        get() = timelineEndMs - timelineStartMs

    fun getPlaybackRange(): LongRange {
        return timelineStartMs until timelineEndMs
    }

    fun containsTime(timeMs: Long): Boolean {
        return timeMs >= timelineStartMs && timeMs < timelineEndMs
    }

    fun withPosition(x: Float, y: Float): StickerClip {
        return copy(
            positionX = x.coerceIn(0f, 1f),
            positionY = y.coerceIn(0f, 1f)
        )
    }

    fun withScale(scale: Float): StickerClip {
        return copy(scale = scale.coerceIn(0.1f, 10f))
    }

    fun withRotation(rotation: Float): StickerClip {
        return copy(rotation = rotation % 360f)
    }

    fun withOpacity(opacity: Float): StickerClip {
        return copy(opacity = opacity.coerceIn(0f, 1f))
    }

    fun withAnimation(animation: StickerAnimation?): StickerClip {
        return copy(animation = animation)
    }

    fun flipHorizontally(): StickerClip {
        return copy(flipHorizontal = !flipHorizontal)
    }

    fun flipVertically(): StickerClip {
        return copy(flipVertical = !flipVertical)
    }

    fun calcTransformAt(timeMs: Long): StickerTransform {
        val progress = if (durationMs > 0) {
            (timeMs - timelineStartMs).toFloat() / durationMs
        } else 0f

        return transform.at(animation, progress)
    }
}

/**
 * Sticker animation configurations.
 */
data class StickerAnimation(
    val type: StickerAnimationType,
    val durationMs: Long = 1000L,
    val delayMs: Long = 0L,
    val repeatCount: Int = 0,
    val easing: Easing = Easing.EASE_OUT
)

/**
 * Sticker animation types.
 */
enum class StickerAnimationType {
    NONE,
    POP_IN,
    POP_OUT,
    SLIDE_IN,
    SLIDE_OUT,
    FADE_IN,
    FADE_OUT,
    BOUNCE,
    ROTATE,
    SHAKE,
    SCALE,
    PULSE,
    WIGGLE,
    TADA,
    SWING,
    SPIN
}

/**
 * Sticker transform at specific timeline position.
 */
data class StickerTransform(
    val translationX: Float = 0f,
    val translationY: Float = 0f,
    val scaleX: Float = 1f,
    val scaleY: Float = 1f,
    val rotation: Float = 0f,
    val opacity: Float = 1f
) {
    fun at(animation: StickerAnimation?, progress: Float): StickerTransform {
        val animProgress = progress.coerceIn(0f, 1f)

        return when {
            animation == null -> this.copy(opacity = opacity)
            animation.type == StickerAnimationType.FADE_IN -> this.copy(
                opacity = animation.easing.apply(animProgress)
            )
            animation.type == StickerAnimationType.FADE_OUT -> this.copy(
                opacity = 1f - animation.easing.apply(animProgress)
            )
            animation.type == StickerAnimationType.POP_IN -> {
                val eased = animation.easing.apply(animProgress)
                val scale = if (eased <= 0.5f) {
                    2 * eased * eased
                } else {
                    1 - (-2 * eased + 2).let { it * it } / 2
                }
                this.copy(scaleX = scale, scaleY = scale, opacity = eased)
            }
            animation.type == StickerAnimationType.SCALE -> {
                val eased = animation.easing.apply(animProgress)
                val scale = 1f + kotlin.math.sin(animProgress * kotlin.math.PI * 2).toFloat() * 0.2f
                this.copy(scaleX = scale, scaleY = scale)
            }
            animation.type == StickerAnimationType.ROTATE -> {
                val eased = animation.easing.apply(animProgress)
                this.copy(rotation = rotation + eased * 360f)
            }
            else -> this
        }
    }

    companion object {
        val DEFAULT = StickerTransform()
    }
}

/**
 * Predefined sticker motion paths.
 */
enum class StickerMotionPath(val path: List<Pair<Float, Float>>) {
    CIRCLE(listOf(0f to 0f)),
    HEART(listOf()),
    STAR(listOf())
}