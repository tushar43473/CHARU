package com.app.clipsteronline.upload.editor.export

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import java.io.File

/**
 * Muxes audio/video streams into container format.
 * Supports MP4/MOV output with audio sync.
 */
class VideoMuxer {

    private var mediaMuxer: MediaMuxer? = null
    private var videoTrackIndex = -1
    private var audioTrackIndex = -1

    private var isStarted = false

    /**
     * Create muxer.
     */
    fun create(outputPath: String, format: Int = MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4): Boolean {
        return try {
            mediaMuxer = MediaMuxer(outputPath, format)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Add video track.
     */
    fun addVideoTrack(format: MediaFormat): Int {
        if (mediaMuxer == null) return -1

        videoTrackIndex = mediaMuxer!!.addTrack(format)
        return videoTrackIndex
    }

    /**
     * Add audio track.
     */
    fun addAudioTrack(format: MediaFormat): Int {
        if (mediaMuxer == null) return -1

        audioTrackIndex = mediaMuxer!!.addTrack(format)
        return audioTrackIndex
    }

    /**
     * Start muxer.
     */
    fun start() {
        if (mediaMuxer != null && !isStarted) {
            mediaMuxer!!.start()
            isStarted = true
        }
    }

    /**
     * Write video sample.
     */
    fun writeVideoSample(
        buffer: ByteBuffer,
        presentationTimeUs: Long,
        flags: Int
    ) {
        if (isStarted && videoTrackIndex >= 0) {
            mediaMuxer?.writeSampleData(videoTrackIndex, buffer, bufferInfo(presentationTimeUs, flags, buffer.remaining()))
        }
    }

    /**
     * Write audio sample.
     */
    fun writeAudioSample(
        buffer: ByteBuffer,
        presentationTimeUs: Long,
        flags: Int
    ) {
        if (isStarted && audioTrackIndex >= 0) {
            mediaMuxer?.writeSampleData(audioTrackIndex, buffer, bufferInfo(presentationTimeUs, flags, buffer.remaining()))
        }
    }

    /**
     * Create buffer info.
     */
    private fun bufferInfo(presentationTimeUs: Long, flags: Int, size: Int): MediaCodec.BufferInfo {
        return MediaCodec.BufferInfo().apply {
            this.presentationTimeUs = presentationTimeUs
            this.flags = flags
            this.size = size
            this.offset = 0
        }
    }

    /**
     * Stop muxer.
     */
    fun stop() {
        try {
            mediaMuxer?.stop()
            isStarted = false
        } catch (e: Exception) {
            // Handle error
        }
    }

    /**
     * Release resources.
     */
    fun release() {
        stop()
        mediaMuxer?.release()
        mediaMuxer = null
    }

    /**
     * Set video rotation.
     */
    fun setVideoRotation(degrees: Int) {
        mediaMuxer?.setOrientationHint(degrees)
    }

    /**
     * Set metadata.
     */
    fun setMetadata(key: String, value: String) {
        mediaMuxer?.setMetadata(MediaMuxer.MuxerDescription(key, value))
    }

    companion object {
        // Format constants
        const val FORMAT_MP4 = MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4
        const val FORMAT_WEBM = MediaMuxer.OutputFormat.MUXER_OUTPUT_WEBM
        const val FORMAT_3GPP = MediaMuxer.OutputFormat.MUXER_OUTPUT_3GPP
    }
}

/**
 * Simple ByteBuffer wrapper.
 */
class ByteBufferWrapper(private val capacity: Int) {
    private val buffer = java.nio.ByteBuffer.allocate(capacity)

    fun put(data: ByteArray) {
        buffer.put(data)
    }

    fun position(pos: Int) {
        buffer.position(pos)
    }

    fun limit(lim: Int) {
        buffer.limit(lim)
    }

    fun remaining(): Int = buffer.remaining()

    fun getBuffer(): java.nio.ByteBuffer = buffer

    companion object {
        fun allocate(capacity: Int) = ByteBufferWrapper(capacity)
    }
}

/**
 * Simple ByteBuffer alias.
 */
typealias ByteBuffer = java.nio.ByteBuffer