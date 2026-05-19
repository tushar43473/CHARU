package com.app.clipsteronline.upload.editor.import

import android.app.Activity
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Media picker.
 * System picker integration, multiple selection.
 */
class MediaPicker(private val context: Context) {

    private var callback: ((List<MediaItem>) -> Unit)? = null
    private var selectedMediaTypes = setOf(MediaType.VIDEO)

    /**
     * Launch video picker.
     */
    fun launchVideoPicker(activity: Activity, requestCode: Int) {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "video/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        activity.startActivityForResult(Intent.createChooser(intent, "Select Video"), requestCode)
    }

    /**
     * Launch audio picker.
     */
    fun launchAudioPicker(activity: Activity, requestCode: Int) {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "audio/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        activity.startActivityForResult(Intent.createChooser(intent, "Select Audio"), requestCode)
    }

    /**
     * Launch image picker.
     */
    fun launchImagePicker(activity: Activity, requestCode: Int) {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        activity.startActivityForResult(Intent.createChooser(intent, "Select Images"), requestCode)
    }

    /**
     * Launch mixed media picker.
     */
    fun launchMediaPicker(activity: Activity, requestCode: Int, types: Set<MediaType>) {
        selectedMediaTypes = types

        val mimeTypes = types.flatMap { getMimeTypes(it) }.toTypedArray()
        val commaSeparated = mimeTypes.joinToString(",")

        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            setType(commaSeparated)
            addCategory(Intent.CATEGORY_OPENABLE)
        }

        activity.startActivityForResult(Intent.createChooser(intent, "Select Media"), requestCode)
    }

    /**
     * Parse result URI.
     */
    suspend fun parseResult(data: Intent?): List<MediaItem> = withContext(Dispatchers.IO) {
        val items = mutableListOf<MediaItem>()

        data?.clipData?.let { clip ->
            for (i in 0 until clip.itemCount) {
                clip.getItemAt(i)?.uri?.let { uri ->
                    items.add(parseUri(uri))
                }
            }
        }

        data?.data?.let { uri ->
            items.add(parseUri(uri))
        }

        items
    }

    /**
     * Parse URI to MediaItem.
     */
    private suspend fun parseUri(uri: Uri): MediaItem = withContext(Dispatchers.IO) {
        val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"

        val (duration, size) = getMediaMetadata(uri, mimeType)

        MediaItem(
            uri = uri,
            name = getFileName(uri),
            mimeType = mimeType,
            duration = duration,
            size = size,
            mediaType = getMediaType(mimeType)
        )
    }

    /**
     * Get file name.
     */
    private fun getFileName(uri: Uri): String {
        var name = "Unknown"
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) {
                    name = cursor.getString(nameIndex) ?: name
                }
            }
        }
        return name
    }

    /**
     * Get media metadata.
     */
    private fun getMediaMetadata(uri: Uri, mimeType: String): Pair<Long, Long> {
        var duration = 0L
        var size = 0L

        when {
            mimeType.startsWith("video/") -> {
                val retriever = android.media.MediaMetadataRetriever()
                try {
                    retriever.setDataSource(context, uri)
                    duration = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
                } catch (e: Exception) { /* Ignore */ }
                finally { retriever.release() }
            }
            mimeType.startsWith("audio/") -> {
                val retriever = android.media.MediaMetadataRetriever()
                try {
                    retriever.setDataSource(context, uri)
                    duration = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
                } catch (e: Exception) { /* Ignore */ }
                finally { retriever.release() }
            }
        }

        // Get size
        context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
            size = pfd.statSize
        }

        return duration to size
    }

    /**
     * Get MediaType from mime type.
     */
    private fun getMediaType(mimeType: String): MediaType {
        return when {
            mimeType.startsWith("video/") -> MediaType.VIDEO
            mimeType.startsWith("audio/") -> MediaType.AUDIO
            mimeType.startsWith("image/") -> MediaType.IMAGE
            else -> MediaType.UNKNOWN
        }
    }

    /**
     * Get mime types.
     */
    private fun getMimeTypes(type: MediaType): List<String> {
        return when (type) {
            MediaType.VIDEO -> listOf("video/mp4", "video/quicktime", "video/x-matroska", "video/webm")
            MediaType.AUDIO -> listOf("audio/mpeg", "audio/wav", "audio/aac", "audio/flac")
            MediaType.IMAGE -> listOf("image/jpeg", "image/png", "image/webp", "image/heic")
            else -> listOf("*/*")
        }
    }

    /**
     * Media item.
     */
    data class MediaItem(
        val uri: Uri,
        val name: String,
        val mimeType: String,
        val duration: Long,
        val size: Long,
        val mediaType: MediaType
    )

    /**
     * Media types.
     */
    enum class MediaType {
        VIDEO,
        AUDIO,
        IMAGE,
        UNKNOWN
    }
}