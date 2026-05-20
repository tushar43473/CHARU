package com.app.clipsteronline.upload.editor.timeline.thumbnails

import android.graphics.Bitmap
import com.app.clipsteronline.upload.editor.core.cache.ThumbnailCache

class ThumbnailRepository(
    private val decoder: ThumbnailDecoder = ThumbnailDecoder(),
    private val cache: ThumbnailCache = ThumbnailCache(),
) {
    fun getThumbnail(path: String, frameUs: Long, width: Int, height: Int): Bitmap? {
        val key = "$path:$frameUs:${width}x$height"
        cache.get(key)?.let { return it }
        val decoded = decoder.decode(path, frameUs, width, height) ?: return null
        cache.put(key, decoded)
        return decoded
    }

    fun clear() = cache.clear()
}
