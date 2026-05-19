package com.app.clipsteronline.upload.editor.core.model

import android.graphics.Color

/**
 * Represents a text clip/overlay in the timeline.
 * Contains text styling, animation, and positioning properties.
 */
data class TextClip(
    val id: String,
    val content: String,
    val timelineStartMs: Long,
    val timelineEndMs: Long,
    val fontFamily: String = "sans-serif",
    val fontSize: Float = 48f,
    val fontStyle: FontStyle = FontStyle.NORMAL,
    val textColor: Int = Color.WHITE,
    val backgroundColor: Int = Color.TRANSPARENT,
    val textAlignment: TextAlignment = TextAlignment.CENTER,
    val positionX: Float = 0.5f,
    val positionY: Float = 0.5f,
    val rotation: Float = 0f,
    val scaleX: Float = 1f,
    val scaleY: Float = 1f,
    val opacity: Float = 1f,
    val strokeColor: Int = Color.TRANSPARENT,
    val strokeWidth: Float = 0f,
    val shadowEnabled: Boolean = false,
    val shadowColor: Int = Color.BLACK,
    val shadowRadius: Float = 4f,
    val shadowOffsetX: Float = 2f,
    val shadowOffsetY: Float = 2f,
    val shadowOpacity: Float = 0.5f,
    val outlineEnabled: Boolean = false,
    val outlineColor: Int = Color.BLACK,
    val outlineWidth: Float = 2f,
    val animation: TextAnimation? = null,
    val effects: List<TextEffect> = emptyList(),
    val transform: TextTransform = TextTransform()
) : Clip {

    val durationMs: Long
        get() = timelineEndMs - timelineStartMs

    fun getPlaybackRange(): LongRange {
        return timelineStartMs until timelineEndMs
    }

    fun containsTime(timeMs: Long): Boolean {
        return timeMs >= timelineStartMs && timeMs < timelineEndMs
    }

    fun withContent(newContent: String): TextClip {
        return copy(content = newContent)
    }

    fun withStyle(
        fontSize: Float = this.fontSize,
        textColor: Int = this.textColor
    ): TextClip {
        return copy(fontSize = fontSize, textColor = textColor)
    }

    fun withPosition(x: Float, y: Float): TextClip {
        return copy(positionX = x.coerceIn(0f, 1f), positionY = y.coerceIn(0f, 1f))
    }

    fun withOpacity(opacity: Float): TextClip {
        return copy(opacity = opacity.coerceIn(0f, 1f))
    }

    fun withAnimation(animation: TextAnimation?): TextClip {
        return copy(animation = animation)
    }

    fun withStroke(color: Int, width: Float): TextClip {
        return copy(strokeColor = color, strokeWidth = width)
    }

    fun withShadow(enabled: Boolean): TextClip {
        return copy(shadowEnabled = enabled)
    }

    fun withOutline(enabled: Boolean, color: Int, width: Float): TextClip {
        return copy(outlineEnabled = enabled, outlineColor = color, outlineWidth = width)
    }

    fun calcTransformAt(timeMs: Long): TextTransform {
        val progress = if (durationMs > 0) {
            (timeMs - timelineStartMs).toFloat() / durationMs
        } else 0f

        return transform.at(animation, progress)
    }
}

/**
 * Font style options.
 */
enum class FontStyle {
    NORMAL,
    BOLD,
    ITALIC,
    BOLD_ITALIC
}

/**
 * Text alignment options.
 */
enum class TextAlignment {
    LEFT,
    CENTER,
    RIGHT;

    fun toAndroidAlignment(): Int {
        return when (this) {
            LEFT -> android.view.Gravity.START
            CENTER -> android.view.Gravity.CENTER
            RIGHT -> android.view.Gravity.END
        }
    }
}

/**
 * Text animation configurations.
 */
data class TextAnimation(
    val type: TextAnimationType,
    val durationMs: Long = 1000L,
    val delayMs: Long = 0L,
    val easing: Easing = Easing.EASE_OUT,
    val params: Map<String, Float> = emptyMap()
)

/**
 * Text animation types.
 */
enum class TextAnimationType {
    NONE,
    FADE_IN,
    FADE_OUT,
    SLIDE_IN_LEFT,
    SLIDE_IN_RIGHT,
    SLIDE_IN_TOP,
    SLIDE_IN_BOTTOM,
    SLIDE_OUT_LEFT,
    SLIDE_OUT_RIGHT,
    SLIDE_OUT_TOP,
    SLIDE_OUT_BOTTOM,
    TYPEWRITE,
    SCALE_IN,
    SCALE_OUT,
    POP_IN,
    BLINK,
    SHAKE,
    BOUNCE,
    ROTATE
}

/**
 * Text transform at specific timeline position.
 */
data class TextTransform(
    val translationX: Float = 0f,
    val translationY: Float = 0f,
    val scaleX: Float = 1f,
    val scaleY: Float = 1f,
    val opacity: Float = 1f,
    val rotation: Float = 0f
) {
    fun at(animation: TextAnimation?, progress: Float): TextTransform {
        val animProgress = progress.coerceIn(0f, 1f)

        return when {
            animation == null -> this.copy(opacity = opacity)
            animation.type == TextAnimationType.FADE_IN -> this.copy(
                opacity = animation.easing.apply(animProgress)
            )
            animation.type == TextAnimationType.FADE_OUT -> this.copy(
                opacity = 1f - animation.easing.apply(animProgress)
            )
            animation.type == TextAnimationType.SCALE_IN -> {
                val eased = animation.easing.apply(animProgress)
                this.copy(
                    scaleX = eased,
                    scaleY = eased,
                    opacity = if (eased < 0.01f) 0f else 1f
                )
            }
            else -> this
        }
    }

    companion object {
        val DEFAULT = TextTransform()
    }
}

/**
 * Text effects for styling.
 */
sealed class TextEffect(val name: String) {
    data class Outline(val color: Int, val width: Float) : TextEffect("outline")
    data class Shadow(val color: Int, val radius: Float, val dx: Float, val dy: Float) : TextEffect("shadow")
    data class Glow(val color: Int, val radius: Float) : TextEffect("glow")
    data class Gradient(val colors: List<Int>, val direction: Float) : TextEffect("gradient")
}