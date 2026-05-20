package com.app.clipsteronline.upload.editor.core.cache

import android.graphics.Bitmap
import android.util.LruCache

class FrameCache(maxEntries: Int = 128) {
    private val cache = object : LruCache<Long, Bitmap>(maxEntries.coerceAtLeast(16)) {
        override fun sizeOf(key: Long, value: Bitmap): Int = value.byteCount / 1024
    }

    @Synchronized
    fun get(frameUs: Long): Bitmap? = cache.get(frameUs)

    @Synchronized
    fun put(frameUs: Long, bitmap: Bitmap) {
        cache.put(frameUs.coerceAtLeast(0L), bitmap)
    }

    @Synchronized
    fun remove(frameUs: Long) {
        cache.remove(frameUs)
    }

    @Synchronized
    fun clear() {
        cache.evictAll()
    }
}
