package com.app.clipsteronline.upload.editor.core.cache

import android.graphics.Bitmap
import android.util.LruCache

class ThumbnailCache(maxEntries: Int = 256) {
    private val memory = object : LruCache<String, Bitmap>(maxEntries.coerceAtLeast(32)) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount / 1024
    }

    @Synchronized
    fun get(key: String): Bitmap? = memory.get(key)

    @Synchronized
    fun put(key: String, bitmap: Bitmap) {
        memory.put(key, bitmap)
    }

    @Synchronized
    fun clear() {
        memory.evictAll()
    }
}
