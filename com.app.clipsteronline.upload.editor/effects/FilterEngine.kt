package com.app.clipsteronline.upload.editor.effects

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Filter engine for color adjustments.
 * Brightness, contrast, saturation, temperature, sharpen, vignette.
 */
class FilterEngine(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {
    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    private var brightness: Float = 0f
    private var contrast: Float = 1f
    private var saturation: Float = 1f
    private var temperature: Float = 0f
    private var sharpen: Float = 0f
    private var vignette: Float = 0f
    private var exposure: Float = 0f

    /**
     * Set brightness (-1 to 1).
     */
    fun setBrightness(value: Float) {
        brightness = value.coerceIn(-1f, 1f)
        emitUpdate()
    }

    /**
     * Set contrast (0 to 2).
     */
    fun setContrast(value: Float) {
        contrast = value.coerceIn(0f, 2f)
        emitUpdate()
    }

    /**
     * Set saturation (0 to 2).
     */
    fun setSaturation(value: Float) {
        saturation = value.coerceIn(0f, 2f)
        emitUpdate()
    }

    /**
     * Set temperature (-1 to 1).
     */
    fun setTemperature(value: Float) {
        temperature = value.coerceIn(-1f, 1f)
        emitUpdate()
    }

    /**
     * Set sharpen (0 to 1).
     */
    fun setSharpen(value: Float) {
        sharpen = value.coerceIn(0f, 1f)
        emitUpdate()
    }

    /**
     * Set vignette (0 to 1).
     */
    fun setVignette(value: Float) {
        vignette = value.coerceIn(0f, 1f)
        emitUpdate()
    }

    /**
     * Set exposure (-2 to 2).
     */
    fun setExposure(value: Float) {
        exposure = value.coerceIn(-2f, 2f)
        emitUpdate()
    }

    /**
     * Reset all filters.
     */
    fun reset() {
        brightness = 0f
        contrast = 1f
        saturation = 1f
        temperature = 0f
        sharpen = 0f
        vignette = 0f
        exposure = 0f
        emitUpdate()
    }

    /**
     * Get filter state.
     */
    private fun emitUpdate() {
        _filterState.value = FilterState(
            brightness = brightness,
            contrast = contrast,
            saturation = saturation,
            temperature = temperature,
            sharpen = sharpen,
            vignette = vignette,
            exposure = exposure
        )
    }

    /**
     * Build shader uniforms.
     */
    fun getShaderUniforms(): Map<String, Float> {
        return mapOf(
            "uBrightness" to brightness,
            "uContrast" to contrast,
            "uSaturation" to saturation,
            "uTemperature" to temperature,
            "uSharpen" to sharpen,
            "uVignette" to vignette,
            "uExposure" to exposure
        )
    }
}

/**
 * Filter state.
 */
data class FilterState(
    val brightness: Float = 0f,
    val contrast: Float = 1f,
    val saturation: Float = 1f,
    val temperature: Float = 0f,
    val sharpen: Float = 0f,
    val vignette: Float = 0f,
    val exposure: Float = 0f
)

/**
 * Preset filters.
 */
object FilterPresets {
    val NONE = FilterState()
    
    val WARM = FilterState(temperature = 0.2f, saturation = 1.1f)
    
    val COOL = FilterState(temperature = -0.2f, saturation = 1.0f)
    
    val DRAMATIC = FilterState(contrast = 1.3f, saturation = 0.8f, vignette = 0.3f)
    
    val SOFT = FilterState(contrast = 0.9f, saturation = 0.9f, sharpen = 0.2f)
    
    val VIVID = FilterState(saturation = 1.5f, contrast = 1.2f)
    
    val MUTED = FilterState(saturation = 0.6f, contrast = 0.85f)
    
    val CINEMATIC = FilterState(contrast = 1.2f, temperature = -0.1f, vignette = 0.4f)
}