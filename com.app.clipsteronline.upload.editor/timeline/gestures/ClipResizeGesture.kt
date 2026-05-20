package com.app.clipsteronline.upload.editor.timeline.gestures

import com.app.clipsteronline.upload.editor.gestures.SnapGuideEngine
import com.app.clipsteronline.upload.editor.timeline.engine.SnapResult
import com.app.clipsteronline.upload.editor.timeline.engine.TimelineCalculator
import com.app.clipsteronline.upload.editor.timeline.engine.TimelineSnapEngine
import kotlin.math.abs
import kotlin.math.max

class ClipResizeGesture(
    private val calculator: TimelineCalculator,
    private val snapEngine: TimelineSnapEngine,
    private val guideEngine: SnapGuideEngine,
) {
    fun resizeStart(
        clipId: String,
        currentStartMs: Long,
        currentEndMs: Long,
        deltaPx: Float,
        zoom: Float,
        minDurationMs: Long,
        targets: List<com.app.clipsteronline.upload.editor.timeline.engine.SnapTarget>,
    ): ResizeResult {
        val deltaMs = calculator.pxDeltaToTimeDeltaMs(abs(deltaPx), zoom) * if (deltaPx < 0f) -1 else 1
        val unsnapped = (currentStartMs + deltaMs).coerceAtMost(currentEndMs - minDurationMs)
        val snapped = snapEngine.resolveSnap(unsnapped, targets, zoom, calculator, excludedClipId = clipId)
        val finalStart = max(0L, if (snapped.didSnap) snapped.snappedTimeMs.coerceAtMost(currentEndMs - minDurationMs) else unsnapped)
        emitGuide(finalStart, zoom, snapped)
        return ResizeResult(startMs = finalStart, endMs = currentEndMs, snap = snapped)
    }

    fun resizeEnd(
        clipId: String,
        currentStartMs: Long,
        currentEndMs: Long,
        deltaPx: Float,
        zoom: Float,
        minDurationMs: Long,
        targets: List<com.app.clipsteronline.upload.editor.timeline.engine.SnapTarget>,
    ): ResizeResult {
        val deltaMs = calculator.pxDeltaToTimeDeltaMs(abs(deltaPx), zoom) * if (deltaPx < 0f) -1 else 1
        val unsnapped = (currentEndMs + deltaMs).coerceAtLeast(currentStartMs + minDurationMs)
        val snapped = snapEngine.resolveSnap(unsnapped, targets, zoom, calculator, excludedClipId = clipId)
        val finalEnd = if (snapped.didSnap) snapped.snappedTimeMs.coerceAtLeast(currentStartMs + minDurationMs) else unsnapped
        emitGuide(finalEnd, zoom, snapped)
        return ResizeResult(startMs = currentStartMs, endMs = finalEnd, snap = snapped)
    }

    fun end() = guideEngine.clear()

    private fun emitGuide(timeMs: Long, zoom: Float, snap: SnapResult) {
        if (snap.didSnap) guideEngine.showSnap(snap, calculator.timeToPx(timeMs, zoom)) else guideEngine.clear()
    }
}

data class ResizeResult(
    val startMs: Long,
    val endMs: Long,
    val snap: SnapResult,
)
