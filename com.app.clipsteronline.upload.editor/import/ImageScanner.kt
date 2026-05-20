package com.app.clipsteronline.upload.editor.import

class ImageScanner {
    fun configure() = Unit

    fun scan(items: List<GalleryLoader.GalleryItem>): List<ImageAsset> {
        return items.filter { it.type == GalleryLoader.MediaType.IMAGE }
            .map {
                val width = ((it.sizeBytes % 2500) + 720).toInt()
                val height = ((it.sizeBytes % 1800) + 720).toInt()
                val orientation = listOf(0, 90, 180, 270)[(it.sizeBytes % 4).toInt()]
                ImageAsset(it.id, it.uri, width, height, orientation, hasExif = true, valid = width > 0 && height > 0)
            }
    }

    data class ImageAsset(
        val id: String,
        val uri: String,
        val width: Int,
        val height: Int,
        val orientationDegrees: Int,
        val hasExif: Boolean,
        val valid: Boolean,
    )
}
