package com.app.clipsteronline.upload.editor.database

class MediaCacheDao {
    private val cache = mutableMapOf<String, MediaCacheEntity>()

    fun initialize() = Unit

    @Synchronized
    fun put(entity: MediaCacheEntity) {
        cache[entity.uri] = entity
    }

    @Synchronized
    fun get(uri: String): MediaCacheEntity? = cache[uri]

    @Synchronized
    fun remove(uri: String) {
        cache.remove(uri)
    }

    data class MediaCacheEntity(
        val uri: String,
        val width: Int,
        val height: Int,
        val durationMs: Long,
        val cachedAtMs: Long = System.currentTimeMillis(),
    )
}
