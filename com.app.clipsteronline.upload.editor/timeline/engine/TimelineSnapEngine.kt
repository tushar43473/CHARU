package com.app.clipsteronline.upload.editor.timeline.engine

import kotlin.math.abs
import kotlin.math.max

class TimelineSnapEngine(
    private val frameDurationMs: Long = 33L,
    private val baseThresholdPx: Float = 14f,
) {

    fun buildTargets(
        clips: List<ClipBounds>,
        playheadMs: Long? = null,
        markersMs: List<Long> = emptyList(),
        beatsMs: List<Long> = emptyList(),
    ): List<SnapTarget> {
        val targets = ArrayList<SnapTarget>(clips.size * 2 + markersMs.size + beatsMs.size + 1)
        clips.forEach { clip ->
            targets.add(SnapTarget(clip.startMs, SnapType.CLIP_START, clip.trackId, clip.clipId))
            targets.add(SnapTarget(clip.endMs, SnapType.CLIP_END, clip.trackId, clip.clipId))
        }
        playheadMs?.let { targets.add(SnapTarget(it, SnapType.PLAYHEAD)) }
        markersMs.forEach { targets.add(SnapTarget(it, SnapType.MARKER)) }
        beatsMs.forEach { targets.add(SnapTarget(it, SnapType.BEAT)) }
        return targets.sortedBy { it.timeMs }
    }

    fun resolveSnap(
        proposedMs: Long,
        targets: List<SnapTarget>,
        zoom: Float,
        calculator: TimelineCalculator,
        excludedClipId: String? = null,
    ): SnapResult {
        if (targets.isEmpty()) return SnapResult.none(proposedMs)

        val thresholdMs = max(frameDurationMs, calculator.pxDeltaToTimeDeltaMs(adaptiveThresholdPx(zoom), zoom))
        var best: SnapTarget? = null
        var bestDistance = Long.MAX_VALUE

        for (target in targets) {
            if (excludedClipId != null && target.clipId == excludedClipId) continue
            val distance = abs(target.timeMs - proposedMs)
            if (distance < bestDistance) {
                best = target
                bestDistance = distance
            }
        }

        if (best == null || bestDistance > thresholdMs) {
            return SnapResult.none(proposedMs)
        }

        return SnapResult(
            snappedTimeMs = best.timeMs,
            didSnap = true,
            target = best,
            distanceMs = bestDistance,
            thresholdMs = thresholdMs,
        )
    }

    fun preventNegativeGap(startMs: Long, minMs: Long = 0L): Long = startMs.coerceAtLeast(minMs)

    fun quantizeToFrame(timeMs: Long): Long {
        val frames = (timeMs.toDouble() / frameDurationMs.toDouble()).toLong()
        val snapped = frames * frameDurationMs
        val next = snapped + frameDurationMs
        return if (abs(next - timeMs) < abs(snapped - timeMs)) next else snapped
    }

    private fun adaptiveThresholdPx(zoom: Float): Float {
        val z = zoom.coerceIn(0.1f, 20f)
        return (baseThresholdPx / z).coerceIn(4f, baseThresholdPx)
    }
}

data class ClipBounds(
    val clipId: String,
    val trackId: String,
    val startMs: Long,
    val endMs: Long,
)

enum class SnapType { CLIP_START, CLIP_END, PLAYHEAD, MARKER, BEAT }

data class SnapTarget(
    val timeMs: Long,
    val type: SnapType,
    val trackId: String? = null,
    val clipId: String? = null,
)

data class SnapResult(
    val snappedTimeMs: Long,
    val didSnap: Boolean,
    val target: SnapTarget?,
    val distanceMs: Long,
    val thresholdMs: Long,
) {
    companion object {
        fun none(timeMs: Long) = SnapResult(timeMs, didSnap = false, target = null, distanceMs = Long.MAX_VALUE, thresholdMs = 0L)
    }
}
