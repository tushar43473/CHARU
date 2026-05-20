package com.app.clipsteronline.upload.editor.import

class VideoScanner {
    fun configure() = Unit

    fun scan(items: List<GalleryLoader.GalleryItem>): List<VideoAsset> {
        return items.filter { it.type == GalleryLoader.MediaType.VIDEO }
            .map {
                val width = inferDimension(it.sizeBytes, 1920)
                val height = inferDimension(it.sizeBytes / 2, 1080)
                val fps = inferFps(it.sizeBytes)
                VideoAsset(it.id, it.uri, width, height, fps, codec = inferCodec(it.uri), durationMs = inferDuration(it.sizeBytes), valid = width > 0 && height > 0)
            }
    }

    private fun inferDimension(seed: Long, fallback: Int): Int = if (seed <= 0) fallback else (seed % fallback + fallback / 2).toInt()
    private fun inferDuration(sizeBytes: Long): Long = (sizeBytes / 32_000).coerceIn(1_000, 600_000)
    private fun inferFps(sizeBytes: Long): Float = listOf(24f, 25f, 30f, 60f)[((sizeBytes / 1000) % 4).toInt()]
    private fun inferCodec(uri: String): String = if (uri.endsWith(".mov", true)) "prores" else "h264"

    data class VideoAsset(
        val id: String,
        val uri: String,
        val width: Int,
        val height: Int,
        val fps: Float,
        val codec: String,
        val durationMs: Long,
        val valid: Boolean,
    )
}
