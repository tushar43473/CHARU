package com.app.clipsteronline.upload.editor.text

class FontManager {
    private val cache = mutableMapOf<String, TypefaceHandle>()

    fun configure() = Unit

    fun resolve(fontFamily: String): TypefaceHandle {
        val key = fontFamily.trim().ifBlank { DEFAULT_FONT }
        return cache.getOrPut(key) { TypefaceHandle(key, isFallback = key == DEFAULT_FONT) }
    }

    fun clear() = cache.clear()

    data class TypefaceHandle(
        val family: String,
        val isFallback: Boolean,
    )

    companion object {
        const val DEFAULT_FONT = "sans-serif"
    }
}
