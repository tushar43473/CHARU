package com.app.clipsteronline.upload.editor.effects

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Central effects coordinator.
 * Manages active effects and applies them to timeline.
 */
class EffectEngine(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {
    private val _effectState = MutableStateFlow(EffectEngineState())
    val effectState: StateFlow<EffectEngineState> = _effectState.asStateFlow()

    private val activeEffects = mutableListOf<ActiveEffect>()
    private val effectQueue = mutableListOf<Effect>()

    /**
     * Add effect to clip.
     */
    fun addEffect(effect: Effect, clipId: String, intensity: Float = 1f) {
        val active = ActiveEffect(
            effect = effect,
            clipId = clipId,
            intensity = intensity,
            isActive = true
        )

        activeEffects.add(active)
        _effectState.value = _effectState.value.copy(
            activeCount = activeEffects.size
        )
    }

    /**
     * Remove effect.
     */
    fun removeEffect(effectId: String) {
        activeEffects.removeAll { it.effect.id == effectId }
        _effectState.value = _effectState.value.copy(
            activeCount = activeEffects.size
        )
    }

    /**
     * Update effect intensity.
     */
    fun setIntensity(effectId: String, intensity: Float) {
        val index = activeEffects.indexOfFirst { it.effect.id == effectId }
        if (index >= 0) {
            activeEffects[index] = activeEffects[index].copy(
                intensity = intensity.coerceIn(0f, 1f)
            )
        }
    }

    /**
     * Get effects for clip.
     */
    fun getEffectsForClip(clipId: String): List<ActiveEffect> {
        return activeEffects.filter { it.clipId == clipId && it.isActive }
    }

    /**
     * Apply effects at time.
     */
    fun applyEffects(clipId: String, timeMs: Long): List<Effect> {
        return getEffectsForClip(clipId).map { it.effect }
    }

    /**
     * Queue effect for rendering.
     */
    fun queueEffect(effect: Effect) {
        effectQueue.add(effect)
    }

    /**
     * Process queued effects.
     */
    fun processQueue(): List<Effect> {
        val effects = effectQueue.toList()
        effectQueue.clear()
        return effects
    }

    /**
     * Clear all effects.
     */
    fun clearAll() {
        activeEffects.clear()
        effectQueue.clear()
        _effectState.value = _effectState.value.copy(activeCount = 0)
    }
}

/**
 * Effect engine state.
 */
data class EffectEngineState(
    val activeCount: Int = 0,
    val isProcessing: Boolean = false
)

/**
 * Effect base class.
 */
abstract class Effect(
    val id: String,
    val name: String,
    val type: EffectType
)

/**
 * Active effect.
 */
data class ActiveEffect(
    val effect: Effect,
    val clipId: String,
    val intensity: Float = 1f,
    val isActive: Boolean = true
)

/**
 * Effect types.
 */
enum class EffectType {
    FILTER,
    BLUR,
    TRANSITION,
    BEAUTY,
    COLOR,
    RETRO,
    GLITCH
}