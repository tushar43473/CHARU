package com.app.clipsteronline.upload.editor.core.model

import android.net.Uri
import android.graphics.RectF

/**
 * Represents a video clip in the timeline.
 * Contains all video-specific properties and transformations.
 */
data class VideoClip(
    val id: String,
    val sourceUri: Uri,
    val sourceDurationMs: Long,
    val timelineStartMs: Long,
    val timelineEndMs: Long,
    val trimStartMs: Long = 0L,
    val trimEndMs: Long = 0L,
    val speed: Float = 1.0f,
    val rotation: Float = 0f,
    val scaleX: Float = 1.0f,
    val scaleY: Float = 1.0f,
    val positionX: Float = 0f,
    val positionY: Float = 0f,
    val opacity: Float = 1.0f,
    val isMuted: Boolean = false,
    val isReversed: Boolean = false,
    val volume: Float = 1.0f,
    val filters: List<FilterModel> = emptyList(),
    val transition: TransitionModel? = null,
    val keyframes: List<Keyframe> = emptyList(),
    val transform: Transform = Transform.IDENTITY
) : Clip {

    val durationMs: Long
        get() = timelineEndMs - timelineStartMs

    val effectiveDurationMs: Long
        get() = ((timelineEndMs - timelineStartMs) / speed).toLong()

    val actualTrimStartMs: Long
        get() = trimStartMs

    val actualTrimEndMs: Long
        get() = trimEndMs

    val isVisible: Boolean
        get() = opacity > 0f

    fun getPlaybackRange(): LongRange {
        return timelineStartMs until timelineEndMs
    }

    fun containsTime(timeMs: Long): Boolean {
        return timeMs >= timelineStartMs && timeMs < timelineEndMs
    }

    fun withSpeed(newSpeed: Float): VideoClip {
        return copy(speed = newSpeed.coerceIn(0.25f, 4.0f))
    }

    fun withOpacity(newOpacity: Float): VideoClip {
        return copy(opacity = newOpacity.coerceIn(0f, 1f))
    }

    fun withPosition(x: Float, y: Float): VideoClip {
        return copy(positionX = x, positionY = y)
    }

    fun withScale(scaleX: Float, scaleY: Float): VideoClip {
        return copy(scaleX = scaleX, scaleY = scaleY)
    }

    fun withRotation(angle: Float): VideoClip {
        return copy(rotation = angle % 360f)
    }

    fun withTrim(startMs: Long, endMs: Long): VideoClip {
        return copy(trimStartMs = startMs, trimEndMs = endMs)
    }

    fun withVolume(newVolume: Float): VideoClip {
        return copy(volume = newVolume.coerceIn(0f, 1f))
    }

    fun withFilters(newFilters: List<FilterModel>): VideoClip {
        return copy(filters = newFilters)
    }

    fun addFilter(filter: FilterModel): VideoClip {
        return copy(filters = filters + filter)
    }

    fun withTransform(transform: Transform): VideoClip {
        return copy(transform = transform)
    }

    fun reversePlayback(): VideoClip {
        return copy(isReversed = !isReversed)
    }

    fun toggleMute(): VideoClip {
        return copy(isMuted = !isMuted)
    }
}

/**
 * Base clip interface.
 */
interface Clip {
    val id: String
    val sourceUri: Uri
    val timelineStartMs: Long
    val timelineEndMs: Long
    val durationMs: Long
}

/**
 * Transform configuration for clips.
 */
data class Transform(
    val translationX: Float = 0f,
    val translationY: Float = 0f,
    val scaleX: Float = 1f,
    val scaleY: Float = 1f,
    val rotation: Float = 0f,
    val pivotX: Float = 0.5f,
    val pivotY: Float = 0.5f
) {
    companion object {
        val IDENTITY = Transform()

        val FLIP_HORIZONTAL = Transform(scaleX = -1f)
        val FLIP_VERTICAL = Transform(scaleY = -1f)
        val ROTATE_90 = Transform(rotation = 90f)
        val ROTATE_180 = Transform(rotation = 180f)
        val ROTATE_270 = Transform(rotation = 270f)
    }

    fun isIdentity(): Boolean {
        return this == IDENTITY
    }

    fun toMatrix(): FloatArray {
        val matrix = FloatArray(6)
        // Simplified transformation matrix representation
        matrix[0] = scaleX
        matrix[1] = 0f
        matrix[2] = 0f
        matrix[3] = scaleY
        matrix[4] = translationX
        matrix[5] = translationY
        return matrix
    }
}

/**
 * Alpha/blending configuration.
 */
data class AlphaConfig(
    val blendMode: BlendMode = BlendMode.NORMAL,
    val opacity: Float = 1f
)

/**
 * Blend modes for compositing.
 */
enum class BlendMode {
    NORMAL,
    MULTIPLY,
    SCREEN,
    OVERLAY,
    DARKEN,
    LIGHTEN,
    COLOR_DODGE,
    COLOR_BURN,
    SOFT_LIGHT,
    HARD_LIGHT,
    DIFFERENCE,
    EXCLUSION
}