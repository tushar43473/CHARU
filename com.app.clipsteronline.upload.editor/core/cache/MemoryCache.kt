package com.app.clipsteronline.upload.editor.core.cache

import android.util.Log
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/**
 * Generic in-memory cache system with configurable size and LRU eviction.
 * Provides cache hit/miss tracking and safe memory cleanup.
 */
class MemoryCache<K : Any, V : Any>(
    private val maxSizeBytes: Long = DEFAULT_MAX_SIZE,
    private val name: String = "MemoryCache"
) {
    private val cache = ConcurrentHashMap<K, CacheEntry<V>>()
    private val accessOrder = LinkedHashMap<K, CacheEntry<V>>(32, 0.75f, true)

    private val currentSize = AtomicLong(0)
    private val hits = AtomicInteger(0)
    private val misses = AtomicInteger(0)
    private val evictions = AtomicInteger(0)

    var isEnabled = true

    companion object {
        private const val TAG = "MemoryCache"
        const val DEFAULT_MAX_SIZE = 50 * 1024 * 1024L // 50MB

        const val PRIORITY_LOW = 0
        const val PRIORITY_NORMAL = 1
        const val PRIORITY_HIGH = 2
    }

    /**
     * Get value from cache.
     */
    fun get(key: K): V? {
        if (!isEnabled) return null

        val entry = cache[key] ?: run {
            misses.incrementAndGet()
            return@run null
        }

        // Update access order for LRU
        synchronized(accessOrder) {
            accessOrder.remove(key)
            accessOrder[key] = entry
        }

        hits.incrementAndGet()
        return entry.value
    }

    /**
     * Put value in cache.
     */
    fun put(key: K, value: V, sizeBytes: Long? = null): Boolean {
        if (!isEnabled) return false

        val valueSize = sizeBytes ?: estimateSize(value)
        if (valueSize > maxSizeBytes) {
            return false
        }

        // Remove old entry if exists
        val existingEntry = cache[key]
        if (existingEntry != null) {
            currentSize.addAndGet(-existingEntry.sizeBytes)
        }

        // Evict if needed
        while (currentSize.get() + valueSize > maxSizeBytes && cache.isNotEmpty()) {
            evictOldest()
        }

        // Add new entry
        val entry = CacheEntry(value, valueSize, 0)
        cache[key] = entry

        synchronized(accessOrder) {
            accessOrder[key] = entry
        }

        currentSize.addAndGet(valueSize)
        return true
    }

    /**
     * Put with priority.
     */
    fun put(key: K, value: V, priority: Int, sizeBytes: Long? = null): Boolean {
        return put(key, value, sizeBytes)
    }

    /**
     * Check if key exists.
     */
    fun containsKey(key: K): Boolean {
        return cache.containsKey(key)
    }

    /**
     * Remove entry.
     */
    fun remove(key: K): Boolean {
        val entry = cache.remove(key) ?: return false

        synchronized(accessOrder) {
            accessOrder.remove(key)
        }

        currentSize.addAndGet(-entry.sizeBytes)
        return true
    }

    /**
     * Clear all cache entries.
     */
    fun clear() {
        cache.clear()
        synchronized(accessOrder) {
            accessOrder.clear()
        }
        currentSize.set(0)
        Log.d(TAG, "$name cleared")
    }

    /**
     * Trim to target size.
     */
    fun trimToSize(targetBytes: Long): Int {
        var trimmed = 0

        while (currentSize.get() > targetBytes && cache.isNotEmpty()) {
            evictOldest()
            trimmed++
        }

        return trimmed
    }

    /**
     * Get current size in bytes.
     */
    fun size(): Long = currentSize.get()

    /**
     * Get number of entries.
     */
    fun count(): Int = cache.size()

    /**
     * Get hit rate percentage.
     */
    fun hitRate(): Float {
        val total = hits.get() + misses.get()
        return if (total > 0) hits.get().toFloat() / total else 0f
    }

    /**
     * Get hit count.
     */
    fun hitCount(): Int = hits.get()

    /**
     * Get miss count.
     */
    fun missCount(): Int = misses.get()

    /**
     * Get eviction count.
     */
    fun evictionCount(): Int = evictions.get()

    /**
     * Get cache statistics.
     */
    fun getStats(): CacheStats {
        return CacheStats(
            currentSize = currentSize.get(),
            maxSize = maxSizeBytes,
            entryCount = cache.size(),
            hits = hits.get(),
            misses = misses.get(),
            evictions = evictions.get(),
            hitRate = hitRate()
        )
    }

    /**
     * Set enabled state.
     */
    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
        if (!enabled) {
            clear()
        }
    }

    /**
     * Evict oldest entry.
     */
    private fun evictOldest() {
        val oldestKey: K? = synchronized(accessOrder) {
            accessOrder.keys().toList().firstOrNull()
        } ?: return

        oldestKey.let { key ->
            val entry = cache.remove(key)
            if (entry != null) {
                synchronized(accessOrder) {
                    accessOrder.remove(key)
                }
                currentSize.addAndGet(-entry.sizeBytes)
                evictions.incrementAndGet()
            }
        }
    }

    /**
     * Estimate size of value.
     */
    private fun estimateSize(value: V): Long {
        return when (value) {
            is android.graphics.Bitmap -> value.byteCount.toLong()
            is ByteArray -> value.size.toLong()
            is CharArray -> value.size * 2L
            is String -> value.length * 2L
            else -> 1024 // Default estimate
        }
    }

    /**
     * Cache entry wrapper.
     */
    private data class CacheEntry<V>(
        val value: V,
        val sizeBytes: Long,
        val priority: Int
    )
}

/**
 * Cache statistics.
 */
data class CacheStats(
    val currentSize: Long,
    val maxSize: Long,
    val entryCount: Int,
    val hits: Int,
    val misses: Int,
    val evictions: Int,
    val hitRate: Float
) {
    val usagePercent: Float
        get() = if (maxSize > 0) currentSize.toFloat() / maxSize else 0f

    fun prettyPrint(): String {
        return buildString {
            appendLine("Cache Stats:")
            appendLine("  Entries: $entryCount")
            appendLine("  Size: ${formatSize(currentSize)} / ${formatSize(maxSize)} (${(usagePercent * 100).toInt()}%)")
            appendLine("  Hits: $hits, Misses: $misses, Hit Rate: ${(hitRate * 100).toInt()}%")
            appendLine("  Evictions: $evictions")
        }
    }

    private fun formatSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> "${bytes / (1024 * 1024)} MB"
        }
    }
}