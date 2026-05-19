package com.app.clipsteronline.upload.editor.render

import android.opengl.GLES20
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import com.app.clipsteronline.upload.editor.core.model.Clip

/**
 * Composes video, audio, effects, and overlays.
 * Handles z-order and blending.
 */
class RenderComposition(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {
    private val layers = mutableListOf<Layer>()

    /**
     * Add layer.
     */
    fun addLayer(layer: Layer) {
        layers.add(layer)
        sortLayers()
    }

    /**
     * Remove layer.
     */
    fun removeLayer(layerId: String) {
        layers.removeAll { it.id == layerId }
    }

    /**
     * Get layer.
     */
    fun getLayer(layerId: String): Layer? {
        return layers.find { it.id == layerId }
    }

    /**
     * Compose frame.
     */
    fun compose(outputTexture: Int, timeMs: Long): Boolean {
        // Bind output framebuffer
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, outputTexture)
        GLES20.glViewport(0, 0, 1920, 1080) // TODO: dynamic size

        // Clear to transparent
        GLES20.glClearColor(0f, 0f, 0f, 0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        // Render each layer
        for (layer in layers) {
            renderLayer(layer, timeMs)
        }

        return true
    }

    /**
     * Render single layer.
     */
    private fun renderLayer(layer: Layer, timeMs: Long) {
        if (!layer.visible) return
        if (!layer.containsTime(timeMs)) return

        // Enable blending for compositing
        when (layer.blendMode) {
            BlendMode.NORMAL -> {
                GLES20.glEnable(GLES20.GL_BLEND)
                GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
            }
            BlendMode.ADD -> {
                GLES20.glEnable(GLES20.GL_BLEND)
                GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE)
            }
            BlendMode.MULTIPLY -> {
                GLES20.glEnable(GLES20.GL_BLEND)
                GLES20.glBlendFunc(GLES20.GL_DST_COLOR, GLES20.GL_ONE_MINUS_SRC_ALPHA)
            }
            BlendMode.NONE -> {
                GLES20.glDisable(GLES20.GL_BLEND)
            }
        }

        // Render layer content
        layer.render(timeMs)
    }

    /**
     * Sort by z-index.
     */
    private fun sortLayers() {
        layers.sortBy { it.zIndex }
    }

    /**
     * Clear composition.
     */
    fun clear() {
        layers.clear()
    }
}

/**
 * Layer data.
 */
data class Layer(
    val id: String,
    val type: LayerType,
    var zIndex: Int = 0,
    var visible: Boolean = true,
    var blendMode: BlendMode = BlendMode.NORMAL,
    var clip: Clip? = null,
    private val renderAction: (Long) -> Unit = {}
) {
    fun containsTime(timeMs: Long): Boolean {
        return clip?.containsTime(timeMs) ?: false
    }

    fun render(timeMs: Long) {
        renderAction(timeMs)
    }
}

/**
 * Layer types.
 */
enum class LayerType {
    VIDEO,
    AUDIO,
    TEXT,
    STICKER,
    EFFECT,
    OVERLAY
}

/**
 * Blend modes.
 */
enum class BlendMode {
    NONE,
    NORMAL,
    ADD,
    MULTIPLY,
    SCREEN
}

/**
 * Video layer helper.
 */
fun createVideoLayer(
    id: String,
    clip: Clip,
    zIndex: Int,
    textureId: Int,
    render: (Long) -> Unit
): Layer = Layer(
    id = id,
    type = LayerType.VIDEO,
    zIndex = zIndex,
    clip = clip,
    renderAction = render
)

/**
 * Text layer helper.
 */
fun createTextLayer(
    id: String,
    text: String,
    zIndex: Int,
    render: (Long) -> Unit
): Layer = Layer(
    id = id,
    type = LayerType.TEXT,
    zIndex = zIndex,
    renderAction = render
)

/**
 * Effect layer helper.
 */
fun createEffectLayer(
    id: String,
    effectType: String,
    zIndex: Int,
    intensity: Float,
    render: (Long) -> Unit
): Layer = Layer(
    id = id,
    type = LayerType.EFFECT,
    zIndex = zIndex,
    renderAction = render
)