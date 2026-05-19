package com.app.clipsteronline.upload.editor.timeline.thumbnails

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Central thumbnail repository.
 * Coordinates caching and retrieval.
 */
class ThumbnailRepository(
    private val context: Context
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val generator: ThumbnailGenerator
    private val loader: ThumbnailLoader
    private val decoder: ThumbnailDecoder

    init {
        generator = ThumbnailGenerator(context)
        loader = ThumbnailLoader(context, generator, Dispatchers.IO)
        decoder = ThumbnailDecoder(context)
    }

    /**
     * Get thumbnail.
     */
    fun getThumbnail(
        uri: Uri,
        timeMs: Long,
        width: Int,
        height: Int,
        onLoaded: (Bitmap?) -> Unit
    ) {
        loader.configure(width, height)
        loader.loadThumbnail(uri, timeMs, onLoaded)
    }

    /**
     * Get thumbnail synchronously.
     */
    suspend fun getThumbnailSync(
        uri: Uri,
        timeMs: Long,
        width: Int,
        height: Int
    ): Bitmap? {
        return generator.generateThumbnail(uri, timeMs, width, height)
    }

    /**
     * Preload timeline range.
     */
    fun preloadTimeline(
        uri: Uri,
        startMs: Long,
        endMs: Long,
        intervalMs: Long
    ) {
        loader.preloadRange(uri, startMs, endMs, intervalMs)
    }

    /**
     * Preload clip thumbnails.
     */
    fun preloadClip(
        uri: Uri,
        startMs: Long,
        durationMs: Long,
        thumbnailCount: Int,
        width: Int,
        height: Int
    ) {
        if (thumbnailCount <= 0) return

        val interval = durationMs / thumbnailCount
        val endMs = startMs + durationMs

        preloadTimeline(uri, startMs, endMs, interval)
    }

    /**
     * Cancel off-screen.
     */
    fun cancelOffscreen(uri: Uri, visibleStartMs: Long, visibleEndMs: Long) {
        loader.cancelOffscreen(uri, visibleStartMs, visibleEndMs)
    }

    /**
     * Clear cache.
     */
    fun clearCache() {
        loader.clearCache()
    }

    /**
     * Cancel all.
     */
    fun cancelAll() {
        loader.cancelAll()
    }

    /**
     * Release resources.
     */
    fun release() {
        cancelAll()
        generator.releaseAll()
        decoder.release()
    }

    /**
     * Get decoder.
     */
    fun getDecoder(): ThumbnailDecoder = decoder
}