package com.app.clipsteronline.upload.editor.performance

import android.app.ActivityManager
import android.content.ComponentCallbacks2
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.util.LruCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.lang.ref.WeakReference

/**
 * Memory manager.
 * Bitmap management, LRU cache, pressure handling.
 */
class MemoryManager(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {
    private val _memoryState = MutableStateFlow(MemoryState())
    val memoryState: StateFlow<MemoryState> = _memoryState.asStateFlow()

    private var maxMemory: Long
    private val bitmapCache: LruCache<String, Bitmap>

    init {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        maxMemory = am.memoryClass * 1024 * 1024L / 4 // Use 25% of app memory

        bitmapCache = object : LruCache<String, Bitmap>((maxMemory / 5).toInt()) {
            override fun sizeOf(key: String, bitmap: Bitmap): Int = bitmap.byteCount

            override fun entryRemoved(evicted: Boolean, key: String, oldValue: Bitmap, newValue: Bitmap?) {
                if (evicted && !oldValue.isRecycled) {
                    oldValue.recycle()
                }
            }
        }
    }

    /**
     * Get bitmap from cache.
     */
    fun getBitmap(key: String): Bitmap? = bitmapCache.get(key)

    /**
     * Put bitmap in cache.
     */
    fun putBitmap(key: String, bitmap: Bitmap) {
        if (!bitmap.isRecycled) {
            bitmapCache.put(key, bitmap)
        }
    }

    /**
     * Remove bitmap.
     */
    fun removeBitmap(key: String) {
        bitmapCache.remove(key)
    }

    /**
     * Clear cache.
     */
    fun clearCache() {
        bitmapCache.evictAll()
        notifyCleanup()
    }

    /**
     * Trim to size.
     */
    fun trimToSize(maxSize: Int) {
        bitmapCache.trimToSize(maxSize)
    }

    /**
     * Get memory class.
     */
    fun getAvailableMemory(): Long {
        val rt = Runtime.getRuntime()
        return rt.freeMemory()
    }

    /**
     * Is memory low.
     */
    fun isLowMemory(): Boolean {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        am.getMemoryInfo(memInfo)
        return memInfo.lowMemory
    }

    /**
     * Get memory usage percentage.
     */
    fun getMemoryUsagePercent(): Float {
        val rt = Runtime.getRuntime()
        val used = rt.totalMemory() - rt.freeMemory()
        return (used.toFloat() / maxMemory * 100).coerceIn(0f, 100f)
    }

    /**
     * Aggressive cleanup on memory pressure.
     */
    fun onLowMemory() {
        clearCache()
        System.gc()

        _memoryState.value = _memoryState.value.copy(
            isLowMemory = true,
            cleanupAttempts = _memoryState.value.cleanupAttempts + 1
        )
    }

    /**
     * Normalize memory.
     */
    fun onNormalMemory() {
        _memoryState.value = _memoryState.value.copy(isLowMemory = false)
    }

    /**
     * Observer cleanup callback.
     */
    private fun notifyCleanup() {
        _memoryState.value = _memoryState.value.copy(lastCleanup = System.currentTimeMillis())
    }

    /**
     * Register memory callback.
     */
    fun registerMemoryCallbacks(activity: android.app.Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            activity.onTrimMemory(ComponentCallbacks2.TRIM_MEMORY_MODERATE)
        }
    }
}

/**
 * Memory state.
 */
data class MemoryState(
    val isLowMemory: Boolean = false,
    val cleanupAttempts: Int = 0,
    val lastCleanup: Long = 0L,
    val cachedBitmaps: Int = 0
)

/**
 * Pooled bitmap allocator.
 */
class BitmapPool(private val maxPoolSize: Int = 20) {
    private val pool = mutableListOf<Bitmap>()
    private val maxWidth = 1920
    private val maxHeight = 1080

    /**
     * Acquire bitmap.
     */
    fun acquire(): Bitmap? {
        return if (pool.isNotEmpty()) {
            pool.removeLast()
        } else {
            try {
                Bitmap.createBitmap(maxWidth, maxHeight, Bitmap.Config.ARGB_8888)
            } catch (e: OutOfMemoryError) {
                null
            }
        }
    }

    /**
     * Release bitmap.
     */
    fun release(bitmap: Bitmap) {
        if (!bitmap.isRecycled && pool.size < maxPoolSize) {
            bitmap.eraseColor(android.graphics.Color.TRANSPARENT)
            pool.add(bitmap)
        } else if (!bitmap.isRecycled) {
            bitmap.recycle()
        }
    }

    /**
     * Clear pool.
     */
    fun clear() {
        pool.forEach { it.recycle() }
        pool.clear()
    }
}