package upload.editor.core.utils

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri

object MediaUtils {
    fun extractDurationMs(source: String): Long = withRetriever(source) {
        extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
    }

    fun extractFrameAtMs(source: String, timeMs: Long): Bitmap? = withRetriever(source) {
        getFrameAtTime(timeMs * 1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
    }

    fun extractAudioMetadata(source: String): AudioMetadata = withRetriever(source) {
        AudioMetadata(
            sampleRate = extractMetadata(MediaMetadataRetriever.METADATA_KEY_SAMPLERATE)?.toIntOrNull() ?: 0,
            channels = extractMetadata(MediaMetadataRetriever.METADATA_KEY_NUM_TRACKS)?.toIntOrNull() ?: 0,
            bitrate = extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toIntOrNull() ?: 0,
        )
    }

    fun extractResolution(source: String): Resolution = withRetriever(source) {
        val width = extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 0
        val height = extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 0
        Resolution(width, height)
    }

    fun extractBitrate(source: String): Int = withRetriever(source) {
        extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toIntOrNull() ?: 0
    }

    fun extractFps(source: String): Float = withRetriever(source) {
        extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE)?.toFloatOrNull() ?: 0f
    }

    fun extractDurationMs(source: Uri): Long = withRetriever(source) {
        extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
    }

    private inline fun <T> withRetriever(source: String, block: MediaMetadataRetriever.() -> T): T {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(source)
        return try { retriever.block() } finally { retriever.release() }
    }

    private inline fun <T> withRetriever(source: Uri, block: MediaMetadataRetriever.() -> T): T {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(source.toString(), HashMap())
        return try { retriever.block() } finally { retriever.release() }
    }

    data class AudioMetadata(val sampleRate: Int, val channels: Int, val bitrate: Int)
    data class Resolution(val width: Int, val height: Int)
}
