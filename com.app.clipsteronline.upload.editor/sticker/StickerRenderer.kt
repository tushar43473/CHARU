package com.app.clipsteronline.upload.editor.sticker

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.net.Uri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Renders stickers with transformations.
 * Supports static and animated stickers.
 */
class StickerRenderer(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) {
    private val stickerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isFilterBitmap = true
    }

    private val bitmapCache = mutableMapOf<String, Bitmap>()
    private val matrix = Matrix()

    /**
     * Render sticker.
     */
    fun renderSticker(sticker: Sticker, canvas: Canvas, canvasWidth: Int, canvasHeight: Int) {
        if (!sticker.isVisible) return

        val bitmap = getStickerBitmap(sticker) ?: return

        // Calculate position
        val posX = sticker.x * canvasWidth
        val posY = sticker.y * canvasHeight

        // Build transformation matrix
        matrix.reset()
        matrix.postTranslate(-bitmap.width / 2f, -bitmap.height / 2f)
        matrix.postScale(sticker.scale, sticker.scale)

        if (sticker.isFlippedHorizontal) {
            matrix.postScale(-1f, 1f)
        }
        if (sticker.isFlippedVertical) {
            matrix.postScale(1f, -1f)
        }

        matrix.postRotate(sticker.rotation)
        matrix.postTranslate(posX, posY)

        // Apply alpha
        stickerPaint.alpha = (sticker.alpha * 255).toInt()

        // Draw
        canvas.drawBitmap(bitmap, matrix, stickerPaint)
    }

    /**
     * Render selection handles.
     */
    fun renderSelectionHandles(sticker: Sticker, canvas: Canvas, canvasWidth: Int, canvasHeight: Int) {
        if (!sticker.isVisible || sticker.type == StickerType.ANIMATED) return

        val bitmap = getStickerBitmap(sticker) ?: return
        val posX = sticker.x * canvasWidth
        val posY = sticker.y * canvasHeight

        val size = (minOf(bitmap.width, bitmap.height) * sticker.scale).toInt()
        val halfSize = size / 2

        // Selection rectangle
        stickerPaint.style = Paint.Style.STROKE
        stickerPaint.strokeWidth = 2f
        stickerPaint.color = 0xFFFFD700.toInt() // Gold
        stickerPaint.alpha = 255

        canvas.drawRect(
            posX - halfSize,
            posY - halfSize,
            posX + halfSize,
            posY + halfSize,
            stickerPaint
        )

        // Corner handles
        val handleRadius = 8f
        stickerPaint.style = Paint.Style.FILL
        stickerPaint.alpha = 255

        val corners = listOf(
            posX - halfSize to posY - halfSize,
            posX + halfSize to posY - halfSize,
            posX + halfSize to posY + halfSize,
            posX - halfSize to posY + halfSize
        )

        for ((hx, hy) in corners) {
            canvas.drawCircle(hx, hy, handleRadius, stickerPaint)
        }
    }

    /**
     * Get sticker bitmap.
     */
    private fun getStickerBitmap(sticker: Sticker): Bitmap? {
        // Check cache
        bitmapCache[sticker.id]?.let { return it }

        // Load if animated
        if (sticker.type == StickerType.ANIMATED || sticker.type == StickerType.GIF) {
            return null
        }

        // Load static bitmap
        return try {
            context.contentResolver.openInputStream(sticker.uri)?.use { stream ->
                Bitmap.decodeStream(stream)
            }?.also { bitmapCache[sticker.id] = it }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Clear cache.
     */
    fun clearCache() {
        bitmapCache.values.forEach { it.recycle() }
        bitmapCache.clear()
    }

    /**
     * Preload sticker.
     */
    fun preloadSticker(sticker: Sticker) {
        if (!bitmapCache.containsKey(sticker.id)) {
            getStickerBitmap(sticker)
        }
    }

    /**
     * Remove from cache.
     */
    fun removeFromCache(stickerId: String) {
        bitmapCache.remove(stickerId)?.recycle()
    }

    /**
     * Release resources.
     */
    fun release() {
        clearCache()
    }
}