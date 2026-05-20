package com.app.clipsteronline.upload.editor.performance

class ThumbnailPreloader {
    private val queued = ArrayDeque<String>()

    fun configure() = Unit

    fun schedule(uris: List<String>) {
        uris.forEach { if (it.isNotBlank()) queued.addLast(it) }
    }

    fun nextBatch(limit: Int = 8): List<String> {
        val out = mutableListOf<String>()
        repeat(limit.coerceAtLeast(1)) {
            val n = queued.removeFirstOrNull() ?: return@repeat
            out += n
        }
        return out
    }

    fun clear() = queued.clear()
}
