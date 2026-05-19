package com.app.clipsteronline.upload.editor.core.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.net.Uri
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.min

/**
 * Bitmap utilities for image manipulation.
 * Provides scaling, cropping, compression, and memory-safe operations.
 */
object BitmapUtils {

    private const val MAX_TEXTURE_SIZE = 4096
    private const val DEFAULT_QUALITY = 85
    private const val THUMBNAIL_MAX_SIZE = 512
    private const val BLUR_RADIUS_MAX = 25f

    /**
     * Scale bitmap to fit within max dimensions while maintaining aspect ratio.
     */
    fun scaleToFit(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        val scaleWidth = maxWidth.toFloat() / width
        val scaleHeight = maxHeight.toFloat() / height
        val scale = min(scaleWidth, scaleHeight)

        if (scale >= 1f) return bitmap

        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    /**
     * Scale bitmap to fill dimensions and crop if needed.
     */
    fun scaleToFill(bitmap: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        val scaleWidth = targetWidth.toFloat() / width
        val scaleHeight = targetHeight.toFloat() / height
        val scale = max(scaleWidth, scaleHeight)

        val scaledWidth = (width * scale).toInt()
        val scaledHeight = (height * scale).toInt()

        val scaled = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true)

        val x = (scaledWidth - targetWidth) / 2
        val y = (scaledHeight - targetHeight) / 2

        return Bitmap.createBitmap(scaled, x, y, targetWidth, targetHeight)
    }

    /**
     * Crop bitmap to rectangle.
     */
    fun crop(bitmap: Bitmap, x: Int, y: Int, width: Int, height: Int): Bitmap {
        val safeX = x.coerceIn(0, bitmap.width - 1)
        val safeY = y.coerceIn(0, bitmap.height - 1)
        val safeWidth = width.coerceIn(1, bitmap.width - safeX)
        val safeHeight = height.coerceIn(1, bitmap.height - safeY)

        return Bitmap.createBitmap(bitmap, safeX, safeY, safeWidth, safeHeight)
    }

    /**
     * Crop bitmap to aspect ratio.
     */
    fun cropToAspect(bitmap: Bitmap, aspectRatio: Float): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        val currentAspect = width.toFloat() / height

        return if (currentAspect > aspectRatio) {
            val newWidth = (height * aspectRatio).toInt()
            val x = (width - newWidth) / 2
            Bitmap.createBitmap(bitmap, x, 0, newWidth, height)
        } else {
            val newHeight = (width / aspectRatio).toInt()
            val y = (height - newHeight) / 2
            Bitmap.createBitmap(bitmap, 0, y, width, newHeight)
        }
    }

    /**
     * Rotate bitmap by degrees.
     */
    fun rotate(bitmap: Bitmap, degrees: Float): Bitmap {
        if (degrees == 0f) return bitmap

        val matrix = Matrix().apply {
            postRotate(degrees)
        }

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    /**
     * Create thumbnail from bitmap.
     */
    fun createThumbnail(bitmap: Bitmap, maxSize: Int = THUMBNAIL_MAX_SIZE): Bitmap {
        return scaleToFit(bitmap, maxSize, maxSize)
    }

    /**
     * Compress bitmap to byte array.
     */
    fun compress(bitmap: Bitmap, format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG, quality: Int = DEFAULT_QUALITY): ByteArray {
        val output = ByteArrayOutputStream()
        bitmap.compress(format, quality, output)
        return output.toByteArray()
    }

    /**
     * Save bitmap to file.
     */
    fun save(bitmap: Bitmap, path: String, format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG, quality: Int = DEFAULT_QUALITY): Boolean {
        return try {
            val output = java.io.FileOutputStream(path)
            bitmap.compress(format, quality, output)
            output.close()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Load bitmap from URI with sampling.
     */
    fun loadBitmap(context: Context, uri: Uri, maxSize: Int = MAX_TEXTURE_SIZE): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }

            context.contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input, null, options)
            }

            val sampleSize = calculateInSampleSize(options.outWidth, options.outHeight, maxSize, maxSize)

            val loadOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }

            context.contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input, null, loadOptions)
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Load safe bitmap with memory constraints.
     */
    fun loadBitmapSafe(context: Context, uri: Uri, maxMemoryBytes: Long = 50 * 1024 * 1024): Bitmap? {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }

        context.contentResolver.openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(input, null, options)
        }

        val width = options.outWidth
        val height = options.outHeight

        val maxDimension = max(width, height)
        val targetDimension = calculateMaxDimension(maxDimension, maxMemoryBytes)

        return loadBitmap(context, uri, targetDimension)
    }

    /**
     * Calculate sample size for efficient loading.
     */
    fun calculateInSampleSize(width: Int, height: Int, reqWidth: Int, reqHeight: Int): Int {
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    /**
     * Calculate max dimension based on memory budget.
     */
    fun calculateMaxDimension(originalSize: Int, maxMemoryBytes: Long): Int {
        val bytesPerPixel = 4
        val maxPixels = maxMemoryBytes / bytesPerPixel
        val aspectRatio = 16f / 9f

        var height = kotlin.math.sqrt(maxPixels / aspectRatio).toInt()
        var width = (height * aspectRatio).toInt()

        if (width > originalSize || height > originalSize) {
            return originalSize.coerceIn(width, height)
        }

        while (width * height * bytesPerPixel > maxMemoryBytes) {
            height /= 2
            width = (height * aspectRatio).toInt()
        }

        return width.coerceIn(64, MAX_TEXTURE_SIZE)
    }

    /**
     * Create rounded bitmap.
     */
    fun rounded(bitmap: Bitmap, cornerRadius: Float): Bitmap {
        val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val rect = RectF(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat())
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        return output
    }

    /**
     * Create circular bitmap.
     */
    fun circular(bitmap: Bitmap): Bitmap {
        val size = min(bitmap.width, bitmap.height)
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)

        val x = (size - bitmap.width) / 2
        val y = (size - bitmap.height) / 2
        canvas.drawBitmap(bitmap, x.toFloat(), y.toFloat(), paint)

        return output
    }

    /**
     * Apply blur effect to bitmap.
     */
    fun blur(context: Context, bitmap: Bitmap, radius: Float = BLUR_RADIUS_MAX): Bitmap? {
        if (radius <= 0 || radius > BLUR_RADIUS_MAX) return null

        return try {
            val input = Allocation.createFromBitmap(bitmap)
            val output = Allocation.createTyped(bitmap.width, bitmap.height, Element.U8_4(context, RenderScript))

            val script = ScriptIntrinsicBlur.create(context, Element.U8_4(context, RenderScript))
            script.setRadius(radius)
            script.setInput(input)
            script.forEach(output)

            val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
            output.copyTo(result)

            input.destroy()
            output.destroy()

            result
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Apply fast blur (stack blur approximation).
     */
    fun fastBlur(bitmap: Bitmap, radius: Int = 10): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val output = bitmap.copy(Bitmap.Config.ARGB_8888, true)

        // Simple box blur for performance
        var sum = 0L
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        // Horizontal pass
        for (y in 0 until height) {
            sum = 0L
            for (x in 0 until width) {
                if (x == 0) {
                    for (i in -radius..radius) {
                        val idx = (y * width + i.coerceIn(0, width - 1))
                        sum += (pixels[idx] shr 16 and 0xFF).toLong()
                        sum += (pixels[idx] shr 8 and 0xFF).toLong()
                        sum += (pixels[idx] and 0xFF).toLong()
                    }
                }
            }
        }

        return output
    }

    /**
     * Flip bitmap horizontally.
     */
    fun flipHorizontal(bitmap: Bitmap): Bitmap {
        val matrix = Matrix().apply {
            preScale(-1f, 1f)
            postTranslate(bitmap.width.toFloat(), 0f)
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, false)
    }

    /**
     * Flip bitmap vertically.
     */
    fun flipVertical(bitmap: Bitmap): Bitmap {
        val matrix = Matrix().apply {
            preScale(1f, -1f)
            postTranslate(0f, bitmap.height.toFloat())
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, false)
    }

    /**
     * Overlay two bitmaps.
     */
    fun overlay(bottom: Bitmap, top: Bitmap, x: Int = 0, y: Int = 0): Bitmap {
        val output = bottom.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(output)
        canvas.drawBitmap(top, x.toFloat(), y.toFloat(), null)
        return output
    }

    /**
     * Get bitmap byte size.
     */
    fun getByteSize(bitmap: Bitmap): Long {
        return bitmap.byteCount.toLong()
    }

    /**
     * Check if bitmap fits in memory.
     */
    fun fitsInMemory(bitmap: Bitmap, maxMemoryBytes: Long): Boolean {
        return getByteSize(bitmap) <= maxMemoryBytes
    }

    /**
     * Recycle bitmap safely.
     */
    fun recycle(bitmap: Bitmap?) {
        bitmap?.let {
            if (!it.isRecycled) {
                it.recycle()
            }
        }
    }
}