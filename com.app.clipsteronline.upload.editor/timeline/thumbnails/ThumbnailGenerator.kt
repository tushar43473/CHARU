package com.app.clipsteronline.upload.editor.timeline.thumbnails

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

/**
 * Generator for video thumbnails.
 * Extracts frames using MediaMetadataRetriever.
 */
class ThumbnailGenerator(
    private val context: Context
) {
    private val retrieverPool = ConcurrentHashMap<String, MediaMetadataRetriever>()
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO

    /**
     * Generate thumbnail at time position.
     */
    suspend fun generateThumbnail(
        uri: Uri,
        timeMs: Long,
        width: Int,
        height: Int
    ): Bitmap? = withContext(ioDispatcher) {
        val retriever = getRetriever(uri) ?: return@withContext null

        try {
            retriever.getScaledFrameAtTime(
                timeMs * 1000,
                MediaMetadataRetriever.OPTION_CLOSEST_SYNC,
                width.toLong(),
                height.toLong()
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Generate frame at specific frame number.
     */
    suspend fun generateFrameAtFrame(
        uri: Uri,
        frameNumber: Int,
        frameRate: Int = 30,
        width: Int,
        height: Int
    ): Bitmap? {
        val timeMs = frameNumber * 1000L / frameRate
        return generateThumbnail(uri, timeMs, width, height)
    }

    /**
     * Generate multiple thumbnails at intervals.
     */
    suspend fun generateTimelineThumbnails(
        uri: Uri,
        intervals: List<Long>,
        width: Int,
        height: Int,
        onThumbnailGenerated: (Int, Bitmap?) -> Unit
    ) = withContext(ioDispatcher) {
        intervals.forEachIndexed { index, timeMs ->
            val thumbnail = generateThumbnail(uri, timeMs, width, height)
            onThumbnailGenerated(index, thumbnail)
        }
    }

    /**
     * Get video duration.
     */
    suspend fun getVideoDuration(uri: Uri): Long = withContext(ioDispatcher) {
        val retriever = getRetriever(uri)
        retriever?.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
    }

    /**
     * Get video dimensions.
     */
    suspend fun getVideoDimensions(uri: Uri): Pair<Int, Int> = withContext(ioDispatcher) {
        val retriever = getRetriever(uri)
        val width = retriever?.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 0
        val height = retriever?.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 0
        width to height
    }

    /**
     * Release retriever for URI.
     */
    fun releaseRetriever(uri: Uri) {
        retrieverPool.remove(uri.toString())?.release()
    }

    /**
     * Release all retrievers.
     */
    fun releaseAll() {
        retrieverPool.values.forEach { it.release() }
        retrieverPool.clear()
    }

    /**
     * Get pooled retriever.
     */
    private fun getRetriever(uri: Uri): MediaMetadataRetriever? {
        val key = uri.toString()
        return retrieverPool.getOrPut(key) {
            MediaMetadataRetriever().apply {
                setDataSource(context, uri)
            }
        }
    }
}