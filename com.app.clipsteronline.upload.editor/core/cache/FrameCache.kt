package com.app.clipsteronline.upload.editor.core.cache

import android.graphics.Bitmap
import android.util.Log
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/**
 * Cache for extracted video frames with optimized timeline scrubbing support.
 * Provides frame reuse and handles low memory situations.
 */
class FrameCache(
    private val maxFrames: Int = DEFAULT_MAX_FRAMES,
    private val reuseEnabled: Boolean = true
) {
    private val frameCache = ConcurrentHashMap<Long, CachedFrame>()
    private val uriIndex = ConcurrentHashMap<String, MutableSet<Long>>()

    private val currentSize = AtomicInteger(0)
    private val hits = AtomicInteger(0)
    private val misses = AtomicInteger(0)
    private val reuses = AtomicInteger(0)

    var isEnabled = true

    companion object {
        private const val TAG = "FrameCache"
        const val DEFAULT_MAX_FRAMES = 60

        private const val FRAME_MARGIN_MS = 100L
    }

    /**
     * Get frame at time.
     */
    fun getFrame(uri: String, timeMs: Long): Bitmap? {
        if (!isEnabled) return null

        val key = makeKey(uri, timeMs)
        val cached = frameCache[key] ?: run {
            misses.incrementAndGet()
            return null
        }

        // Update access time
        cached.lastAccessTime = System.currentTimeMillis()
        hits.incrementAndGet()
        return cached.bitmap
    }

    /**
     * Put frame in cache.
     */
    fun putFrame(uri: String, timeMs: Long, bitmap: Bitmap): Boolean {
        if (!isEnabled) {
            bitmap.recycle()
            return false
        }

        val key = makeKey(uri, timeMs)

        // Evict if needed
        while (currentSize.get() >= maxFrames && frameCache.isNotEmpty()) {
            evictOldest()
        }

        val cached = CachedFrame(bitmap, uri, timeMs)
        frameCache[key] = cached

        // Track by URI
        uriIndex.getOrPut(uri) { mutableSetOf() }.add(timeMs)

        currentSize.incrementAndGet()
        return true
    }

    /**
     * Get frame for reuse if available.
     */
    fun getFrameForReuse(uri: String, targetTimeMs: Long): Bitmap? {
        if (!reuseEnabled) return null

        // Try to find nearby frame
        val uriFrames = uriIndex[uri] ?: return null

        // Find closest frame
        var closestFrame: CachedFrame? = null
        var closestDiff = Long.MAX_VALUE

        for (timeMs in uriFrames) {
            val diff = kotlin.math.abs(timeMs - targetTimeMs)
            if (diff < closestDiff && diff <= FRAME_MARGIN_MS) {
                val cached = frameCache[makeKey(uri, timeMs)]
                if (cached != null && !cached.isReused) {
                    closestDiff = diff
                    closestFrame = cached
                }
            }
        }

        return if (closestFrame != null) {
            closestFrame.isReused = true
            reuses.incrementAndGet()
            closestFrame.bitmap
        } else null
    }

    /**
     * Mark frame as used (no reuse).
     */
    fun markUsed(uri: String, timeMs: Long) {
        val key = makeKey(uri, timeMs)
        frameCache[key]?.isReused = false
    }

    /**
     * Invalidate frames for URI.
     */
    fun invalidate(uri: String) {
        val times = uriIndex.remove(uri) ?: return

        times.forEach { time ->
            val key = makeKey(uri, time)
            frameCache.remove(key)?.bitmap?.recycle()
        }

        currentSize.addAndGet(-times.size)
    }

    /**
     * Invalidate frame at time.
     */
    fun invalidate(uri: String, timeMs: Long) {
        val key = makeKey(uri, timeMs)
        frameCache.remove(key)?.bitmap?.recycle()
        uriIndex[uri]?.remove(timeMs)
        currentSize.decrementAndGet()
    }

    /**
     * Clear all frames.
     */
    fun clear() {
        frameCache.values.forEach { it.bitmap.recycle() }
        frameCache.clear()
        uriIndex.clear()
        currentSize.set(0)
    }

    /**
     * Get cache size.
     */
    fun size(): Int = currentSize.get()

    /**
     * Get hit count.
     */
    fun hitCount(): Int = hits.get()

    /**
     * Get miss count.
     */
    fun missCount(): Int = misses.get()

    /**
     * Get reuse count.
     */
    fun reuseCount(): Int = reuses.get()

    /**
     * Get hit rate.
     */
    fun hitRate(): Float {
        val total = hits.get() + misses.get()
        return if (total > 0) hits.get().toFloat() / total else 0f
    }

    /**
     * Handle low memory.
     */
    fun onLowMemory() {
        // Clear half of the cache
        val targetSize = maxFrames / 2
        while (currentSize.get() > targetSize && frameCache.isNotEmpty()) {
            evictOldest()
        }
        isEnabled = currentSize.get() > 0
    }

    /**
     * Resume cache after low memory.
     */
    fun resume() {
        isEnabled = true
    }

    /**
     * Generate cache key.
     */
    private fun makeKey(uri: String, timeMs: Long): String {
        return "${uri}_$timeMs"
    }

    /**
     * Evict oldest entry.
     */
    private fun evictOldest() {
        var oldestEntry: CachedFrame? = null
        var oldestKey: String? = null
        var oldestTime = Long.MAX_VALUE

        for ((key, frame) in frameCache) {
            if (frame.lastAccessTime < oldestTime) {
                oldestTime = frame.lastAccessTime
                oldestKey = key
                oldestEntry = frame
            }
        }

        if (oldestKey != null && oldestEntry != null) {
            frameCache.remove(oldestKey)?.bitmap?.recycle()
            uriIndex[oldestEntry.uri]?.remove(oldestEntry.timeMs)
            currentSize.decrementAndGet()
        }
    }

    /**
     * Cached frame wrapper.
     */
    private data class CachedFrame(
        val bitmap: Bitmap,
        val uri: String,
        val timeMs: Long,
        var lastAccessTime: Long = System.currentTimeMillis(),
        var isReused: Boolean = false
    )
}