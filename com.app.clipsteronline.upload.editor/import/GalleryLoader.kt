package com.app.clipsteronline.upload.editor.import

class GalleryLoader {
    fun configure() = Unit

    fun recent(items: List<GalleryItem>, limit: Int = 100): List<GalleryItem> =
        items.sortedByDescending { it.dateAddedMs }.take(limit.coerceAtLeast(1))

    fun byFolder(items: List<GalleryItem>): Map<String, List<GalleryItem>> =
        items.groupBy { it.folder.ifBlank { "root" } }

    data class GalleryItem(
        val id: String,
        val uri: String,
        val type: MediaType,
        val folder: String,
        val dateAddedMs: Long,
        val sizeBytes: Long,
    )

    enum class MediaType { VIDEO, AUDIO, IMAGE }
}
