package com.app.clipsteronline.upload.editor.core.cache

class MemoryCache<K, V>(private val max: Int) {
    init {
        require(max > 0) { "max entries must be > 0" }
    }

    private val map = object : LinkedHashMap<K, V>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>?): Boolean = size > max
    }

    @Synchronized
    fun put(key: K, value: V) {
        map[key] = value
    }

    @Synchronized
    fun get(key: K): V? = map[key]

    @Synchronized
    fun size(): Int = map.size

    @Synchronized
    fun clear() = map.clear()
}
