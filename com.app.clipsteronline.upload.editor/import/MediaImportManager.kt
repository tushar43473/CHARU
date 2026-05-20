package com.app.clipsteronline.upload.editor.import

class MediaImportManager(
    private val galleryLoader: GalleryLoader = GalleryLoader(),
    private val mediaPicker: MediaPicker = MediaPicker(),
    private val videoScanner: VideoScanner = VideoScanner(),
    private val audioScanner: AudioScanner = AudioScanner(),
    private val imageScanner: ImageScanner = ImageScanner(),
    private val compressor: MediaCompressor = MediaCompressor(),
) {
    private val queue = ArrayDeque<GalleryLoader.GalleryItem>()

    fun configure() = Unit

    fun enqueue(items: List<GalleryLoader.GalleryItem>) {
        items.forEach { queue.addLast(it) }
    }

    fun importAll(request: MediaPicker.PickRequest = MediaPicker.PickRequest()): ImportReport {
        val source = queue.toList()
        queue.clear()

        val picked = mediaPicker.pick(source, request).items
        val videos = videoScanner.scan(picked)
        val audios = audioScanner.scan(picked)
        val images = imageScanner.scan(picked)

        val compressed = picked.map {
            compressor.compress(
                MediaCompressor.CompressionRequest(
                    uri = it.uri,
                    sizeBytes = it.sizeBytes,
                    profile = if (it.sizeBytes > 100_000_000L) MediaCompressor.CompressionProfile.AGGRESSIVE else MediaCompressor.CompressionProfile.BALANCED,
                ),
            )
        }

        return ImportReport(
            totalRequested = source.size,
            importedCount = picked.size,
            remainingInQueue = queue.size,
            videoAssets = videos,
            audioAssets = audios,
            imageAssets = images,
            compressed = compressed,
            folders = galleryLoader.byFolder(source).mapValues { it.value.size },
        )
    }

    data class ImportReport(
        val totalRequested: Int,
        val importedCount: Int,
        val remainingInQueue: Int,
        val videoAssets: List<VideoScanner.VideoAsset>,
        val audioAssets: List<AudioScanner.AudioAsset>,
        val imageAssets: List<ImageScanner.ImageAsset>,
        val compressed: List<MediaCompressor.CompressionResult>,
        val folders: Map<String, Int>,
    )
}
