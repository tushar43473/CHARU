package com.app.clipsteronline.upload.editor.import

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

/**
 * Gallery loader.
 * Paged media loading, thumbnails, albums.
 */
class GalleryLoader(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) {
    private val _galleryState = MutableStateFlow(GalleryState())
    val galleryState: StateFlow<GalleryState> = _galleryState.asStateFlow()

    private val resolver: ContentResolver = context.contentResolver

    private var pageSize = 50
    private var currentPage = 0

    /**
     * Load videos.
     */
    suspend fun loadVideos(page: Int = 0): List<GalleryMedia> = withContext(Dispatchers.IO) {
        currentPage = page

        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.WIDTH,
            MediaStore.Video.Media.HEIGHT,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.DATE_MODIFIED
        )

        val sortOrder = "${MediaStore.Video.Media.DATE_MODIFIED} DESC LIMIT $pageSize OFFSET ${page * pageSize}"

        val mediaItems = mutableListOf<GalleryMedia>()

        resolver.query(collection, projection, null, null, sortOrder)?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val durCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            val wCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH)
            val hCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val uri = ContentUris.withAppendedId(collection, id)

                mediaItems.add(GalleryMedia(
                    id = id,
                    uri = uri,
                    name = cursor.getString(nameCol) ?: "Unknown",
                    duration = cursor.getLong(durCol),
                    sizeBytes = cursor.getLong(sizeCol),
                    width = cursor.getInt(wCol),
                    height = cursor.getInt(hCol),
                    mediaType = MediaType.VIDEO
                ))
            }
        }

        currentPage++
        _galleryState.value = _galleryState.value.copy(videos = mediaItems)

        mediaItems
    }

    /**
     * Load images.
     */
    suspend fun loadImages(page: Int = 0): List<GalleryMedia> = withContext(Dispatchers.IO) {
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT,
            MediaStore.Images.Media.DATE_ADDED
        )

        val sortOrder = "${MediaStore.Images.Media.DATE_MODIFIED} DESC LIMIT $pageSize OFFSET ${page * pageSize}"

        val mediaItems = mutableListOf<GalleryMedia>()

        resolver.query(collection, projection, null, null, sortOrder)?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            val wCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
            val hCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val uri = ContentUris.withAppendedId(collection, id)

                mediaItems.add(GalleryMedia(
                    id = id,
                    uri = uri,
                    name = cursor.getString(nameCol) ?: "Unknown",
                    sizeBytes = cursor.getLong(sizeCol),
                    width = cursor.getInt(wCol),
                    height = cursor.getInt(hCol),
                    mediaType = MediaType.IMAGE
                ))
            }
        }

        mediaItems
    }

    /**
     * Load album groups.
     */
    suspend fun loadAlbums(): List<Album> = withContext(Dispatchers.IO) {
        val albums = mutableListOf<Album>()

        // Simplified: return recent folder as default album
        albums.add(Album("Recent", "Recent", listOf()))

        _galleryState.value = _galleryState.value.copy(albums = albums)
        albums
    }

    /**
     * Set page size.
     */
    fun setPageSize(size: Int) {
        pageSize = size.coerceIn(10, 200)
    }

    /**
     * Get thumbnail.
     */
    suspend fun getThumbnail(uri: Uri, size: Int = 200): Bitmap? = withContext(Dispatchers.IO) {
        try {
            resolver.openInputStream(uri)?.use { input ->
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                BitmapFactory.decodeStream(input, null, options)

                val scale = maxOf(options.outWidth, options.outHeight) / size
                options.inSampleSize = scale.coerceIn(1, 4)
                options.inJustDecodeBounds = false

                resolver.openInputStream(uri)?.use { input2 ->
                    BitmapFactory.decodeStream(input2, null, options)
                }
            }
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * Gallery state.
 */
data class GalleryState(
    val videos: List<GalleryMedia> = emptyList(),
    val albums: List<Album> = emptyList(),
    val isLoading: Boolean = false
)

/**
 * Gallery media item.
 */
data class GalleryMedia(
    val id: Long,
    val uri: Uri,
    val name: String,
    val duration: Long = 0L,
    val sizeBytes: Long = 0L,
    val width: Int = 0,
    val height: Int = 0,
    val mediaType: MediaType = MediaType.UNKNOWN
)

/**
 * Album.
 */
data class Album(
    val id: String,
    val name: String,
    val coverUris: List<Uri>
)

/**
 * Media type.
 */
enum class MediaType {
    VIDEO,
    AUDIO,
    IMAGE
}