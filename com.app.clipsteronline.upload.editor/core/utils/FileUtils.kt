package com.app.clipsteronline.upload.editor.core.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.webkit.MimeTypeMap
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * File handling utilities for media and project files.
 * Provides validation, file operations, and MIME type detection.
 */
object FileUtils {

    private const val EDITOR_FOLDER = "VideoEditor"
    private const val PROJECTS_FOLDER = "projects"
    private const val EXPORTS_FOLDER = "exports"
    private const val TEMP_FOLDER = "temp"
    private const val CACHE_FOLDER = "cache"

    private val VIDEO_EXTENSIONS = setOf("mp4", "mkv", "webm", "mov", "avi", "3gp", "ts")
    private val IMAGE_EXTENSIONS = setOf("jpg", "jpeg", "png", "gif", "webp", "bmp")
    private val AUDIO_EXTENSIONS = setOf("mp3", "aac", "ogg", "wav", "flac", "m4a", "wma")

    /**
     * Validate if file is a supported media file.
     */
    fun isMediaFile(file: File): Boolean {
        val extension = file.extension.lowercase()
        return isVideoFile(extension) || isImageFile(extension) || isAudioFile(extension)
    }

    /**
     * Validate if file is a supported video file.
     */
    fun isVideoFile(file: File): Boolean {
        return isVideoFile(file.extension.lowercase())
    }

    /**
     * Check if extension is a video.
     */
    fun isVideoFile(extension: String): Boolean {
        return extension.lowercase() in VIDEO_EXTENSIONS
    }

    /**
     * Check if extension is an image.
     */
    fun isImageFile(extension: String): Boolean {
        return extension.lowercase() in IMAGE_EXTENSIONS
    }

    /**
     * Check if extension is audio.
     */
    fun isAudioFile(extension: String): Boolean {
        return extension.lowercase() in AUDIO_EXTENSIONS
    }

    /**
     * Get MIME type from file extension.
     */
    fun getMimeType(file: File): String? {
        return getMimeType(file.extension)
    }

    /**
     * Get MIME type from extension.
     */
    fun getMimeType(extension: String): String? {
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.lowercase())
    }

    /**
     * Get MIME type from URI.
     */
    fun getMimeType(context: Context, uri: Uri): String? {
        return context.contentResolver.getType(uri)
    }

    /**
     * Generate unique export file name.
     */
    fun generateExportFileName(prefix: String = "export"): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        return "${prefix}_$timestamp"
    }

    /**
     * Generate unique project file name.
     */
    fun generateProjectFileName(name: String): String {
        return name.replace(" ", "_").replace(Regex("[^a-zA-Z0-9_]"), "")
    }

    /**
     * Create editor directory in app storage.
     */
    fun createEditorDirectory(context: Context, folder: String): File {
        val directory = File(context.filesDir, "$EDITOR_FOLDER/$folder")
        if (!directory.exists()) {
            directory.mkdirs()
        }
        return directory
    }

    /**
     * Get projects directory.
     */
    fun getProjectsDirectory(context: Context): File {
        return createEditorDirectory(context, PROJECTS_FOLDER)
    }

    /**
     * Get exports directory.
     */
    fun getExportsDirectory(context: Context): File {
        return createEditorDirectory(context, EXPORTS_FOLDER)
    }

    /**
     * Get temp directory.
     */
    fun getTempDirectory(context: Context): File {
        return createEditorDirectory(context, TEMP_FOLDER)
    }

    /**
     * Get cache directory.
     */
    fun getCacheDirectory(context: Context): File {
        return createEditorDirectory(context, CACHE_FOLDER)
    }

    /**
     * Get external storage directory for exports.
     */
    fun getExternalExportDirectory(context: Context): File {
        val directory = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
            EDITOR_FOLDER
        )
        if (!directory.exists()) {
            directory.mkdirs()
        }
        return directory
    }

    /**
     * Copy file to destination.
     */
    fun copyFile(source: File, destination: File): Boolean {
        return try {
            FileInputStream(source).use { input ->
                FileOutputStream(destination).use { output ->
                    input.copyTo(output)
                }
            }
            true
        } catch (e: IOException) {
            false
        }
    }

    /**
     * Move file to destination.
     */
    fun moveFile(source: File, destination: File): Boolean {
        return if (copyFile(source, destination)) {
            source.delete()
        } else {
            false
        }
    }

    /**
     * Delete file safely.
     */
    fun deleteFile(file: File): Boolean {
        return try {
            if (file.isDirectory) {
                file.deleteRecursively()
            } else {
                file.delete()
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Delete file by path.
     */
    fun deleteFile(path: String): Boolean {
        return deleteFile(File(path))
    }

    /**
     * Format file size for display.
     */
    fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format("%.1f KB", bytes / 1024.0)
            bytes < 1024 * 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024))
            else -> String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024))
        }
    }

    /**
     * Format storage size with automatic unit selection.
     */
    fun formatStorageSize(bytes: Long): String {
        return formatFileSize(bytes)
    }

    /**
     * Get available storage space.
     */
    fun getAvailableSpace(directory: File): Long {
        return directory.freeSpace
    }

    /**
     * Check if sufficient storage available.
     */
    fun hasSufficientStorage(directory: File, requiredBytes: Long): Boolean {
        return getAvailableSpace(directory) >= requiredBytes
    }

    /**
     * Get file size.
     */
    fun getFileSize(file: File): Long {
        return if (file.isFile) file.length() else 0L
    }

    /**
     * Check if file exists and is valid.
     */
    fun isValidFile(path: String): Boolean {
        val file = File(path)
        return file.exists() && file.isFile && file.canRead()
    }

    /**
     * Get file extension.
     */
    fun getExtension(file: File): String {
        return file.extension.lowercase()
    }

    /**
     * Get file name without extension.
     */
    fun getNameWithoutExtension(file: File): String {
        return file.nameWithoutExtension
    }

    /**
     * Clear temp directory.
     */
    fun clearTempDirectory(context: Context): Boolean {
        return getTempDirectory(context).listFiles()?.all { it.delete() } ?: true
    }

    /**
     * Clear cache directory.
     */
    fun clearCacheDirectory(context: Context): Boolean {
        return getCacheDirectory(context).listFiles()?.all { it.delete() } ?: true
    }

    /**
     * Get total cache size.
     */
    fun getCacheSize(context: Context): Long {
        return getCacheDirectory(context).listFiles()?.sumOf { it.length() } ?: 0L
    }

    /**
     * Clean up old files from directory.
     */
    fun cleanupOldFiles(directory: File, maxAgeMs: Long): Int {
        val cutoffTime = System.currentTimeMillis() - maxAgeMs
        var deletedCount = 0

        directory.listFiles()?.forEach { file ->
            if (file.lastModified() < cutoffTime) {
                if (deleteFile(file)) deletedCount++
            }
        }

        return deletedCount
    }

    /**
     * Create safe file name from input.
     */
    fun sanitizeFileName(name: String): String {
        return name.replace(Regex("[^a-zA-Z0-9._-]"), "_")
            .take(100)
    }

    /**
     * Ensure file has correct extension.
     */
    fun ensureExtension(file: File, extension: String): File {
        val ext = extension.removePrefix(".")
        return if (file.extension.lowercase() == ext) {
            file
        } else {
            File(file.parentFile, "${file.nameWithoutExtension}.$ext")
        }
    }
}