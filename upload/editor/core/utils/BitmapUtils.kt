package upload.editor.core.utils

import android.content.ContentResolver
import android.graphics.*
import android.net.Uri
import kotlin.math.max
import kotlin.math.min

object BitmapUtils {
    fun scale(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        require(maxWidth > 0 && maxHeight > 0)
        val ratio = min(maxWidth.toFloat() / bitmap.width, maxHeight.toFloat() / bitmap.height)
        if (ratio >= 1f) return bitmap
        return Bitmap.createScaledBitmap(bitmap, (bitmap.width * ratio).toInt(), (bitmap.height * ratio).toInt(), true)
    }

    fun cropCenter(bitmap: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        require(targetWidth > 0 && targetHeight > 0)
        val width = min(targetWidth, bitmap.width)
        val height = min(targetHeight, bitmap.height)
        val x = (bitmap.width - width) / 2
        val y = (bitmap.height - height) / 2
        return Bitmap.createBitmap(bitmap, x, y, width, height)
    }

    fun optimizeThumbnail(bitmap: Bitmap, sizePx: Int = 320): Bitmap = scale(bitmap, sizePx, sizePx)

    fun compress(bitmap: Bitmap, format: Bitmap.CompressFormat, quality: Int): ByteArray {
        val stream = java.io.ByteArrayOutputStream()
        bitmap.compress(format, quality.coerceIn(1, 100), stream)
        return stream.toByteArray()
    }

    fun decodeSampledBitmap(contentResolver: ContentResolver, uri: Uri, reqWidth: Int, reqHeight: Int): Bitmap? {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
        options.inJustDecodeBounds = false
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        return contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
    }

    fun blur(bitmap: Bitmap, radius: Int): Bitmap {
        val r = radius.coerceIn(1, 25)
        val scaled = Bitmap.createScaledBitmap(bitmap, max(1, bitmap.width / 4), max(1, bitmap.height / 4), true)
        val output = scaled.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        repeat(r) {
            paint.maskFilter = BlurMaskFilter((it + 1).toFloat(), BlurMaskFilter.Blur.NORMAL)
            canvas.drawBitmap(output, 0f, 0f, paint)
        }
        return Bitmap.createScaledBitmap(output, bitmap.width, bitmap.height, true)
    }

    fun rounded(bitmap: Bitmap, cornerRadiusPx: Float): Bitmap {
        val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val rect = RectF(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat())
        canvas.drawRoundRect(rect, cornerRadiusPx, cornerRadiusPx, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return output
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height, width) = options.outHeight to options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            var halfHeight = height / 2
            var halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}
