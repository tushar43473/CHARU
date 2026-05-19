package com.app.clipsteronline.upload.editor.audio

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.pow

/**
 * Detects beats and BPM from audio.
 * Analyzes waveform peaks and rhythm.
 */
class BeatDetector(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {
    private var detectedBpm = 0f
    private var beatTimes = listOf<Long>()
    private var audioWaveform: FloatArray = floatArrayOf()

    /**
     * Analyze audio and detect BPM.
     */
    suspend fun analyze(waveformData: FloatArray, sampleRate: Int = 44100): Float = withContext(Dispatchers.Default) {
        audioWaveform = waveformData

        // Detect peaks
        val peaks = findPeaks(waveformData)

        // Filter beats by minimum interval
        val minBeatIntervalMs = 200 // 300 BPM max
        val filteredBeats = filterByInterval(peaks, minBeatIntervalMs)

        // Calculate BPM
        detectedBpm = computeBPM(filteredBeats, sampleRate)
        beatTimes = filteredBeats

        detectedBpm
    }

    /**
     * Find waveform peaks.
     */
    private fun findPeaks(data: FloatArray): List<Int> {
        val peaks = mutableListOf<Int>()
        val threshold = 0.5f

        for (i in 1 until data.size - 1) {
            if (data[i] > threshold &&
                data[i] > data[i - 1] &&
                data[i] >= data[i + 1]
            ) {
                peaks.add(i)
            }
        }

        return peaks
    }

    /**
     * Filter beats by interval.
     */
    private fun filterByInterval(peaks: List<Int>, minIntervalMs: Int): List<Long> {
        if (peaks.isEmpty()) return listOf()

        val filtered = mutableListOf<Long>()
        var lastBeat = 0L

        for (peak in peaks) {
            val timeMs = (peak * 1000L) / 44100
            if (timeMs - lastBeat >= minIntervalMs) {
                filtered.add(timeMs)
                lastBeat = timeMs
            }
        }

        return filtered
    }

    /**
     * Calculate BPM.
     */
    private fun computeBPM(beats: List<Long>, sampleRate: Int): Float {
        if (beats.size < 2) return 0f

        val intervals = mutableListOf<Long>()
        for (i in 1 until beats.size) {
            intervals.add(beats[i] - beats[i - 1])
        }

        if (intervals.isEmpty()) return 0f

        // Use median interval
        intervals.sort()
        val medianInterval = intervals[intervals.size / 2]
        if (medianInterval <= 0) return 0f

        return 60000f / medianInterval
    }

    /**
     * Get beat positions.
     */
    fun getBeatPositions(): List<Long> = beatTimes

    /**
     * Get BPM.
     */
    fun getBPM(): Float = detectedBpm

    /**
     * Get closest beat.
     */
    fun getClosestBeat(timeMs: Long): Long? {
        if (beatTimes.isEmpty()) return null

        var closest = beatTimes[0]
        var minDiff = abs(timeMs - closest)

        for (beat in beatTimes) {
            val diff = abs(timeMs - beat)
            if (diff < minDiff) {
                minDiff = diff
                closest = beat
            }
        }

        return closest
    }

    /**
     * Get next beat.
     */
    fun getNextBeat(timeMs: Long): Long? {
        return beatTimes.firstOrNull { it > timeMs }
    }

    /**
     * Has beat at time.
     */
    fun hasBeatAt(timeMs: Long, toleranceMs: Long = 50): Boolean {
        return beatTimes.any { abs(it - timeMs) <= toleranceMs }
    }
}