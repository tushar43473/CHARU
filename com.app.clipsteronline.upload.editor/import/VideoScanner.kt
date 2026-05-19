package com.app.clipsteronline.upload.editor.import

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.withContext

/**
 * Video scanner.
 * Scan device videos, extract metadata.
 */
class VideoScanner(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) {
    private val resolver: ContentResolver = context.contentResolver

    /**
     * Scan all videos.
     */
    suspend fun scanVideos(): List<VideoInfo> = withContext(Dispatchers.IO) {
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.WIDTH,
            MediaStore.Video.Media.HEIGHT,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.DATE_MODIFIED,
            MediaStore.Video.Media.MIME_TYPE,
            MediaStore.Video.Media.ALBUM,
            MediaStore.Video.Media.ARTIST
        )

        val videos = mutableListOf<VideoInfo>()

        resolver.query(collection, projection, null, null, "${MediaStore.Video.Media.DATE_MODIFIED} DESC")?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val pathCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)
            val durCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            val wCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH)
            val hCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT)
            val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
            val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)
            val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.ALBUM)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val uri = ContentUris.withAppendedId(collection, id)

                videos.add(VideoInfo(
                    id = id,
                    uri = uri,
                    path = cursor.getString(pathCol) ?: "",
                    name = cursor.getString(nameCol) ?: "Unknown",
                    title = cursor.getString(titleCol) ?: "",
                    duration = cursor.getLong(durCol),
                    size = cursor.getLong(sizeCol),
                    width = cursor.getInt(wCol),
                    height = cursor.getInt(hCol),
                    dateAdded = cursor.getLong(dateCol),
                    mimeType = cursor.getString(mimeCol) ?: "video/mp4",
                    album = cursor.getString(albumCol) ?: ""
                ))
            }
        }

        videos
    }

    /**
     * Get video by ID.
     */
    suspend fun getVideo(id: Long): VideoInfo? = withContext(Dispatchers.IO) {
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

        val selection = "${MediaStore.Video.Media._ID} = ?"
        val selectionArgs = arrayOf(id.toString())

        resolver.query(collection, null, selection, selectionArgs, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val uri = ContentUris.withAppendedId(collection, id)

                return@withContext VideoInfo(
                    id = id,
                    uri = uri,
                    path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)) ?: "",
                    name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)) ?: "",
                    title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)) ?: "",
                    duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)),
                    size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)),
                    width = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH)),
                    height = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT)),
                    dateAdded = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)),
                    mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)) ?: "video/mp4",
                    album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.ALBUM)) ?: ""
                )
            }
        }
        null
    }

    /**
     * Extract detailed metadata.
     */
    suspend fun extractMetadata(uri: Uri): VideoMetadata? = withContext(Dispatchers.IO) {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, uri)

            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 0
            val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 0
            val bitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toIntOrNull() ?: 0
            val rotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)?.toIntOrNull() ?: 0
            val mimeType = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE) ?: "video/mp4"

            VideoMetadata(
                durationMs = duration,
                width = width,
                height = height,
                bitrate = bitrate,
                rotation = rotation,
                codec = mimeType,
                hasAudio = true, // Simplified check
                hasVideo = true
            )
        } catch (e: Exception) {
            null
        } finally {
            retriever.release()
        }
    }
}

/**
 * Video info.
 */
data class VideoInfo(
    val id: Long,
    val uri: Uri,
    val path: String,
    val name: String,
    val title: String,
    val duration: Long,
    val size: Long,
    val width: Int,
    val height: Int,
    val dateAdded: Long,
    val mimeType: String,
    val album: String
)

/**
 * Detailed video metadata.
 */
data class VideoMetadata(
    val durationMs: Long,
    val width: Int,
    val height: Int,
    val bitrate: Int,
    val rotation: Int,
    val codec: String,
    val hasAudio: Boolean,
    val hasVideo: Boolean
) {
    val resolution: String get() = "${width}x${height}"
    val frameRate: Int get() = 30 // Simplified
}