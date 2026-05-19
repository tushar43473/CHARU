package com.app.clipsteronline.upload.editor.timeline.thumbnails

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Decoder for thumbnail bitmaps.
 * Handles downsampling, memory-safe decoding, and bitmap reuse.
 */
class ThumbnailDecoder(
    private val context: Context
) {
    private val options = BitmapFactory.Options().apply {
        inJustDecodeBounds = false
        inBitmap = null
        inPreferredConfig = Bitmap.Config.RGB_565 // Lower memory than ARGB_8888
    }

    /**
     * Decode bitmap from URI.
     */
    suspend fun decodeBitmap(
        uri: Uri,
        width: Int,
        height: Int
    ): Bitmap? = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                // Calculate sample size
                val sampleSize = calculateSampleSize(stream, width, height)

                // Re-open stream and decode
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val opts = BitmapFactory.Options().apply {
                        inSampleSize = sampleSize
                        inPreferredConfig = Bitmap.Config.RGB_565
                        inMutable = false
                    }

                    BitmapFactory.decodeStream(inputStream, null, opts)
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Decode scaled bitmap.
     */
    suspend fun decodeScaledBitmap(
        uri: Uri,
        targetWidth: Int,
        targetHeight: Int
    ): Bitmap? = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                // Get bounds first
                val boundsOpts = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                BitmapFactory.decodeStream(stream, null, boundsOpts)

                // Calculate scale and sample
                val sampleSize = calculateSampleSize(
                    boundsOpts.outWidth,
                    boundsOpts.outHeight,
                    targetWidth,
                    targetHeight
                )

                // Decode with sample size
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val opts = BitmapFactory.Options().apply {
                        inSampleSize = sampleSize
                        inPreferredConfig = Bitmap.Config.RGB_565
                    }

                    val bitmap = BitmapFactory.decodeStream(inputStream, null, opts)

                    // Scale if needed
                    bitmap?.let { scaleBitmap(it, targetWidth, targetHeight) }
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Decode with reuse.
     */
    suspend fun decodeWithReuse(
        uri: Uri,
        reuse: Bitmap,
        width: Int,
        height: Int
    ): Bitmap? = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val opts = BitmapFactory.Options().apply {
                    inBitmap = reuse
                    inSampleSize = 1
                    inPreferredConfig = Bitmap.Config.RGB_565
                }

                BitmapFactory.decodeStream(stream, null, opts)
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Calculate optimal sample size.
     */
    private fun calculateSampleSize(
        stream: java.io.InputStream,
        targetWidth: Int,
        targetHeight: Int
    ): Int {
        val boundsOpts = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeStream(stream, null, boundsOpts)

        return calculateSampleSize(
            boundsOpts.outWidth,
            boundsOpts.outHeight,
            targetWidth,
            targetHeight
        )
    }

    /**
     * Calculate sample size from dimensions.
     */
    private fun calculateSampleSize(
        width: Int,
        height: Int,
        targetWidth: Int,
        targetHeight: Int
    ): Int {
        if (width <= 0 || height <= 0 || targetWidth <= 0 || targetHeight <= 0) {
            return 1
        }

        var sampleSize = 1
        while (width / sampleSize > targetWidth * 2 ||
               height / sampleSize > targetHeight * 2) {
            sampleSize *= 2
        }

        return sampleSize
    }

    /**
     * Scale bitmap.
     */
    private fun scaleBitmap(
        bitmap: Bitmap,
        targetWidth: Int,
        targetHeight: Int
    ): Bitmap {
        val scaleX = targetWidth.toFloat() / bitmap.width
        val scaleY = targetHeight.toFloat() / bitmap.height
        val scale = minOf(scaleX, scaleY)

        if (scale >= 1f) return bitmap

        val matrix = Matrix()
        matrix.postScale(scale, scale)

        return Bitmap.createBitmap(
            bitmap,
            0, 0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )
    }

    /**
     * Release resources.
     */
    fun release() {
        // Options don't hold resources
    }
}