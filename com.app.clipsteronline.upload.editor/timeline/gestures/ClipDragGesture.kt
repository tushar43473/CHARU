package com.app.clipsteronline.upload.editor.timeline.gestures

import com.app.clipsteronline.upload.editor.gestures.SnapGuideEngine
import com.app.clipsteronline.upload.editor.timeline.engine.SnapResult
import com.app.clipsteronline.upload.editor.timeline.engine.TimelineCalculator
import com.app.clipsteronline.upload.editor.timeline.engine.TimelineSnapEngine
import kotlin.math.abs

class ClipDragGesture(
    private val calculator: TimelineCalculator,
    private val snapEngine: TimelineSnapEngine,
    private val guideEngine: SnapGuideEngine,
) {
    fun drag(
        clipId: String,
        currentStartMs: Long,
        deltaPx: Float,
        deltaTrackPx: Float,
        currentTrackIndex: Int,
        trackHeightPx: Float,
        zoom: Float,
        targets: List<com.app.clipsteronline.upload.editor.timeline.engine.SnapTarget>,
    ): DragResult {
        val rawTimeDelta = calculator.pxDeltaToTimeDeltaMs(abs(deltaPx), zoom)
        val signedDelta = if (deltaPx < 0f) -rawTimeDelta else rawTimeDelta
        val proposed = snapEngine.preventNegativeGap(currentStartMs + signedDelta)
        val snap = snapEngine.resolveSnap(proposed, targets, zoom, calculator, excludedClipId = clipId)
        val finalTime = if (snap.didSnap) snap.snappedTimeMs else proposed
        val trackShift = if (trackHeightPx <= 0f) 0 else (deltaTrackPx / trackHeightPx).toInt()
        val newTrack = (currentTrackIndex + trackShift).coerceAtLeast(0)

        val finalPx = calculator.timeToPx(finalTime, zoom)
        if (snap.didSnap) guideEngine.showSnap(snap, finalPx) else guideEngine.clear()

        return DragResult(finalTime, newTrack, snap)
    }

    fun end() {
        guideEngine.clear()
    }
}

data class DragResult(
    val startMs: Long,
    val trackIndex: Int,
    val snap: SnapResult,
)
