package com.app.clipsteronline.upload.editor.performance

class TimelineCache {
    private val cache = linkedMapOf<String, CacheEntry>()

    fun configure() = Unit

    @Synchronized
    fun put(key: String, payload: Any, cost: Int) {
        cache[key] = CacheEntry(payload, cost, System.currentTimeMillis())
    }

    @Synchronized
    fun get(key: String): Any? = cache[key]?.payload

    @Synchronized
    fun prune(maxCost: Int) {
        var cost = cache.values.sumOf { it.cost }
        val iterator = cache.entries.iterator()
        while (cost > maxCost && iterator.hasNext()) {
            val e = iterator.next()
            cost -= e.value.cost
            iterator.remove()
        }
    }

    data class CacheEntry(val payload: Any, val cost: Int, val createdAtMs: Long)
}
