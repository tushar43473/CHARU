package com.app.clipsteronline.upload.editor.audio

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt

class AudioWaveformGenerator {
    fun configure() = Unit

    fun generate(samples: FloatArray, sampleRate: Int, windowSize: Int = 1024): WaveformData {
        if (samples.isEmpty() || sampleRate <= 0) {
            return WaveformData(emptyList(), emptyList(), 0f, sampleRate.coerceAtLeast(0), 0L)
        }

        val normalized = samples.map { it.coerceIn(-1f, 1f) }
        val peaks = mutableListOf<Float>()
        val rms = mutableListOf<Float>()
        var i = 0
        while (i < normalized.size) {
            val end = minOf(i + windowSize, normalized.size)
            val chunk = normalized.subList(i, end)
            val peak = chunk.maxOf { abs(it) }
            val energy = chunk.sumOf { (it * it).toDouble() }.toFloat()
            val root = sqrt(energy / max(1, chunk.size))
            peaks += peak
            rms += root
            i = end
        }

        val durationMs = ((samples.size.toDouble() / sampleRate) * 1000.0).toLong()
        return WaveformData(peaks = peaks, rms = rms, maxAmplitude = peaks.maxOrNull() ?: 0f, sampleRate = sampleRate, durationMs = durationMs)
    }

    fun downsample(source: List<Float>, targetPoints: Int): List<Float> {
        if (source.isEmpty() || targetPoints <= 0) return emptyList()
        if (source.size <= targetPoints) return source
        val bucket = source.size.toFloat() / targetPoints
        return List(targetPoints) { idx ->
            val start = (idx * bucket).toInt().coerceAtMost(source.lastIndex)
            val end = (((idx + 1) * bucket).toInt().coerceAtMost(source.size)).coerceAtLeast(start + 1)
            source.subList(start, end).maxOrNull() ?: 0f
        }
    }

    data class WaveformData(
        val peaks: List<Float>,
        val rms: List<Float>,
        val maxAmplitude: Float,
        val sampleRate: Int,
        val durationMs: Long,
    )
}
