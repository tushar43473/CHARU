package com.app.clipsteronline.upload.editor.timeline.thumbnails

import android.graphics.Bitmap
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class ThumbnailLoader(
    private val repository: ThumbnailRepository = ThumbnailRepository(),
    private val generator: ThumbnailGenerator = ThumbnailGenerator(),
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    suspend fun loadRange(
        path: String,
        startUs: Long,
        endUs: Long,
        zoom: Float,
        viewportWidthPx: Int,
        thumbWidth: Int,
        thumbHeight: Int,
    ): List<ThumbnailItem> = coroutineScope {
        val times = generator.sampleFrameTimesUs(startUs, endUs, zoom, viewportWidthPx)
        times.map { t ->
            async(dispatcher) {
                ThumbnailItem(t, repository.getThumbnail(path, t, thumbWidth, thumbHeight))
            }
        }.awaitAll().filter { it.bitmap != null }
    }
}

data class ThumbnailItem(
    val frameUs: Long,
    val bitmap: Bitmap?,
)
