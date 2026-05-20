package com.app.clipsteronline.upload.editor.player

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import kotlin.math.max

class FrameExtractor {
    fun extractFrame(path: String, timeUs: Long, targetWidth: Int, targetHeight: Int): Bitmap? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(path)
            val raw = retriever.getFrameAtTime(timeUs.coerceAtLeast(0L), MediaMetadataRetriever.OPTION_CLOSEST_SYNC) ?: return null
            Bitmap.createScaledBitmap(raw, max(1, targetWidth), max(1, targetHeight), true)
        } catch (_: Throwable) {
            null
        } finally {
            runCatching { retriever.release() }
        }
    }
}
