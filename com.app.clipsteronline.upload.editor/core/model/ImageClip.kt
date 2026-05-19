package com.app.clipsteronline.upload.editor.core.model

import android.net.Uri

/**
 * Represents an image clip in the timeline.
 * Contains image-specific properties and transformations including Ken Burns effect.
 */
data class ImageClip(
    val id: String,
    val sourceUri: Uri,
    val sourceWidth: Int,
    val sourceHeight: Int,
    val timelineStartMs: Long,
    val timelineEndMs: Long,
    val durationMs: Long = timelineEndMs - timelineStartMs,
    val speed: Float = 1.0f,
    val rotation: Float = 0f,
    val scaleX: Float = 1.0f,
    val scaleY: Float = 1.0f,
    val positionX: Float = 0f,
    val positionY: Float = 0f,
    val opacity: Float = 1.0f,
    val kenBurnsEffect: KenBurnsEffect? = null,
    val transform: ImageTransform? = null,
    val filters: List<FilterModel> = emptyList(),
    val keyframes: List<Keyframe> = emptyList()
) : Clip {

    val actualDurationMs: Long
        get() = ((timelineEndMs - timelineStartMs) / speed).toLong()

    val aspectRatio: Float
        get() = sourceWidth.toFloat() / sourceHeight

    fun getPlaybackRange(): LongRange {
        return timelineStartMs until timelineEndMs
    }

    fun containsTime(timeMs: Long): Boolean {
        return timeMs >= timelineStartMs && timeMs < timelineEndMs
    }

    fun withKenBurns(effect: KenBurnsEffect?): ImageClip {
        return copy(kenBurnsEffect = effect)
    }

    fun withTransform(transform: ImageTransform?): ImageClip {
        return copy(transform = transform)
    }

    fun withOpacity(newOpacity: Float): ImageClip {
        return copy(opacity = newOpacity.coerceIn(0f, 1f))
    }

    fun withScale(scaleX: Float, scaleY: Float): ImageClip {
        return copy(scaleX = scaleX, scaleY = scaleY)
    }

    fun withPosition(x: Float, y: Float): ImageClip {
        return copy(positionX = x, positionY = y)
    }

    fun withRotation(angle: Float): ImageClip {
        return copy(rotation = angle % 360f)
    }

    fun withFilters(newFilters: List<FilterModel>): ImageClip {
        return copy(filters = newFilters)
    }

    fun addFilter(filter: FilterModel): ImageClip {
        return copy(filters = filters + filter)
    }

    fun calcTransformAt(timeMs: Long): ImageTransform {
        val progress = if (durationMs > 0) {
            (timeMs - timelineStartMs).toFloat() / durationMs
        } else 0f

        return transform?.at(progress) ?: kenBurnsEffect?.at(progress) ?: ImageTransform.DEFAULT
    }
}

/**
 * Ken Burns effect configuration for image zoom/pan animations.
 */
data class KenBurnsEffect(
    val type: KenBurnsType = KenBurnsType.NONE,
    val startFrame: KenBurnsFrame = KenBurnsFrame(),
    val endFrame: KenBurnsFrame = KenBurnsFrame(),
    val durationMs: Long = 3000L,
    val easing: Easing = Easing.EASE_IN_OUT
) {
    fun at(progress: Float): ImageTransform {
        val clampedProgress = progress.coerceIn(0f, 1f)
        val easedProgress = easing.apply(clampedProgress)

        return ImageTransform(
            translationX = lerp(startFrame.translationX, endFrame.translationX, easedProgress),
            translationY = lerp(startFrame.translationY, endFrame.translationY, easedProgress),
            scale = lerp(startFrame.scale, endFrame.scale, easedProgress),
            rotation = lerp(startFrame.rotation, endFrame.rotation, easedProgress)
        )
    }

    private fun lerp(start: Float, end: Float, t: Float): Float {
        return start + (end - start) * t
    }

    companion object {
        fun zoomIn(): KenBurnsEffect {
            return KenBurnsEffect(
                type = KenBurnsType.ZOOM_IN,
                startFrame = KenBurnsFrame(scale = 1.0f),
                endFrame = KenBurnsFrame(scale = 1.2f)
            )
        }

        fun zoomOut(): KenBurnsEffect {
            return KenBurnsEffect(
                type = KenBurnsType.ZOOM_OUT,
                startFrame = KenBurnsFrame(scale = 1.2f),
                endFrame = KenBurnsFrame(scale = 1.0f)
            )
        }

        fun panLeft(): KenBurnsEffect {
            return KenBurnsEffect(
                type = KenBurnsType.PAN_LEFT,
                startFrame = KenBurnsFrame(translationX = 0.1f),
                endFrame = KenBurnsFrame(translationX = -0.1f)
            )
        }

        fun panRight(): KenBurnsEffect {
            return KenBurnsEffect(
                type = KenBurnsType.PAN_RIGHT,
                startFrame = KenBurnsFrame(translationX = -0.1f),
                endFrame = KenBurnsFrame(translationX = 0.1f)
            )
        }
    }
}

/**
 * Ken Burns effect types.
 */
enum class KenBurnsType {
    NONE,
    ZOOM_IN,
    ZOOM_OUT,
    PAN_LEFT,
    PAN_RIGHT,
    DIAGONAL,
    CUSTOM
}

/**
 * Single frame configuration for Ken Burns effect.
 */
data class KenBurnsFrame(
    val translationX: Float = 0f,
    val translationY: Float = 0f,
    val scale: Float = 1f,
    val rotation: Float = 0f
)

/**
 * Image transform at specific timeline position.
 */
data class ImageTransform(
    val translationX: Float = 0f,
    val translationY: Float = 0f,
    val scale: Float = 1f,
    val rotation: Float = 0f
) {
    companion object {
        val DEFAULT = ImageTransform()
        val IDENTITY = ImageTransform()
    }
}

/**
 * Easing functions for animations.
 */
enum class Easing {
    LINEAR,
    EASE_IN,
    EASE_OUT,
    EASE_IN_OUT,
    EASE_IN_CUBIC,
    EASE_OUT_CUBIC,
    EASE_IN_OUT_CUBIC,
    BOUNCE_OUT,
    ELASTIC_OUT;

    fun apply(t: Float): Float {
        return when (this) {
            LINEAR -> t
            EASE_IN -> t * t
            EASE_OUT -> 1 - (1 - t) * (1 - t)
            EASE_IN_OUT -> if (t < 0.5f) 2 * t * t else 1 - 2 * (1 - t) * (1 - t)
            EASE_IN_CUBIC -> t * t * t
            EASE_OUT_CUBIC -> 1 - (1 - t) * (1 - t) * (1 - t)
            EASE_IN_OUT_CUBIC -> if (t < 0.5f) 4 * t * t * t else 1 - 4 * (1 - t) * (1 - t) * (1 - t)
            BOUNCE_OUT -> {
                val t1 = 2.75 * t * t
                if (t < 1 / 2.75) t1 else if (t < 2) t1 - 1.5f * (t - 1 / 2.75f) + 0.75f else if (t < 2.5) t1 - 2.25f * (t - 2f / 2.75f) + 0.9375f else t1 - 3.0625f * (t - 2.5f / 2.75f) + 1.1875f
            }
            ELASTIC_OUT -> {
                if (t == 0f || t == 1f) t
                else {
                    val p = 0.3f
                    (1 - Math.pow(2.0, (-10 * t).toDouble())).toFloat() * Math.sin((t - p / 4) * (2 * Math.PI) / p).toFloat() + 1
                }
            }
        }
    }
}