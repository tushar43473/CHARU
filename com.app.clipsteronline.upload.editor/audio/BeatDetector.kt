package com.app.clipsteronline.upload.editor.audio

import kotlin.math.max

class BeatDetector {
    fun configure() = Unit

    fun detect(samples: FloatArray, sampleRate: Int, windowSize: Int = 1024): BeatAnalysis {
        if (samples.isEmpty() || sampleRate <= 0) return BeatAnalysis(0f, emptyList(), 0f)

        val energies = mutableListOf<Float>()
        var i = 0
        while (i < samples.size) {
            val end = minOf(i + windowSize, samples.size)
            val chunk = samples.sliceArray(i until end)
            val energy = chunk.sumOf { (it * it).toDouble() }.toFloat() / max(1, chunk.size)
            energies += energy
            i = end
        }

        if (energies.isEmpty()) return BeatAnalysis(0f, emptyList(), 0f)
        val average = energies.average().toFloat().coerceAtLeast(1e-6f)
        val threshold = average * 1.35f
        val beats = mutableListOf<Long>()
        energies.forEachIndexed { index, value ->
            if (value > threshold) {
                val ms = ((index * windowSize).toDouble() / sampleRate * 1000.0).toLong()
                if (beats.isEmpty() || ms - beats.last() > 120L) beats += ms
            }
        }

        val bpm = if (beats.size > 1) {
            val intervals = beats.zipWithNext { a, b -> (b - a).coerceAtLeast(1L) }
            60_000f / intervals.average().toFloat()
        } else 0f
        return BeatAnalysis(bpm = bpm.coerceIn(0f, 280f), beatMarkersMs = beats, confidence = (beats.size / energies.size.toFloat()).coerceIn(0f, 1f))
    }

    data class BeatAnalysis(
        val bpm: Float,
        val beatMarkersMs: List<Long>,
        val confidence: Float,
    )
}
