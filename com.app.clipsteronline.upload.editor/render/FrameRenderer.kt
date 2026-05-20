package com.app.clipsteronline.upload.editor.render

class FrameRenderer(
    private val composition: RenderComposition,
    private val pipeline: RenderPipeline,
) {
    fun render(
        width: Int,
        height: Int,
        transitions: List<RenderPipeline.TransitionRequest> = emptyList(),
    ): Boolean {
        val layers = composition.visibleLayersSorted()
        if (layers.isEmpty()) return false
        return pipeline.render(layers, width, height, transitions)
    }
}
