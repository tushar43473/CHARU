package com.app.clipsteronline.upload.editor.render

enum class BlendMode {
    NORMAL,
    ADD,
    MULTIPLY,
    SCREEN,
    OVERLAY,
}

data class CompositionLayer(
    val id: String,
    val textureId: Int,
    val zIndex: Int,
    val alpha: Float = 1f,
    val visible: Boolean = true,
    val blendMode: BlendMode = BlendMode.NORMAL,
)

class RenderComposition {
    private val layers = mutableListOf<CompositionLayer>()

    @Synchronized
    fun setLayers(newLayers: List<CompositionLayer>) {
        layers.clear()
        layers.addAll(newLayers)
    }

    @Synchronized
    fun visibleLayersSorted(): List<CompositionLayer> {
        return layers.asSequence()
            .filter { it.visible && it.textureId != 0 && it.alpha > 0f }
            .sortedBy { it.zIndex }
            .toList()
    }

    @Synchronized
    fun exportableFrameCount(): Int {
        return visibleLayersSorted().size
    }
}
