package com.app.clipsteronline.upload.editor.text

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator

/**
 * Text animation engine.
 * Handles text entrance, exit, and motion animations.
 */
class TextAnimationEngine(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {
    private val activeAnimations = mutableMapOf<String, TextAnimation>()

    /**
     * Add animation.
     */
    fun addAnimation(id: String, animation: TextAnimation) {
        activeAnimations[id] = animation
    }

    /**
     * Remove animation.
     */
    fun removeAnimation(id: String) {
        activeAnimations.remove(id)
    }

    /**
     * Get animation value at time.
     */
    fun getValue(animationId: String, timeMs: Long): Float {
        val anim = activeAnimations[animationId] ?: return 0f

        val progress = ((timeMs - anim.startMs).toFloat() / anim.duration).coerceIn(0f, 1f)
        val easedProgress = applyInterpolator(progress, anim.interpolator)

        return anim.from + (anim.to - anim.from) * easedProgress
    }

    /**
     * Apply interpolator.
     */
    private fun applyInterpolator(progress: Float, interpolator: Interpolator?): Float {
        return interpolator?.getInterpolation(progress) ?: progress
    }

    /**
     * Create entrance animation.
     */
    fun createEntrance(type: EntranceType, durationMs: Long): TextAnimation {
        return TextAnimation(
            id = java.util.UUID.randomUUID().toString(),
            type = AnimationType.ENTRANCE,
            entranceType = type,
            startMs = 0,
            duration = durationMs,
            from = 0f,
            to = 1f,
            interpolator = type.getInterpolator()
        )
    }

    /**
     * Create exit animation.
     */
    fun createExit(type: ExitType, durationMs: Long): TextAnimation {
        return TextAnimation(
            id = java.util.UUID.randomUUID().toString(),
            type = AnimationType.EXIT,
            exitType = type,
            startMs = 0,
            duration = durationMs,
            from = 1f,
            to = 0f,
            interpolator = type.getInterpolator()
        )
    }

    /**
     * Create motion animation.
     */
    fun createMotion(type: MotionType, fromValue: Float, toValue: Float, durationMs: Long): TextAnimation {
        return TextAnimation(
            id = java.util.UUID.randomUUID().toString(),
            type = AnimationType.MOTION,
            motionType = type,
            startMs = 0,
            duration = durationMs,
            from = fromValue,
            to = toValue,
            interpolator = type.getInterpolator()
        )
    }

    /**
     * Clear all animations.
     */
    fun clear() {
        activeAnimations.clear()
    }
}

/**
 * Text animation data.
 */
data class TextAnimation(
    val id: String,
    val type: AnimationType,
    val entranceType: EntranceType? = null,
    val exitType: ExitType? = null,
    val motionType: MotionType? = null,
    val startMs: Long,
    val duration: Long,
    val from: Float,
    val to: Float,
    val interpolator: Interpolator? = null
)

/**
 * Animation types.
 */
enum class AnimationType {
    ENTRANCE,
    EXIT,
    MOTION,
    EFFECT
}

/**
 * Entrance animation types.
 */
enum class EntranceType {
    FADE_IN,
    SLIDE_IN_LEFT,
    SLIDE_IN_RIGHT,
    SLIDE_IN_TOP,
    SLIDE_IN_BOTTOM,
    SCALE_IN,
    TYPEWRITER,
    BLUR_IN;

    fun getInterpolator(): Interpolator = when (this) {
        EntranceType.FADE_IN -> android.view.animation.DecelerateInterpolator()
        EntranceType.SCALE_IN -> android.view.animation.OvershootInterpolator()
        EntranceType.TYPEWRITER -> LinearInterpolator()
        else -> android.view.animation.DecelerateInterpolator()
    }
}

/**
 * Exit animation types.
 */
enum class ExitType {
    FADE_OUT,
    SLIDE_OUT_LEFT,
    SLIDE_OUT_RIGHT,
    SLIDE_OUT_TOP,
    SLIDE_OUT_BOTTOM,
    SCALE_OUT,
    BLUR_OUT;

    fun getInterpolator(): Interpolator = when (this) {
        ExitType.SCALE_OUT -> android.view.animation.AccelerateInterpolator()
        else -> android.view.animation.AccelerateInterpolator()
    }
}

/**
 * Motion animation types.
 */
enum class MotionType {
    SHAKE,
    PULSE,
    BOUNCE,
    WAVE,
    ROTATE;

    fun getInterpolator(): Interpolator = when (this) {
        MotionType.PULSE -> android.view.animation.OvershootInterpolator()
        MotionType.BOUNCE -> android.view.animation.BounceInterpolator()
        else -> LinearInterpolator()
    }
}