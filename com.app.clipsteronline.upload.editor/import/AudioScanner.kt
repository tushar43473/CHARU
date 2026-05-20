package com.app.clipsteronline.upload.editor.import

class AudioScanner {
    fun configure() = Unit

    fun scan(items: List<GalleryLoader.GalleryItem>): List<AudioAsset> {
        return items.filter { it.type == GalleryLoader.MediaType.AUDIO }
            .map {
                val durationMs = (it.sizeBytes / 24_000).coerceIn(1_000, 3_600_000)
                val sampleRate = if (it.sizeBytes % 2L == 0L) 44_100 else 48_000
                val channels = if (it.sizeBytes % 3L == 0L) 1 else 2
                AudioAsset(it.id, it.uri, durationMs, sampleRate, channels, mime = inferMime(it.uri), valid = durationMs > 0)
            }
    }

    private fun inferMime(uri: String): String = when {
        uri.endsWith(".wav", true) -> "audio/wav"
        uri.endsWith(".aac", true) -> "audio/aac"
        else -> "audio/mpeg"
    }

    data class AudioAsset(
        val id: String,
        val uri: String,
        val durationMs: Long,
        val sampleRate: Int,
        val channels: Int,
        val mime: String,
        val valid: Boolean,
    )
}
