package com.app.clipsteronline.upload.editor.import

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.graphics.ExifInterface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.withContext
import java.io.InputStream

/**
 * Image scanner.
 * Scan images, EXIF support, thumbnails.
 */
class ImageScanner(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) {
    private val resolver: ContentResolver = context.contentResolver

    /**
     * Scan all images.
     */
    suspend fun scanImages(): List<ImageInfo> = withContext(Dispatchers.IO) {
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.TITLE,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.MIME_TYPE
        )

        val images = mutableListOf<ImageInfo>()

        resolver.query(collection, projection, null, null, "${MediaStore.Images.Media.DATE_MODIFIED} DESC")?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val pathCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.TITLE)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            val wCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
            val hCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)
            val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
            val dateTakenCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
            val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val uri = ContentUris.withAppendedId(collection, id)

                images.add(ImageInfo(
                    id = id,
                    uri = uri,
                    path = cursor.getString(pathCol) ?: "",
                    name = cursor.getString(nameCol) ?: "Unknown",
                    title = cursor.getString(titleCol) ?: "",
                    sizeBytes = cursor.getLong(sizeCol),
                    width = cursor.getInt(wCol),
                    height = cursor.getInt(hCol),
                    dateAdded = cursor.getLong(dateCol),
                    dateTaken = cursor.getLong(dateTakenCol),
                    mimeType = cursor.getString(mimeCol) ?: "image/jpeg"
                ))
            }
        }

        images
    }

    /**
     * Get image metadata.
     */
    suspend fun getMetadata(uri: Uri): ImageMetadata? = withContext(Dispatchers.IO) {
        try {
            resolver.openInputStream(uri)?.use { input ->
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                BitmapFactory.decodeStream(input, null, options)

                val exif = getExif(uri)

                ImageMetadata(
                    width = options.outWidth,
                    height = options.outHeight,
                    orientation = exif?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL),
                    dateTime = exif?.getAttribute(ExifInterface.TAG_DATETIME),
                    make = exif?.getAttribute(ExifInterface.TAG_MAKE),
                    model = exif?.getAttribute(ExifInterface.TAG_MODEL),
                    iso = exif?.getAttributeInt(ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY, 0),
                    aperture = exif?.getAttribute(ExifInterface.TAG_APERTURE_VALUE),
                    focalLength = exif?.getAttribute(ExifInterface.TAG_FOCAL_LENGTH),
                    flash = exif?.getAttributeInt(ExifInterface.TAG_FLASH, -1),
                    gpsLatitude = exif?.getAttribute(ExifInterface.TAG_GPS_LATITUDE),
                    gpsLongitude = exif?.getAttribute(ExifInterface.TAG_GPS_LONGITUDE)
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Get EXIF data.
     */
    private fun getExif(uri: Uri): ExifInterface? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                val exif = ExifInterface(input)
                exif
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Load thumbnail efficiently.
     */
    suspend fun loadThumbnail(uri: Uri, targetSize: Int = 200): android.graphics.Bitmap? = withContext(Dispatchers.IO) {
        try {
            resolver.openInputStream(uri)?.use { input ->
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                BitmapFactory.decodeStream(input, null, options)

                val scale = maxOf(options.outWidth, options.outHeight) / targetSize
                options.inSampleSize = scale.coerceIn(1, 8)
                options.inJustDecodeBounds = false
                options.inPreferredConfig = android.graphics.Bitmap.Config.RGB_565

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
 * Image info.
 */
data class ImageInfo(
    val id: Long,
    val uri: Uri,
    val path: String,
    val name: String,
    val title: String = "",
    val sizeBytes: Long = 0L,
    val width: Int = 0,
    val height: Int = 0,
    val dateAdded: Long = 0,
    val dateTaken: Long = 0,
    val mimeType: String = "image/jpeg"
) {
    val resolution: String get() = "${width}x${height}"
}

/**
 * Detailed image metadata.
 */
data class ImageMetadata(
    val width: Int,
    val height: Int,
    val orientation: Int,
    val dateTime: String?,
    val make: String?,
    val model: String?,
    val iso: Int,
    val aperture: String?,
    val focalLength: String?,
    val flash: Int,
    val gpsLatitude: String?,
    val gpsLongitude: String?
)

/**
 * Orientation constants.
 */
object Orientations {
    const val NORMAL = ExifInterface.ORIENTATION_NORMAL
    const val ROTATE_90 = ExifInterface.ORIENTATION_ROTATE_90
    const val ROTATE_180 = ExifInterface.ORIENTATION_ROTATE_180
    const val ROTATE_270 = ExifInterface.ORIENTATION_ROTATE_270
    const val FLIP_HORIZONTAL = ExifInterface.ORIENTATION_FLIP_HORIZONTAL
    const val FLIP_VERTICAL = ExifInterface.ORIENTATION_FLIP_VERTICAL
}