package com.app.clipsteronline.upload.editor.import

class MediaPicker {
    fun configure() = Unit

    fun pick(media: List<GalleryLoader.GalleryItem>, request: PickRequest): PickResult {
        if (media.isEmpty()) return PickResult(emptyList(), false)
        val filtered = media.asSequence()
            .filter { request.allowedTypes.contains(it.type) }
            .filter { request.folder == null || it.folder == request.folder }
            .sortedByDescending { it.dateAddedMs }
            .toList()

        val selected = if (request.multiSelect) filtered.take(request.maxItems.coerceAtLeast(1)) else filtered.take(1)
        return PickResult(selected, hasMore = filtered.size > selected.size)
    }

    data class PickRequest(
        val allowedTypes: Set<GalleryLoader.MediaType> = setOf(GalleryLoader.MediaType.VIDEO, GalleryLoader.MediaType.AUDIO, GalleryLoader.MediaType.IMAGE),
        val multiSelect: Boolean = true,
        val maxItems: Int = 20,
        val folder: String? = null,
    )

    data class PickResult(val items: List<GalleryLoader.GalleryItem>, val hasMore: Boolean)
}
