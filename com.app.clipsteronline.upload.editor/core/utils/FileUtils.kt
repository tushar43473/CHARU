package com.app.clipsteronline.upload.editor.core.utils

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FileUtils {
    private val validExtensions = setOf("mp4", "mov", "mkv", "webm", "jpg", "jpeg", "png", "webp", "mp3", "wav", "aac", "m4a")

    fun isValidMediaFile(file: File): Boolean {
        if (!file.exists() || !file.isFile || file.length() <= 0L) return false
        return file.extension.lowercase(Locale.US) in validExtensions
    }

    fun ensureEditorDirectories(context: Context): File {
        val root = File(context.filesDir, "editor")
        listOf("cache", "projects", "exports", "thumbs", "temp").forEach { File(root, it).mkdirs() }
        return root
    }

    fun createTempFile(context: Context, prefix: String, extension: String): File {
        val tempDir = File(ensureEditorDirectories(context), "temp")
        tempDir.mkdirs()
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        return File(tempDir, "${prefix}_${stamp}.${extension.removePrefix(".")}")
    }

    fun copySafely(source: File, destination: File, overwrite: Boolean = true): Boolean {
        if (!source.exists() || !source.isFile) return false
        if (destination.exists() && !overwrite) return false
        destination.parentFile?.mkdirs()
        FileInputStream(source).use { input ->
            FileOutputStream(destination, false).use { out ->
                input.channel.transferTo(0, input.channel.size(), out.channel)
            }
        }
        return destination.exists() && destination.length() == source.length()
    }

    fun moveSafely(source: File, destination: File, overwrite: Boolean = true): Boolean {
        return copySafely(source, destination, overwrite) && source.delete()
    }

    fun mimeType(pathOrUri: String): String {
        val ext = when {
            pathOrUri.startsWith("content://") || pathOrUri.startsWith("file://") -> MimeTypeMap.getFileExtensionFromUrl(pathOrUri)
            else -> File(pathOrUri).extension
        }.lowercase(Locale.US)
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: "application/octet-stream"
    }

    fun parseUri(path: String): Uri = Uri.parse(path)

    fun formatSize(bytes: Long): String {
        if (bytes < 1024) return "$bytes B"
        val units = arrayOf("KB", "MB", "GB", "TB")
        var value = bytes.toDouble()
        var idx = -1
        while (value >= 1024 && idx < units.lastIndex) {
            value /= 1024
            idx++
        }
        return "${DecimalFormat("#.##").format(value)} ${units[idx]}"
    }
}
