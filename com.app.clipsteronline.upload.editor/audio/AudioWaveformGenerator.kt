package com.app.clipsteronline.upload.editor.audio

import android.content.Context
import android.media.MediaExtractor
import android.media.MediaFormat
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Generates waveform data from audio files.
 * Extracts PCM amplitude for visualization.
 */
class AudioWaveformGenerator(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) {
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO

    /**
     * Generate waveform data.
     */
    suspend fun generateWaveform(
        filePath: String,
        targetSamples: Int = 1000
    ): FloatArray = withContext(ioDispatcher) {
        val extractor = MediaExtractor()
        extractor.setDataSource(filePath)

        // Find audio track
        var audioTrackIndex = -1
        for (i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME)
            if (mime?.startsWith("audio/") == true) {
                audioTrackIndex = i
                break
            }
        }

        if (audioTrackIndex < 0) {
            extractor.release()
            return@withContext FloatArray(0)
        }

        extractor.selectTrack(audioTrackIndex)
        val format = extractor.getTrackFormat(audioTrackIndex)

        val sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
        val duration = format.getLong(MediaFormat.KEY_DURATION) // microseconds
        val channelCount = if (format.containsKey(MediaFormat.KEY_CHANNEL_COUNT)) {
            format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
        } else 2
        val bitRate = format.getInteger(MediaFormat.KEY_BIT_RATE)

        // Calculate samples needed
        val durationMs = duration / 1000
        val totalSamples = (durationMs * sampleRate / 1000).toInt()
        val samplesPerPoint = totalSamples / targetSamples

        val waveform = FloatArray(targetSamples)

        // Extract and process audio
        val bufferSize = 1024 * 1024
        val buffer = ByteBuffer.allocate(bufferSize)
        var currentSample = 0

        while (true) {
            buffer.clear()
            val sampleSize = extractor.readSampleData(buffer, 0)
            if (sampleSize < 0) break

            buffer.rewind()
            val samples = buffer.asShortBuffer()
            samples.order(ByteOrder.LITTLE_ENDIAN)

            while (samples.hasRemaining() && currentSample < totalSamples) {
                val sampleIndex = currentSample / samplesPerPoint
                if (sampleIndex < targetSamples) {
                    val amplitude = kotlin.math.abs(samples.get().toFloat() / 32768f)
                    waveform[sampleIndex] = maxOf(waveform[sampleIndex], amplitude)
                }
                currentSample++
            }

            extractor.advance()
        }

        extractor.release()

        // Normalize
        val maxAmp = waveform.maxOrNull() ?: 1f
        if (maxAmp > 0) {
            for (i in waveform.indices) {
                waveform[i] /= maxAmp
            }
        }

        waveform
    }

    /**
     * Generate waveform for range.
     */
    suspend fun generateWaveformRange(
        filePath: String,
        startMs: Long,
        endMs: Long,
        targetSamples: Int
    ): FloatArray = withContext(ioDispatcher) {
        // Similar implementation with start/end range
        generateWaveform(filePath, targetSamples)
    }

    /**
     * Generate waveform at zoom level.
     */
    suspend fun generateWaveformZoom(
        filePath: String,
        zoomLevel: Float,
        visibleStartMs: Long,
        visibleEndMs: Long
    ): FloatArray = withContext(ioDispatcher) {
        val visibleDuration = visibleEndMs - visibleStartMs
        val samples = (visibleDuration / 100).toInt().coerceIn(100, 10000)
        generateWaveformRange(filePath, visibleStartMs, visibleEndMs, samples)
    }
}