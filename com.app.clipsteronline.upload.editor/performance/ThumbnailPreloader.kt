package com.app.clipsteronline.upload.editor.performance

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Thumbnail preloader.
 * Predictive loading, background queue.
 */
class ThumbnailPreloader(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) {
    private val preloadQueue = ConcurrentLinkedQueue<PreloadTask>()
    private var preloadJob: Job? = null

    private var isPreloading = false
    private var maxConcurrentLoads = 3

    /**
     * Preload thumbnails.
     */
    fun preloadRange(
        uris: List<Uri>,
        startMs: Long,
        endMs: Long,
        onLoaded: (Long, Bitmap) -> Unit
    ) {
        uris.forEachIndexed { index, uri ->
            val timeMs = estimateTimestamp(index, startMs, endMs)
            addToQueue(PreloadTask(uri, timeMs, onLoaded))
        }

        ensurePreloading()
    }

    /**
     * Add to preload queue.
     */
    private fun addToQueue(task: PreloadTask) {
        if (!preloadQueue.any { t -> t.uri == task.uri && t.timeMs == task.timeMs }) {
            preloadQueue.add(task)
        }
    }

    /**
     * Ensure background worker.
     */
    private fun ensurePreloading() {
        if (isPreloading || preloadQueue.isEmpty()) return

        isPreloading = true
        preloadJob = scope.launch {
            while (isActive && preloadQueue.isNotEmpty()) {
                val tasks = (0 until maxConcurrentLoads).mapNotNull { preloadQueue.poll() }

                tasks.forEach { task ->
                    try {
                        loadThumbnail(task.uri)?.let { bitmap ->
                            withContext(Dispatchers.Main) {
                                task.onLoaded(task.timeMs, bitmap)
                            }
                        }
                    } catch (e: Exception) {
                        // Log error
                    }
                }

                delay(100)
            }
            isPreloading = false
        }
    }

    /**
     * Cancel preload.
     */
    fun cancel() {
        preloadJob?.cancel()
        preloadQueue.clear()
        isPreloading = false
    }

    /**
     * Load thumbnail.
     */
    private fun loadThumbnail(uri: Uri): Bitmap? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                val options = BitmapFactory.Options().apply {
                    inSampleSize = 2
                    inPreferredConfig = Bitmap.Config.RGB_565
                }
                BitmapFactory.decodeStream(input, null, options)
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Estimate timestamp.
     */
    private fun estimateTimestamp(index: Int, startMs: Long, endMs: Long): Long {
        val duration = endMs - startMs
        return startMs + (duration * index / 10)
    }
}

/**
 * Preload task.
 */
data class PreloadTask(
    val uri: Uri,
    val timeMs: Long,
    val onLoaded: (Long, Bitmap) -> Unit
)

/**
 * Visible item detector.
 */
class VisibleItemDetector(
    private val firstVisible: Int,
    private val lastVisible: Int,
    private val totalCount: Int
) {
    fun getPriorityOrder(): List<Int> {
        val center = (firstVisible + lastVisible) / 2
        val order = mutableListOf<Int>()

        order.add(center)

        var i = 1
        while (firstVisible + i <= lastVisible || center - i >= firstVisible) {
            if (center + i <= lastVisible) order.add(center + i)
            if (center - i >= firstVisible) order.add(center - i)
            i++
        }

        return order
    }
}