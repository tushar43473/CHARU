package com.app.clipsteronline.upload.editor.core.cache

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.security.MessageDigest

/**
 * Persistent disk cache for thumbnails and cached bitmaps.
 * Provides automatic cleanup and manages cache size.
 */
class DiskCache(
    private val context: Context,
    private val cacheDir: File = DEFAULT_CACHE_DIR,
    private val maxSizeBytes: Long = DEFAULT_MAX_SIZE
) {
    private val tag = "DiskCache"

    companion object {
        private const val TAG = "DiskCache"
        private val DEFAULT_CACHE_DIR by lazy {
            File(
                android.os.Environment.getExternalStoragePublicDirectory(
                    android.os.Environment.DIRECTORY_CACHE
                ),
                "video_editor"
            )
        }
        private const val DEFAULT_MAX_SIZE = 200L * 1024 * 1024 // 200MB
        private const val CACHE_FILE_EXTENSION = ".cache"
        private const val THUMBNAIL_EXTENSION = ".jpg"
        private const val COMPRESSION_QUALITY = 85
    }

    private var currentSize = 0L

    init {
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        calculateCurrentSize()
    }

    /**
     * Save bitmap to disk cache.
     */
    fun saveBitmap(key: String, bitmap: Bitmap, quality: Int = COMPRESSION_QUALITY): Boolean {
        return try {
            val file = getCacheFile(key)
            FileOutputStream(file).use { output ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, output)
            }
            currentSize += file.length()
            true
        } catch (e: IOException) {
            Log.e(TAG, "Failed to save bitmap: ${e.message}")
            false
        }
    }

    /**
     * Load bitmap from disk cache.
     */
    fun loadBitmap(key: String): Bitmap? {
        val file = getCacheFile(key)
        if (!file.exists()) return null

        return try {
            BitmapFactory.decodeFile(file.absolutePath)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load bitmap: ${e.message}")
            null
        }
    }

    /**
     * Save thumbnail specifically.
     */
    fun saveThumbnail(uri: String, timeMs: Long, bitmap: Bitmap): Boolean {
        val key = makeThumbnailKey(uri, timeMs)
        return saveBitmap(key, bitmap)
    }

    /**
     * Load thumbnail from cache.
     */
    fun loadThumbnail(uri: String, timeMs: Long): Bitmap? {
        val key = makeThumbnailKey(uri, timeMs)
        return loadBitmap(key)
    }

    /**
     * Check if thumbnail exists in cache.
     */
    fun hasThumbnail(uri: String, timeMs: Long): Boolean {
        val key = makeThumbnailKey(uri, timeMs)
        return getCacheFile(key).exists()
    }

    /**
     * Check if key exists in cache.
     */
    fun hasKey(key: String): Boolean {
        return getCacheFile(key).exists()
    }

    /**
     * Delete cached file.
     */
    fun delete(key: String): Boolean {
        val file = getCacheFile(key)
        if (!file.exists()) return false

        val deleted = file.delete()
        if (deleted) {
            currentSize -= file.length()
        }
        return deleted
    }

    /**
     * Delete thumbnail.
     */
    fun deleteThumbnail(uri: String, timeMs: Long): Boolean {
        val key = makeThumbnailKey(uri, timeMs)
        return delete(key)
    }

    /**
     * Clear all cached files.
     */
    fun clear() {
        cacheDir.listFiles()?.forEach { file ->
            if (file.isFile) {
                file.delete()
            } else if (file.isDirectory) {
                file.deleteRecursively()
            }
        }
        currentSize = 0
    }

    /**
     * Clean up old files beyond max size.
     */
    fun cleanup(): Int {
        var cleaned = 0

        while (currentSize > maxSizeBytes && cacheDir.listFiles()?.isNotEmpty() == true) {
            val oldestFile = cacheDir.listFiles()
                ?.filter { it.isFile }
                ?.minByOrNull { it.lastModified() }
                ?: break

            currentSize -= oldestFile.length()
            if (oldestFile.delete()) {
                cleaned++
            }
        }

        return cleaned
    }

    /**
     * Clean up files older than max age.
     */
    fun cleanupOldFiles(maxAgeMs: Long): Int {
        val cutoffTime = System.currentTimeMillis() - maxAgeMs
        var cleaned = 0

        cacheDir.listFiles()?.forEach { file ->
            if (file.isFile && file.lastModified() < cutoffTime) {
                currentSize -= file.length()
                if (file.delete()) {
                    cleaned++
                }
            }
        }

        return cleaned
    }

    /**
     * Get current cache size.
     */
    fun getSize(): Long = currentSize

    /**
     * Get max cache size.
     */
    fun getMaxSize(): Long = maxSizeBytes

    /**
     * Get cache directory.
     */
    fun getDirectory(): File = cacheDir

    /**
     * Get usage percentage.
     */
    fun getUsagePercent(): Float {
        return if (maxSizeBytes > 0) currentSize.toFloat() / maxSizeBytes else 0f
    }

    /**
     * Get number of cached files.
     */
    fun getFileCount(): Int {
        return cacheDir.listFiles()?.size ?: 0
    }

    /**
     * Get cache file for key.
     */
    private fun getCacheFile(key: String): File {
        val safeName = sanitizeFileName(key)
        return File(cacheDir, "$safeName$CACHE_FILE_EXTENSION")
    }

    /**
     * Make thumbnail cache key.
     */
    private fun makeThumbnailKey(uri: String, timeMs: Long): String {
        return "${hashKey(uri)}_$timeMs"
    }

    /**
     * Hash key for safe file name.
     */
    private fun hashKey(key: String): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(key.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

    /**
     * Sanitize file name.
     */
    private fun sanitizeFileName(name: String): String {
        return name.replace(Regex("[^a-zA-Z0-9]"), "_").take(50)
    }

    /**
     * Calculate current cache size.
     */
    private fun calculateCurrentSize() {
        currentSize = cacheDir.listFiles()?.sumOf { it.length() } ?: 0L
    }
}