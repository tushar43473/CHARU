package com.app.clipsteronline.upload.editor.import

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Media import manager.
 * Central coordinator, queue, cache.
 */
class MediaImportManager(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) {
    private val _importState = MutableStateFlow(ImportState())
    val importState: StateFlow<ImportState> = _importState.asStateFlow()

    private val videoScanner = VideoScanner(context, scope)
    private val audioScanner = AudioScanner(context, scope)
    private val imageScanner = ImageScanner(context, scope)
    private val galleryLoader = GalleryLoader(context, scope)

    private val importQueue = mutableListOf<PendingImport>()

    /**
     * Import from URI.
     */
    suspend fun importMedia(uri: Uri): ImportResult {
        val pending = PendingImport(
            id = System.currentTimeMillis().toString(),
            uri = uri,
            status = ImportStatus.QUEUED
        )

        importQueue.add(pending)
        notifyQueueChanged()

        return try {
            _importState.value = _importState.value.copy(status = ImportStatus.IMPORTING)

            // Check if already imported
            val existing = findCachedMedia(uri)
            if (existing != null) {
                removeFromQueue(pending.id)
                return ImportResult.Success(existing)
            }

            // Add to imported
            removeFromQueue(pending.id)

            notifyImported(pending)
            ImportResult.Success(pending)
        } catch (e: Exception) {
            removeFromQueue(pending.id)
            ImportResult.Error(e.message ?: "Import failed")
        }
    }

    /**
     * Scan all device media.
     */
    suspend fun scanAllMedia(): List<ImportedMedia> {
        _importState.value = _importState.value.copy(isScanning = true)

        val allMedia = mutableListOf<ImportedMedia>()

        try {
            allMedia.addAll(videoScanner.scanVideos().map { ImportedMedia(it.id.toString(), it.uri, it.name, ImportedMediaType.VIDEO) })
            allMedia.addAll(imageScanner.scanImages().map { ImportedMedia(it.id.toString(), it.uri, it.name, ImportedMediaType.IMAGE) })
            allMedia.addAll(audioScanner.scanAudio().map { ImportedMedia(it.id.toString(), it.uri, it.name, ImportedMediaType.AUDIO) })

            _importState.value = _importState.value.copy(allMedia = allMedia)
        } finally {
            _importState.value = _importState.value.copy(isScanning = false)
        }

        return allMedia
    }

    /**
     * Get videos.
     */
    suspend fun getVideos(): List<ImportedMedia> {
        val videos = videoScanner.scanVideos()
        return videos.map { ImportedMedia(it.id.toString(), it.uri, it.name, ImportedMediaType.VIDEO) }
    }

    /**
     * Get images.
     */
    suspend fun getImages(): List<ImportedMedia> {
        return imageScanner.scanImages().map { ImportedMedia(it.id.toString(), it.uri, it.name, ImportedMediaType.IMAGE) }
    }

    /**
     * Get audio.
     */
    suspend fun getAudio(): List<ImportedMedia> {
        return audioScanner.scanAudio().map { ImportedMedia(it.id.toString(), it.uri, it.name, ImportedMediaType.AUDIO) }
    }

    /**
     * Get gallery loader.
     */
    fun getGalleryLoader(): GalleryLoader = galleryLoader

    /**
     * Find cached media.
     */
    private fun findCachedMedia(uri: Uri): ImportedMedia? {
        return _importState.value.importedMedia.find { it.uri == uri }
    }

    /**
     * Remove from queue.
     */
    private fun removeFromQueue(id: String) {
        importQueue.removeAll { it.id == id }
        notifyQueueChanged()
    }

    /**
     * Notify queue changed.
     */
    private fun notifyQueueChanged() {
        _importState.value = _importState.value.copy(
            queuedCount = importQueue.size,
            status = if (importQueue.isNotEmpty()) ImportStatus.PENDING else ImportStatus.IDLE
        )
    }

    /**
     * Notify imported.
     */
    private fun notifyImported(media: PendingImport) {
        val current = _importState.value.importedMedia.toMutableList()
        current.add(ImportedMedia(media.id, media.uri, "Imported", ImportedMediaType.UNKNOWN))

        _importState.value = _importState.value.copy(
            importedMedia = current,
            importedCount = current.size
        )
    }

    /**
     * Clear cache.
     */
    fun clearCache() {
        _importState.value = _importState.value.copy(
            importedMedia = emptyList(),
            importedCount = 0
        )
    }
}

/**
 * Import state.
 */
data class ImportState(
    val status: ImportStatus = ImportStatus.IDLE,
    val importedMedia: List<ImportedMedia> = emptyList(),
    val allMedia: List<ImportedMedia> = emptyList(),
    val importedCount: Int = 0,
    val queuedCount: Int = 0,
    val isScanning: Boolean = false
)

/**
 * Import statuses.
 */
enum class ImportStatus {
    IDLE,
    QUEUED,
    PENDING,
    IMPORTING,
    COMPLETE,
    ERROR
}

/**
 * Pending import.
 */
data class PendingImport(
    val id: String,
    val uri: Uri,
    val status: ImportStatus,
    val error: String? = null
)

/**
 * Imported media.
 */
data class ImportedMedia(
    val id: String,
    val uri: Uri,
    val name: String,
    val type: ImportedMediaType,
    val duration: Long = 0L,
    val sizeBytes: Long = 0L
)

/**
 * Imported media types.
 */
enum class ImportedMediaType {
    VIDEO,
    AUDIO,
    IMAGE,
    UNKNOWN
}

/**
 * Import result.
 */
sealed class ImportResult {
    data class Success(val media: ImportedMedia) : ImportResult()
    data class Error(val message: String) : ImportResult()
}