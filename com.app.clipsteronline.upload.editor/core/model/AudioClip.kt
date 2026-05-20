package com.app.clipsteronline.upload.editor.core.model

data class AudioClip(
    override val clipId: String,
    override val startMs: Long,
    override val endMs: Long,
    override val layer: Int,
    val sourceUri: String,
    val trimStartMs: Long,
    val trimEndMs: Long,
    val fadeInMs: Long = 0L,
    val fadeOutMs: Long = 0L,
    val waveformSamples: List<Float> = emptyList(),
    val volume: Float = 1f,
    val speed: Float = 1f,
    val isLooping: Boolean = false,
    val automationPoints: List<VolumePoint> = emptyList(),
) : TimelineTrack.TimelineClip {
    init {
        require(clipId.isNotBlank())
        require(sourceUri.isNotBlank())
        require(startMs >= 0 && endMs >= startMs)
        require(trimStartMs >= 0 && trimEndMs >= trimStartMs)
        require(fadeInMs >= 0 && fadeOutMs >= 0)
        require(volume in 0f..4f)
        require(speed in 0.25f..4f)
        require(waveformSamples.none { it.isNaN() || it.isInfinite() })
        require(fadeInMs + fadeOutMs <= (trimEndMs - trimStartMs).coerceAtLeast(0L))
        require(automationPoints.zipWithNext().all { it.first.timeMs <= it.second.timeMs })
    }

    fun contains(timeMs: Long): Boolean = timeMs in startMs..endMs

    fun volumeAtTimelineMs(timeMs: Long): Float {
        if (!contains(timeMs)) return 0f
        val base = volume * fadeGain(timeMs)
        if (automationPoints.isEmpty()) return base
        val local = (timeMs - startMs).coerceAtLeast(0L)
        val point = automationPoints.lastOrNull { it.timeMs <= local } ?: automationPoints.first()
        return (base * point.volume).coerceIn(0f, 4f)
    }

    private fun fadeGain(timeMs: Long): Float {
        val local = (timeMs - startMs).coerceAtLeast(0L)
        val duration = (endMs - startMs).coerceAtLeast(1L)
        val fadeIn = if (fadeInMs > 0) (local.toFloat() / fadeInMs).coerceIn(0f, 1f) else 1f
        val tailMs = (duration - local).coerceAtLeast(0L)
        val fadeOut = if (fadeOutMs > 0) (tailMs.toFloat() / fadeOutMs).coerceIn(0f, 1f) else 1f
        return minOf(fadeIn, fadeOut)
    }

    data class VolumePoint(
        val timeMs: Long,
        val volume: Float,
    ) {
        init {
            require(timeMs >= 0)
            require(volume in 0f..4f)
        }
    }
}
