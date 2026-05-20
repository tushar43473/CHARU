package upload.editor.core.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.channels.FileChannel
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FileUtils {
    private val mediaExtensions = setOf("mp4", "mov", "mkv", "webm", "mp3", "aac", "wav", "flac", "jpg", "jpeg", "png", "webp")

    fun isValidMediaFile(file: File): Boolean {
        if (!file.exists() || !file.isFile || file.length() <= 0L) return false
        val ext = file.extension.lowercase(Locale.US)
        return ext in mediaExtensions
    }

    fun generateExportFileName(prefix: String, extension: String): String {
        val safePrefix = prefix.replace("[^a-zA-Z0-9_-]".toRegex(), "_")
        val ts = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        return "${safePrefix}_${ts}.${extension.lowercase(Locale.US)}"
    }

    fun ensureEditorDirectories(context: Context): File {
        val root = File(context.filesDir, "editor")
        listOf("projects", "cache", "exports", "thumbnails", "temp").forEach { File(root, it).mkdirs() }
        return root
    }

    fun copyFile(source: File, destination: File, overwrite: Boolean = true): Boolean {
        if (!source.exists() || !source.isFile) return false
        if (destination.exists() && !overwrite) return false
        destination.parentFile?.mkdirs()
        FileInputStream(source).channel.use { input ->
            FileOutputStream(destination, false).channel.use { output ->
                transferAll(input, output)
            }
        }
        return destination.exists() && destination.length() == source.length()
    }

    fun moveFile(source: File, destination: File, overwrite: Boolean = true): Boolean {
        if (copyFile(source, destination, overwrite)) {
            return source.delete()
        }
        return false
    }

    fun deleteSafely(file: File): Boolean {
        return if (!file.exists()) true else file.delete()
    }

    fun detectMimeType(file: File, contentResolver: ContentResolver? = null, uri: Uri? = null): String {
        contentResolver?.let { cr ->
            uri?.let { u ->
                cr.getType(u)?.let { return it }
            }
        }
        val ext = file.extension.lowercase(Locale.US)
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: "application/octet-stream"
    }

    fun formatStorageSize(bytes: Long): String {
        if (bytes < 1024) return "$bytes B"
        val units = arrayOf("KB", "MB", "GB", "TB")
        var value = bytes.toDouble()
        var index = -1
        while (value >= 1024 && index < units.lastIndex) {
            value /= 1024.0
            index++
        }
        return "${DecimalFormat("#.##").format(value)} ${units[index]}"
    }

    private fun transferAll(input: FileChannel, output: FileChannel) {
        var position = 0L
        val size = input.size()
        while (position < size) {
            position += input.transferTo(position, size - position, output)
        }
    }
}
