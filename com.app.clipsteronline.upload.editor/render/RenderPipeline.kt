package com.app.clipsteronline.upload.editor.render

import android.opengl.GLES20
import com.app.clipsteronline.upload.editor.core.model.TransitionModel
import com.app.clipsteronline.upload.editor.effects.EffectEngine
import com.app.clipsteronline.upload.editor.effects.TransitionEngine

class RenderPipeline(
    private val renderer: OpenGLRenderer,
    private val transitionEngine: TransitionEngine = TransitionEngine(),
    private val effectEngine: EffectEngine = EffectEngine(),
) {
    fun render(
        layers: List<CompositionLayer>,
        width: Int,
        height: Int,
        transitions: List<TransitionRequest> = emptyList(),
        effects: List<EffectEngine.EffectRequest> = emptyList(),
        frameTimeMs: Long = 0L,
    ): Boolean {
        if (layers.isEmpty()) return false
        var drewAny = false
        val transitionMap = transitions.associateBy { it.toLayerId }
        val passes = effectEngine.buildRenderPasses(effects, frameTimeMs)

        layers.forEach { layer ->
            applyBlendMode(layer.blendMode, layer.alpha)
            val transition = transitionMap[layer.id]
            val layerOk = if (transition != null) renderTransition(transition, width, height)
            else renderer.renderFrame(layer.textureId, width, height)
            drewAny = drewAny || layerOk
        }

        // lightweight post-style pass scheduling hook for future FBO chaining
        if (drewAny && passes.isNotEmpty()) {
            passes.forEach { pass ->
                applyBlendMode(BlendMode.NORMAL, pass.intensity)
            }
        }

        GLES20.glDisable(GLES20.GL_BLEND)
        return drewAny
    }

    private fun renderTransition(request: TransitionRequest, width: Int, height: Int): Boolean {
        val shader = transitionEngine.resolveShader(request.transition)
        val blendAlpha = request.transition.intensity.coerceIn(0f, 1f) * request.progress.coerceIn(0f, 1f)
        applyBlendMode(BlendMode.NORMAL, blendAlpha)
        val fromOk = renderer.renderFrame(request.fromTextureId, width, height)
        val toOk = renderer.renderFrame(request.toTextureId, width, height)
        return if (fromOk || toOk) true else {
            renderer.renderFrame(request.toTextureId, width, height) && shader.fragmentShader.isNotBlank()
        }
    }

    private fun applyBlendMode(mode: BlendMode, alpha: Float) {
        GLES20.glEnable(GLES20.GL_BLEND)
        when (mode) {
            BlendMode.NORMAL -> GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
            BlendMode.ADD -> GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE)
            BlendMode.MULTIPLY -> GLES20.glBlendFunc(GLES20.GL_DST_COLOR, GLES20.GL_ONE_MINUS_SRC_ALPHA)
            BlendMode.SCREEN -> GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_COLOR)
            BlendMode.OVERLAY -> GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        }
        GLES20.glBlendColor(1f, 1f, 1f, alpha.coerceIn(0f, 1f))
    }

    data class TransitionRequest(
        val fromLayerId: String,
        val toLayerId: String,
        val fromTextureId: Int,
        val toTextureId: Int,
        val transition: TransitionModel,
        val progress: Float,
    )
}
