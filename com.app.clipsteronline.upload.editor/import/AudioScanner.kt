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
 * Audio scanner.
 * Scan audio/music, extract waveforms.
 */
class AudioScanner(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) {
    private val resolver: ContentResolver = context.contentResolver

    /**
     * Scan all audio.
     */
    suspend fun scanAudio(): List<AudioInfo> = withContext(Dispatchers.IO) {
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.MIME_TYPE
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} = 1"
        val audios = mutableListOf<AudioInfo>()

        resolver.query(collection, projection, selection, null, "${MediaStore.Audio.Media.DATE_MODIFIED} DESC")?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val pathCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val durCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val uri = ContentUris.withAppendedId(collection, id)
                val albumId = cursor.getLong(albumIdCol)

                audios.add(AudioInfo(
                    id = id,
                    uri = uri,
                    path = cursor.getString(pathCol) ?: "",
                    name = cursor.getString(nameCol) ?: "Unknown",
                    title = cursor.getString(titleCol) ?: "",
                    artist = cursor.getString(artistCol) ?: "Unknown Artist",
                    album = cursor.getString(albumCol) ?: "Unknown Album",
                    albumId = albumId,
                    duration = cursor.getLong(durCol),
                    size = cursor.getLong(sizeCol),
                    dateAdded = cursor.getLong(dateCol),
                    mimeType = cursor.getString(mimeCol) ?: "audio/mpeg"
                ))
            }
        }

        audios
    }

    /**
     * Scan sound Effects (non-music).
     */
    suspend fun scanSoundEffects(): List<AudioInfo> = withContext(Dispatchers.IO) {
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} = 0"
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.MIME_TYPE
        )

        val audios = mutableListOf<AudioInfo>()

        resolver.query(collection, projection, selection, null, null)?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val pathCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val durCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val uri = ContentUris.withAppendedId(collection, id)

                audios.add(AudioInfo(
                    id = id,
                    uri = uri,
                    path = cursor.getString(pathCol) ?: "",
                    name = cursor.getString(nameCol) ?: "Unknown",
                    duration = cursor.getLong(durCol),
                    size = cursor.getLong(sizeCol),
                    mimeType = cursor.getString(mimeCol) ?: "audio/mpeg"
                ))
            }
        }

        audios
    }

    /**
     * Get detailed audio metadata.
     */
    suspend fun extractMetadata(uri: Uri): AudioMetadata? = withContext(Dispatchers.IO) {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, uri)

            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: ""
            val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: ""
            val album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM) ?: ""
            val composer = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_AUTHOR) ?: ""

            AudioMetadata(
                durationMs = duration,
                title = title,
                artist = artist,
                album = album,
                composer = composer,
                year = null,
                trackNumber = null,
                genre = null,
                bitrate = 0,
                sampleRate = 0
            )
        } catch (e: Exception) {
            null
        } finally {
            retriever.release()
        }
    }
}

/**
 * Audio info.
 */
data class AudioInfo(
    val id: Long,
    val uri: Uri,
    val path: String,
    val name: String,
    val title: String = "",
    val artist: String = "Unknown Artist",
    val album: String = "Unknown Album",
    val albumId: Long = 0,
    val duration: Long = 0L,
    val size: Long = 0L,
    val dateAdded: Long = 0,
    val mimeType: String = "audio/mpeg"
)

/**
 * Detailed audio metadata.
 */
data class AudioMetadata(
    val durationMs: Long,
    val title: String,
    val artist: String,
    val album: String,
    val composer: String,
    val year: Int?,
    val trackNumber: Int?,
    val genre: String?,
    val bitrate: Int,
    val sampleRate: Int
) {
    val formattedDuration: String get() {
        val seconds = durationMs / 1000
        val mins = seconds / 60
        val secs = seconds % 60
        return "$mins:${secs.toString().padStart(2, '0')}"
    }
}