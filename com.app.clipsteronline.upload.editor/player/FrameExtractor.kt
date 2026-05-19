package com.app.clipsteronline.upload.editor.player

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.withContext

/**
 * Frame extractor for video.
 * Uses MediaMetadataRetriever for frame extraction.
 */
class FrameExtractor(
    private val context: Context
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var retriever: MediaMetadataRetriever? = null

    /**
     * Open video.
     */
    fun open(uri: Uri): Boolean {
        return try {
            retriever = MediaMetadataRetriever().apply {
                setDataSource(context, uri)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Open from path.
     */
    fun open(path: String): Boolean {
        return open(Uri.parse(path))
    }

    /**
     * Extract frame at time in microseconds.
     */
    fun extractFrame(timeUs: Long): Bitmap? {
        return try {
            retriever?.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Extract frame at time in milliseconds.
     */
    fun extractFrameMs(timeMs: Long): Bitmap? {
        return extractFrame(timeMs * 1000)
    }

    /**
     * Extract frame at specific frame number.
     */
    fun extractFrameAtFrame(frameNumber: Int, frameRate: Int = 30): Bitmap? {
        val timeMs = frameNumber * 1000L / frameRate
        return extractFrameMs(timeMs)
    }

    /**
     * Extract scaled frame.
     */
    fun extractScaledFrame(timeUs: Long, width: Int, height: Int): Bitmap? {
        return try {
            retriever?.getScaledFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST, width.toLong(), height.toLong())
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Extract scaled frame at ms.
     */
    fun extractScaledFrameMs(timeMs: Long, width: Int, height: Int): Bitmap? {
        return extractScaledFrame(timeMs * 1000, width, height)
    }

    /**
     * Get frame at nearest sync.
     */
    fun extractFrameAtSync(timeUs: Long): Bitmap? {
        return try {
            retriever?.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Get video duration.
     */
    fun getDuration(): Long {
        return (retriever?.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L)
    }

    /**
     * Get video width.
     */
    fun getVideoWidth(): Int {
        return retriever?.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 0
    }

    /**
     * Get video height.
     */
    fun getVideoHeight(): Int {
        return retriever?.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 0
    }

    /**
     * Get video rotation.
     */
    fun getVideoRotation(): Int {
        return retriever?.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)?.toIntOrNull() ?: 0
    }

    /**
     * Get frame rate.
     */
    fun getFrameRate(): Float {
        return retriever?.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE)?.toFloatOrNull() ?: 30f
    }

    /**
     * Get bitrate.
     */
    fun getBitrate(): Int {
        return retriever?.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toIntOrNull() ?: 0
    }

    /**
     * Get rotation corrected dimensions.
     */
    fun getAdjustedDimensions(): Pair<Int, Int> {
        val width = getVideoWidth()
        val height = getVideoHeight()
        val rotation = getVideoRotation()

        return when (rotation) {
            90, 270 -> height to width
            else -> width to height
        }
    }

    /**
     * Extract thumbnails at intervals.
     */
    suspend fun extractThumbnails(
        count: Int,
        width: Int = 200,
        height: Int = 200
    ): List<Bitmap> = withContext(Dispatchers.IO) {
        val duration = getDuration()
        if (duration <= 0 || count <= 0) return@withContext emptyList()

        val interval = duration / count
        val thumbnails = mutableListOf<Bitmap>()

        for (i in 0 until count) {
            val timeMs = i * interval
            extractScaledFrameMs(timeMs, width, height)?.let { thumbnails.add(it) }
        }

        thumbnails
    }

    /**
     * Release extractor.
     */
    fun release() {
        retriever?.release()
        retriever = null
    }
}