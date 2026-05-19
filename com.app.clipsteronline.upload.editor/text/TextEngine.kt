package com.app.clipsteronline.upload.editor.text

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Central text coordinator.
 * Manages text overlays and captions.
 */
class TextEngine(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {
    private val _textState = MutableStateFlow(TextEngineState())
    val textState: StateFlow<TextEngineState> = _textState.asStateFlow()

    private val textLayers = mutableMapOf<String, TextLayer>()
    private var fontManager: FontManager? = null

    /**
     * Initialize text engine.
     */
    fun initialize() {
        fontManager = FontManager()
        _textState.value = _textState.value.copy(isInitialized = true)
    }

    /**
     * Add text layer.
     */
    fun addTextLayer(id: String, layer: TextLayer) {
        textLayers[id] = layer
        _textState.value = _textState.value.copy(
            textLayerCount = textLayers.size
        )
    }

    /**
     * Remove text layer.
     */
    fun removeTextLayer(id: String) {
        textLayers.remove(id)
        _textState.value = _textState.value.copy(
            textLayerCount = textLayers.size
        )
    }

    /**
     * Get text layer.
     */
    fun getTextLayer(id: String): TextLayer? = textLayers[id]

    /**
     * Get all text layers.
     */
    fun getAllTextLayers(): List<TextLayer> = textLayers.values.toList()

    /**
     * Render text at time.
     */
    fun renderText(timeMs: Long): List<TextLayer> {
        return textLayers.values.filter { it.isVisibleAt(timeMs) }
    }

    /**
     * Release resources.
     */
    fun release() {
        textLayers.clear()
        fontManager = null
    }
}

/**
 * Text engine state.
 */
data class TextEngineState(
    val isInitialized: Boolean = false,
    val textLayerCount: Int = 0,
    val isEditing: Boolean = false
)

/**
 * Text layer data.
 */
data class TextLayer(
    val id: String,
    var text: String,
    var startMs: Long,
    var endMs: Long,
    var x: Float = 0.5f,
    var y: Float = 0.9f,
    var scale: Float = 1f,
    var rotation: Float = 0f,
    var alpha: Float = 1f,
    var style: TextStyle = TextStyle.DEFAULT,
    var isVisible: Boolean = true
) {
    /**
     * Check if visible at time.
     */
    fun isVisibleAt(timeMs: Long): Boolean {
        return isVisible && timeMs in startMs..endMs
    }

    /**
     * Get duration.
     */
    fun getDuration(): Long = endMs - startMs
}

/**
 * Text styles.
 */
enum class TextStyle {
    DEFAULT,
    TITLE,
    SUBTITLE,
    CAPTION,
    WATERMARK,
    MEME,
    TYPEWRITER
}

/**
 * Text alignment.
 */
enum class TextAlignment {
    LEFT,
    CENTER,
    RIGHT
}