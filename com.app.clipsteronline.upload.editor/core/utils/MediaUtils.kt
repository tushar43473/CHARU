package com.app.clipsteronline.upload.editor.core.utils

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri

/**
 * Media utilities for extracting video and audio metadata.
 * Provides duration, resolution, frame rate, bitrate, and audio properties.
 */
object MediaUtils {

    /**
     * Get video duration in milliseconds.
     */
    fun getVideoDuration(context: Context, uri: Uri): Long? {
        return getMetadata(context, uri, MediaMetadataRetriever.METADATA_KEY_DURATION)
    }

    /**
     * Get video width.
     */
    fun getVideoWidth(context: Context, uri: Uri): Int? {
        return getMetadata(context, uri, MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
    }

    /**
     * Get video height.
     */
    fun getVideoHeight(context: Context, uri: Uri): Int? {
        return getMetadata(context, uri, MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
    }

    /**
     * Get video rotation.
     */
    fun getVideoRotation(context: Context, uri: Uri): Int? {
        return getMetadata(context, uri, MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
    }

    /**
     * Get video frame rate.
     */
    fun getVideoFrameRate(context: Context, uri: Uri): Int? {
        return getMetadata(context, uri, MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE)?.toIntOrNull()
    }

    /**
     * Get video bitrate.
     */
    fun getVideoBitrate(context: Context, uri: Uri): Int? {
        return getMetadata(context, uri, MediaMetadataRetriever.METADATA_KEY_BITRATE)
    }

    /**
     * Get video mime type.
     */
    fun getVideoMimeType(context: Context, uri: Uri): String? {
        return getMetadata(context, uri, MediaMetadataRetriever.METADATA_KEY_MIMETYPE)
    }

    /**
     * Get video codec.
     */
    fun getVideoCodec(context: Context, uri: Uri): String? {
        return getMetadata(context, uri, MediaMetadataRetriever.METADATA_KEY_VIDEO_DECODER)
    }

    /**
     * Get audio duration.
     */
    fun getAudioDuration(context: Context, uri: Uri): Long? {
        return getVideoDuration(context, uri)
    }

    /**
     * Get audio bitrate.
     */
    fun getAudioBitrate(context: Context, uri: Uri): Int? {
        return getVideoBitrate(context, uri)
    }

    /**
     * Get audio sample rate.
     */
    fun getAudioSampleRate(context: Context, uri: Uri): Int? {
        return getMetadata(context, uri, MediaMetadataRetriever.METADATA_KEY_SAMPLERATE)
    }

    /**
     * Get audio channel count.
     */
    fun getAudioChannels(context: Context, uri: Uri): Int? {
        return getMetadata(context, uri, MediaMetadataRetriever.METADATA_KEY_CHANNEL_COUNT)
    }

    /**
     * Get audio mime type.
     */
    fun getAudioMimeType(context: Context, uri: Uri): String? {
        return getVideoMimeType(context, uri)
    }

    /**
     * Get audio codec.
     */
    fun getAudioCodec(context: Context, uri: Uri): String? {
        return getMetadata(context, uri, MediaMetadataRetriever.METADATA_KEY_AUDIO_DECODER)
    }

    /**
     * Get media date.
     */
    fun getMediaDate(context: Context, uri: Uri): Long? {
        return getMetadata(context, uri, MediaMetadataRetriever.METADATA_KEY_DATE)?.toLongOrNull()
    }

    /**
     * Get media title.
     */
    fun getMediaTitle(context: Context, uri: Uri): String? {
        return getMetadata(context, uri, MediaMetadataRetriever.METADATA_KEY_TITLE)
    }

    /**
     * Get media artist.
     */
    fun getMediaArtist(context: Context, uri: Uri): String? {
        return getMetadata(context, uri, MediaMetadataRetriever.METADATA_KEY_ARTIST)
    }

    /**
     * Get media album.
     */
    fun getMediaAlbum(context: Context, uri: Uri): String? {
        return getMetadata(context, uri, MediaMetadataRetriever.METADATA_KEY_ALBUM)
    }

    /**
     * Get media album artist.
     */
    fun getMediaAlbumArtist(context: Context, uri: Uri): String? {
        return getMetadata(context, uri, MediaMetadataRetriever.METADATA_KEY_ALBUMART)
    }

    /**
     * Get media composer.
     */
    fun getMediaComposer(context: Context, uri: Uri): String? {
        return getMetadata(context, uri, MediaMetadataRetriever.METADATA_KEY_COMPOSER)
    }

    /**
     * Get media genre.
     */
    fun getMediaGenre(context: Context, uri: Uri): String? {
        return getMetadata(context, uri, MediaMetadataRetriever.METADATA_KEY_GENRE)
    }

    /**
     * Get media year.
     */
    fun getMediaYear(context: Context, uri: Uri): Int? {
        return getMetadata(context, uri, MediaMetadataRetriever.METADATA_KEY_YEAR)?.toIntOrNull()
    }

    /**
     * Get video resolution tuple.
     */
    fun getVideoResolution(context: Context, uri: Uri): Pair<Int, Int>? {
        val width = getVideoWidth(context, uri)
        val height = getVideoHeight(context, uri)

        return if (width != null && height != null) {
            width to height
        } else null
    }

    /**
     * Check if video is vertical (height > width).
     */
    fun isVideoVertical(context: Context, uri: Uri): Boolean {
        val resolution = getVideoResolution(context, uri) ?: return false
        return resolution.second > resolution.first
    }

    /**
     * Check if video is horizontal (width > height).
     */
    fun isVideoHorizontal(context: Context, uri: Uri): Boolean {
        val resolution = getVideoResolution(context, uri) ?: return false
        return resolution.first > resolution.second
    }

    /**
     * Check if video is square.
     */
    fun isVideoSquare(context: Context, uri: Uri): Boolean {
        val resolution = getVideoResolution(context, uri) ?: return false
        return resolution.first == resolution.second
    }

    /**
     * Get video rotation adjusted resolution.
     */
    fun getAdjustedResolution(context: Context, uri: Uri): Pair<Int, Int>? {
        val width = getVideoWidth(context, uri)
        val height = getVideoHeight(context, uri)
        val rotation = getVideoRotation(context, uri)

        return if (width != null && height != null) {
            when (rotation) {
                90, 270 -> height to width
                else -> width to height
            }
        } else null
    }

    /**
     * Extract frame at specific time.
     */
    fun extractFrame(context: Context, uri: Uri, timeUs: Long): android.graphics.Bitmap? {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, uri)
            val frame = retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST)
            retriever.release()
            frame
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Extract frame at milliseconds.
     */
    fun extractFrameMs(context: Context, uri: Uri, timeMs: Long): android.graphics.Bitmap? {
        return extractFrame(context, uri, timeMs * 1000)
    }

    /**
     * Get thumbnail at time.
     */
    fun getThumbnail(context: Context, uri: Uri, timeMs: Long = 0, width: Int = 200, height: Int = 200): android.graphics.Bitmap? {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, uri)
            val frame = retriever.getScaledFrameAtTime(timeMs * 1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC, width.toLong(), height.toLong())
            retriever.release()
            frame
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Extract embedded picture.
     */
    fun getEmbeddedPicture(context: Context, uri: Uri): ByteArray? {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, uri)
            val picture = retriever.embeddedPicture
            retriever.release()
            picture
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Get video frame count estimate.
     */
    fun getEstimatedFrameCount(context: Context, uri: Uri): Long? {
        val duration = getVideoDuration(context, uri) ?: return null
        val frameRate = getVideoFrameRate(context, uri) ?: return 30

        return (duration / 1000.0 * frameRate).toLong()
    }

    /**
     * Get video bytes per second estimate.
     */
    fun getBytesPerSecond(context: Context, uri: Uri): Double? {
        val duration = getVideoDuration(context, uri) ?: return null
        val bitrate = getVideoBitrate(context, uri) ?: return null

        return bitrate.toDouble() / 1000.0 / (duration / 1000.0)
    }

    /**
     * Get metadata by key.
     */
    private fun getMetadata(context: Context, uri: Uri, key: Int): String? {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, uri)
            val value = retriever.extractMetadata(key)
            retriever.release()
            value
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Format bitrate for display.
     */
    fun formatBitrate(bitrate: Int): String {
        return when {
            bitrate >= 1_000_000 -> String.format("%.1f Mbps", bitrate / 1_000_000.0)
            bitrate >= 1_000 -> String.format("%.0f Kbps", bitrate / 1_000.0)
            else -> "$bitrate bps"
        }
    }

    /**
     * Format resolution for display.
     */
    fun formatResolution(width: Int, height: Int): String {
        return "${width}x${height}"
    }
}