package com.app.clipsteronline.upload.editor.core.model

data class FilterModel(
    val id: String,
    val name: String,
    val lutUri: String? = null,
    val brightness: Float = 0f,
    val contrast: Float = 1f,
    val saturation: Float = 1f,
    val temperature: Float = 0f,
    val sharpen: Float = 0f,
    val vignette: Float = 0f,
    val shaderRef: String? = null,
) {
    init {
        require(id.isNotBlank())
        require(name.isNotBlank())
        require(brightness in -1f..1f)
        require(contrast in 0f..4f)
        require(saturation in 0f..4f)
        require(temperature in -1f..1f)
        require(sharpen in 0f..2f)
        require(vignette in 0f..1f)
    }
}
