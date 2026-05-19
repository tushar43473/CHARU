package com.app.clipsteronline.upload.editor.text

import android.content.Context
import android.graphics.Typeface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.io.File

/**
 * Manages fonts for text rendering.
 * Font loading and caching.
 */
class FontManager(
    private val context: Context? = null,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) {
    private val fontCache = mutableMapOf<String, Typeface>()
    private val availableFonts = mutableListOf<FontInfo>()

    /**
     * Initialize default fonts.
     */
    fun initialize() {
        availableFonts.clear()
        availableFonts.addAll(getDefaultFonts())
    }

    /**
     * Get Typeface for font name.
     */
    fun getTypeface(fontName: String): Typeface? {
        // Check cache
        fontCache[fontName]?.let { return it }

        // Load font
        val typeface = loadFont(fontName)
        typeface?.let { fontCache[fontName] = it }

        return typeface
    }

    /**
     * Load font from assets/files.
     */
    private fun loadFont(fontName: String): Typeface? {
        return try {
            // Try system font first
            Typeface.create(fontName, Typeface.NORMAL)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Get available fonts.
     */
    fun getAvailableFonts(): List<FontInfo> = availableFonts.toList()

    /**
     * Get default fonts.
     */
    private fun getDefaultFonts(): List<FontInfo> {
        return listOf(
            FontInfo("default", "Default", "sans-serif"),
            FontInfo("bold", "Bold", "sans-serif-black"),
            FontInfo("italic", "Italic", "sans-serif-light"),
            FontInfo("condensed", "Condensed", "sans-serif-condensed"),
            FontInfo("mono", "Monospace", "monospace"),
            FontInfo("serif", "Serif", "serif"),
            FontInfo("casual", "Casual", "casual"),
            FontInfo("cursive", "Cursive", "cursive")
        )
    }

    /**
     * Add custom font.
     */
    fun addCustomFont(name: String, fontFile: File): Boolean {
        return try {
            val typeface = Typeface.createFromFile(fontFile)
            fontCache[name] = typeface
            availableFonts.add(FontInfo(name, name, name))
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Clear font cache.
     */
    fun clearCache() {
        fontCache.clear()
    }
}

/**
 * Font information.
 */
data class FontInfo(
    val id: String,
    val displayName: String,
    val systemName: String
)

/**
 * Common font families.
 */
object FontFamilies {
    const val SANS_SERIF = "sans-serif"
    const val SERIF = "serif"
    const val MONOSPACE = "monospace"
    const val CASUAL = "casual"
    const val CURSIVE = "cursive"
}