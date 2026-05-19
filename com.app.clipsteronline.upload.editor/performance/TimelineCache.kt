package com.app.clipsteronline.upload.editor.performance

import android.graphics.Bitmap
import android.util.LruCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Timeline cache.
 * Thumbnail/waveform caching, visible range support.
 */
class TimelineCache(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) {
    private val _cacheState = MutableStateFlow(CacheState())
    val cacheState: StateFlow<CacheState> = _cacheState.asStateFlow()

    private val thumbnailCache: LruCache<String, Bitmap>
    private val waveformCache: LruCache<String, FloatArray>
    private val metadataCache = mutableMapOf<String, ClipMetadata>()

    private var visibleRangeStart = 0L
    private var visibleRangeEnd = 0L
    private var cacheHits = 0
    private var cacheMisses = 0

    init {
        // 20MB thumbnail cache
        thumbnailCache = object : LruCache<String, Bitmap>(20 * 1024 * 1024 / 8) {
            override fun sizeOf(key: String, bitmap: Bitmap): Int = bitmap.byteCount

            override fun entryRemoved(evicted: Boolean, key: String, oldValue: Bitmap, newValue: Bitmap?) {
                if (evicted && !oldValue.isRecycled) {
                    oldValue.recycle()
                }
            }
        }

        // 10MB waveform cache
        waveformCache = object : LruCache<String, FloatArray>(10 * 1024 * 1024) {
            override fun sizeOf(key: String, value: FloatArray): Int = value.size * 4
        }
    }

    /**
     * Get thumbnail.
     */
    fun getThumbnail(clipId: String, timeMs: Long): Bitmap? {
        val key = makeThumbnailKey(clipId, timeMs)
        return thumbnailCache.get(key).also {
            if (it != null) cacheHits++ else cacheMisses++
        }
    }

    /**
     * Put thumbnail.
     */
    fun putThumbnail(clipId: String, timeMs: Long, bitmap: Bitmap) {
        val key = makeThumbnailKey(clipId, timeMs)
        if (!bitmap.isRecycled) {
            thumbnailCache.put(key, bitmap)
        }
        updateStats()
    }

    /**
     * Get waveform.
     */
    fun getWaveform(clipId: String): FloatArray? {
        return waveformCache.get(clipId).also {
            if (it != null) cacheHits++ else cacheMisses++
        }
    }

    /**
     * Put waveform.
     */
    fun putWaveform(clipId: String, waveform: FloatArray) {
        waveformCache.put(clipId, waveform)
        updateStats()
    }

    /**
     * Get metadata.
     */
    fun getMetadata(clipId: String): ClipMetadata? = metadataCache[clipId]

    /**
     * Put metadata.
     */
    fun putMetadata(clipId: String, metadata: ClipMetadata) {
        metadataCache[clipId] = metadata
    }

    /**
     * Set visible range.
     */
    fun setVisibleRange(startMs: Long, endMs: Long) {
        visibleRangeStart = startMs
        visibleRangeEnd = endMs
    }

    /**
     * Check is in visible range.
     */
    fun isVisible(timeMs: Long): Boolean = timeMs in visibleRangeStart..visibleRangeEnd

    /**
     * Clear caches.
     */
    fun clearCaches() {
        thumbnailCache.evictAll()
        waveformCache.evictAll()
        metadataCache.clear()
        updateStats()
    }

    /**
     * Trim to size.
     */
    fun trimToSize(maxBytes: Int) {
        thumbnailCache.trimToSize(maxBytes)
        updateStats()
    }

    /**
     * Get hit rate.
     */
    fun getHitRate(): Float {
        val total = cacheHits + cacheMisses
        return if (total > 0) cacheHits.toFloat() / total else 0f
    }

    /**
     * Invalidate clip.
     */
    fun invalidateClip(clipId: String) {
        // Would need to iterate and remove thumbnails
        metadataCache.remove(clipId)
    }

    private fun makeThumbnailKey(clipId: String, timeMs: Long) = "${clipId}_${timeMs / 1000}"

    private fun updateStats() {
        _cacheState.value = _cacheState.value.copy(
            thumbnailCount = thumbnailCache.size(),
            waveformCount = waveformCache.size(),
            hitRate = getHitRate()
        )
    }
}

/**
 * Cache state.
 */
data class CacheState(
    val thumbnailCount: Int = 0,
    val waveformCount: Int = 0,
    val hitRate: Float = 0f
)

/**
 * Clip metadata.
 */
data class ClipMetadata(
    val clipId: String,
    val duration: Long,
    val width: Int,
    val height: Int,
    val frameRate: Float,
    val hasAudio: Boolean
)