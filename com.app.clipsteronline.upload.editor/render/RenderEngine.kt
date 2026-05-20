package com.app.clipsteronline.upload.editor.render

import android.view.Surface
import java.util.concurrent.atomic.AtomicBoolean

class RenderEngine(
    private val openGLRenderer: OpenGLRenderer = OpenGLRenderer(),
    private val texturePool: TexturePool = TexturePool(),
) {
    private var renderScale: Float = 1f
    private val composition = RenderComposition()
    private var frameRenderer: FrameRenderer? = null
    private val initialized = AtomicBoolean(false)

    fun initialize(surface: Surface): Boolean {
        if (initialized.get()) return true
        val ok = openGLRenderer.initialize(surface)
        if (!ok) return false
        val pipeline = RenderPipeline(openGLRenderer)
        frameRenderer = FrameRenderer(composition, pipeline)
        initialized.set(true)
        return true
    }

    fun setCompositionLayers(layers: List<CompositionLayer>) {
        composition.setLayers(layers)
    }

    fun acquireTexture(): Int = texturePool.acquire()

    fun releaseTexture(textureId: Int) = texturePool.release(textureId)

    fun setRenderScale(scale: Float) {
        renderScale = scale.coerceIn(0.5f, 1f)
    }

    fun render(width: Int, height: Int): Boolean {
        if (!initialized.get()) return false
        val scaledW = (width * renderScale).toInt().coerceAtLeast(1)
        val scaledH = (height * renderScale).toInt().coerceAtLeast(1)
        return frameRenderer?.render(scaledW, scaledH) == true
    }

    fun release() {
        frameRenderer = null
        texturePool.clear()
        openGLRenderer.release()
        initialized.set(false)
    }
}
