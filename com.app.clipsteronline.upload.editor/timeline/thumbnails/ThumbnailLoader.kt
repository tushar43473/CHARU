package com.app.clipsteronline.upload.editor.timeline.thumbnails

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Async thumbnail loader with coroutine support.
 * Handles RecyclerView preload and visible range loading.
 */
class ThumbnailLoader(
    private val context: Context,
    private val generator: ThumbnailGenerator,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    private val _loadingState = MutableStateFlow(LoadingState())
    val loadingState: StateFlow<LoadingState> = _loadingState.asStateFlow()

    private val activeJobs = mutableMapOf<String, Job>()
    private val loadedThumbnails = Cache<String, Bitmap>(MAX_CACHE_SIZE)

    private var defaultWidth = DEFAULT_THUMBNAIL_SIZE
    private var defaultHeight = DEFAULT_THUMBNAIL_SIZE

    companion object {
        private const val DEFAULT_THUMBNAIL_SIZE = 200
        private const val MAX_CACHE_SIZE = 100
        private const val PRELOAD_RANGE = 5
    }

    /**
     * Configure size.
     */
    fun configure(width: Int, height: Int) {
        defaultWidth = width
        defaultHeight = height
    }

    /**
     * Load thumbnail.
     */
    fun loadThumbnail(
        uri: Uri,
        timeMs: Long,
        onLoaded: (Bitmap?) -> Unit
    ) {
        val key = getCacheKey(uri, timeMs)

        // Return cached if available
        loadedThumbnails.get(key)?.let { onLoaded(it); return }

        // Skip if already loading
        if (activeJobs.containsKey(key)) return

        val job = scope.launch {
            val bitmap = generator.generateThumbnail(uri, timeMs, defaultWidth, defaultHeight)
            bitmap?.let { loadedThumbnails.put(key, it) }

            _loadingState.value = _loadingState.value.copy(
                activeRequests = _loadingState.value.activeRequests - 1
            )

            withContext(Dispatchers.Main) {
                onLoaded(bitmap)
            }
        }

        activeJobs[key] = job
        _loadingState.value = _loadingState.value.copy(
            activeRequests = _loadingState.value.activeRequests + 1
        )
    }

    /**
     * Preload range of thumbnails.
     */
    fun preloadRange(
        uri: Uri,
        startMs: Long,
        endMs: Long,
        intervalMs: Long = 1000L
    ) {
        var timeMs = startMs
        while (timeMs <= endMs) {
            val key = getCacheKey(uri, timeMs)
            if (!loadedThumbnails.contains(key) && !activeJobs.containsKey(key)) {
                scope.launch {
                    generator.generateThumbnail(uri, timeMs, defaultWidth, defaultHeight)?.let {
                        loadedThumbnails.put(key, it)
                    }
                }
            }
            timeMs += intervalMs
        }
    }

    /**
     * Cancel off-screen loads.
     */
    fun cancelOffscreen(
        uri: Uri,
        visibleStartMs: Long,
        visibleEndMs: Long
    ) {
        val toCancel = activeJobs.filter { (key, _) ->
            val time = extractTimeFromKey(key)
            time < visibleStartMs || time > visibleEndMs
        }

        toCancel.values.forEach { it.cancel() }
        toCancel.keys.forEach { activeJobs.remove(it) }
    }

    /**
     * Clear cache.
     */
    fun clearCache() {
        loadedThumbnails.clear()
    }

    /**
     * Cancel all.
     */
    fun cancelAll() {
        activeJobs.values.forEach { it.cancel() }
        activeJobs.clear()
    }

    /**
     * Get cache key.
     */
    private fun getCacheKey(uri: Uri, timeMs: Long): String {
        return "${uri}_${timeMs}_${defaultWidth}x${defaultHeight}"
    }

    /**
     * Extract time from cache key.
     */
    private fun extractTimeFromKey(key: String): Long {
        val parts = key.split("_")
        return parts.getOrNull(1)?.toLongOrNull() ?: 0L
    }
}

/**
 * Loading state.
 */
data class LoadingState(
    val activeRequests: Int = 0
)

/**
 * Simple LRU cache.
 */
class Cache<K>(private val maxSize: Int) {
    private val map = LinkedHashMap<K, Any?>()

    fun get(key: K): Any? = synchronized(map) {
        if (!map.containsKey(key)) return@synchronized null

        map.remove(key)
        map[key] = null
        map.entries.firstOrNull { it.key == key }?.value
    }

    fun put(key: K, value: Any?) = synchronized(map) {
        map.remove(key)
        if (map.size >= maxSize) {
            map.remove(map.keys.first())
        }
        map[key] = value
    }

    fun contains(key: K): Boolean = synchronized(map) { map.containsKey(key) }

    fun clear() = synchronized(map) { map.clear() }
}