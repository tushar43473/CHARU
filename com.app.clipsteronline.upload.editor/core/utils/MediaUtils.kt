package com.app.clipsteronline.upload.editor.core.utils

import android.media.MediaMetadataRetriever

object MediaUtils {
    data class VideoMeta(
        val durationMs: Long,
        val width: Int,
        val height: Int,
        val bitrate: Int,
        val fps: Float,
        val videoCodec: String?,
        val audioCodec: String?,
    )

    fun extractVideoMeta(pathOrUri: String): VideoMeta {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(pathOrUri, HashMap())
            val durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 0
            val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 0
            val bitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toIntOrNull() ?: 0
            val fps = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE)?.toFloatOrNull() ?: 0f
            val vCodec = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_CODEC_MIME_TYPE)
            val aCodec = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)
            VideoMeta(durationMs, width, height, bitrate, fps, vCodec, aCodec)
        } finally {
            retriever.release()
        }
    }

    fun extractDurationMs(pathOrUri: String): Long = extractVideoMeta(pathOrUri).durationMs
}
