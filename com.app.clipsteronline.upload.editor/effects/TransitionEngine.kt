package com.app.clipsteronline.upload.editor.effects

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.app.clipsteronline.upload.editor.timeline.engine.TimelinePhysics

/**
 * Transition engine for clip transitions.
 * Fade, slide, zoom, blur, and timing interpolation.
 */
class TransitionEngine(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {
    private val _transitionState = MutableStateFlow(TransitionState())
    val transitionState: StateFlow<TransitionState> = _transitionState.asStateFlow()

    private var activeTransition: Transition? = null
    private var transitionDuration = 500L // ms
    private var transitionType = TransitionType.FADE

    /**
     * Start transition.
     */
    fun startTransition(type: TransitionType, durationMs: Long = 500L) {
        this.transitionType = type
        this.transitionDuration = durationMs

        activeTransition = Transition(
            type = type,
            startTime = System.currentTimeMillis(),
            duration = durationMs
        )

        _transitionState.value = _transitionState.value.copy(
            isTransitioning = true,
            progress = 0f
        )
    }

    /**
     * Update progress.
     */
    fun updateProgress() {
        val transition = activeTransition ?: return

        val elapsed = System.currentTimeMillis() - transition.startTime
        val progress = (elapsed.toFloat() / transition.duration).coerceIn(0f, 1f)

        _transitionState.value = _transitionState.value.copy(
            progress = progress,
            easedProgress = getEasedProgress(progress)
        )

        if (progress >= 1f) {
            endTransition()
        }
    }

    /**
     * End transition.
     */
    fun endTransition() {
        activeTransition = null
        _transitionState.value = _transitionState.value.copy(
            isTransitioning = false,
            progress = 1f,
            easedProgress = 1f
        )
    }

    /**
     * Apply interpolation.
     */
    private fun getEasedProgress(progress: Float): Float {
        return when (transitionType) {
            TransitionType.FADE -> progress
            TransitionType.SLIDE -> TimelinePhysics.easeOutQuad(progress)
            TransitionType.ZOOM -> TimelinePhysics.easeInOutQuad(progress)
            TransitionType.BLUR -> TimelinePhysics.easeOutQuad(progress)
            TransitionType.FADE_BLACK -> TimelinePhysics.linearInterpolate(0f, 1f, progress)
        }
    }

    /**
     * Get transition value.
     */
    fun getValue(progress: Float): TransitionValue {
        val easedProgress = getEasedProgress(progress)

        return when (transitionType) {
            TransitionType.FADE -> TransitionValue(
                alpha = easedProgress,
                offsetX = 0f,
                offsetY = 0f,
                scale = 1f,
                blur = 0f
            )
            TransitionType.SLIDE -> TransitionValue(
                alpha = 1f,
                offsetX = (1f - easedProgress) * 100,
                offsetY = 0f,
                scale = 1f,
                blur = 0f
            )
            TransitionType.ZOOM -> TransitionValue(
                alpha = 1f,
                offsetX = 0f,
                offsetY = 0f,
                scale = easedProgress,
                blur = 0f
            )
            TransitionType.BLUR -> TransitionValue(
                alpha = 1f,
                offsetX = 0f,
                offsetY = 0f,
                scale = 1f,
                blur = easedProgress
            )
            TransitionType.FADE_BLACK -> TransitionValue(
                alpha = easedProgress,
                offsetX = 0f,
                offsetY = 0f,
                scale = 1f,
                blur = 0f
            )
        }
    }

    /**
     * Set duration.
     */
    fun setDuration(durationMs: Long) {
        transitionDuration = durationMs.coerceIn(100L, 3000L)
    }

    /**
     * Is transitioning.
     */
    fun isTransitioning(): Boolean = _transitionState.value.isTransitioning
}

/**
 * Transition state.
 */
data class TransitionState(
    val isTransitioning: Boolean = false,
    val progress: Float = 0f,
    val easedProgress: Float = 0f
)

/**
 * Transition.
 */
data class Transition(
    val type: TransitionType,
    val startTime: Long,
    val duration: Long
)

/**
 * Transition types.
 */
enum class TransitionType {
    FADE,
    SLIDE,
    ZOOM,
    BLUR,
    FADE_BLACK
}

/**
 * Transition values.
 */
data class TransitionValue(
    val alpha: Float = 1f,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val scale: Float = 1f,
    val blur: Float = 0f
)