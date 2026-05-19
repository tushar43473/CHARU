package com.app.clipsteronline.upload.editor.core.cache

import android.content.Context
import android.graphics.Bitmap
import android.util.LruCache
import android.util.Log

/**
 * LRU-based cache for video thumbnails with bitmap reuse support.
 * Provides automatic eviction and cache statistics.
 */
class ThumbnailCache(
    context: Context,
    maxSizeKb: Int = DEFAULT_CACHE_SIZE_KB
) : LruCache<String, Bitmap>(maxSizeKb * 1024) {

    private val contextRef = context.applicationContext
    private val tag = "ThumbnailCache"

    companion object {
        private const val TAG = "ThumbnailCache"
        const val DEFAULT_CACHE_SIZE_KB = 30 * 1024 // 30MB
    }

    private var hitCount = 0
    private var missCount = 0

    override fun sizeOf(key: String, bitmap: Bitmap): Int {
        return bitmap.byteCount
    }

    /**
     * Get thumbnail from cache.
     */
    fun getThumbnail(uri: String, timeMs: Long): Bitmap? {
        val key = makeKey(uri, timeMs)
        return get(key)
    }

    /**
     * Put thumbnail in cache.
     */
    fun putThumbnail(uri: String, timeMs: Long, bitmap: Bitmap): Bitmap? {
        val key = makeKey(uri, timeMs)
        return put(key, bitmap)
    }

    /**
     * Put thumbnail only if not exists.
     */
    fun putThumbnailIfAbsent(uri: String, timeMs: Long, bitmap: Bitmap): Boolean {
        val key = makeKey(uri, timeMs)
        if (get(key) != null) {
            bitmap.recycle()
            return false
        }
        put(key, bitmap)
        return true
    }

    /**
     * Generate cache key.
     */
    fun makeKey(uri: String, timeMs: Long): String {
        return "${uri}_$timeMs"
    }

    /**
     * Preload thumbnails for video.
     */
    fun preloadThumbnails(
        uri: String,
        timesMs: List<Long>,
        loader: (timeMs: Long) -> Bitmap?
    ) {
        timesMs.forEach { time ->
            if (getThumbnail(uri, time) == null) {
                loader(time)?.let { bitmap ->
                    putThumbnail(uri, time, bitmap)
                }
            }
        }
    }

    /**
     * Invalidate thumbnails for URI.
     */
    fun invalidate(uri: String) {
        // Remove all entries for this URI
        snapshot().forEach { (key, bitmap) ->
            if (key.startsWith("${uri}_")) {
                remove(key, bitmap)
            }
        }
    }

    /**
     * Clear all thumbnails.
     */
    fun clearAll() {
        evictAll()
        hitCount = 0
        missCount = 0
    }

    /**
     * Get cache statistics.
     */
    fun getStats(): CacheStatistics {
        val totalRequests = hitCount + missCount
        return CacheStatistics(
            hitCount = hitCount,
            missCount = missCount,
            totalRequests = totalRequests,
            hitRate = if (totalRequests > 0) hitCount.toFloat() / totalRequests else 0f,
            currentSize = size(),
            maxSize = maxSize()
        )
    }

    /**
     * Record cache hit.
     */
    fun recordHit() {
        hitCount++
    }

    /**
     * Record cache miss.
     */
    fun recordMiss() {
        missCount++
    }

    override fun entryRemoved(
        evicted: Boolean,
        key: String,
        oldValue: Bitmap,
        newValue: Bitmap?
    ) {
        if (evicted) {
            // Bitmap evicted - optionally save to disk cache
        }
    }
}

/**
 * Cache statistics data class.
 */
data class CacheStatistics(
    val hitCount: Int,
    val missCount: Int,
    val totalRequests: Int,
    val hitRate: Float,
    val currentSize: Int,
    val maxSize: Int
)