package com.app.clipsteronline.upload.editor.timeline.sync

import com.app.clipsteronline.upload.editor.timeline.engine.TimelineScrollEngine
import com.app.clipsteronline.upload.editor.timeline.engine.TimelineZoomEngine

class PlayheadSyncController(
    private val scrollEngine: TimelineScrollEngine,
    private val zoomEngine: TimelineZoomEngine,
) {
    fun syncPlayheadToTimeline(positionUs: Long, viewportWidthPx: Double, autoCenter: Boolean): Double {
        val playheadPx = zoomEngine.timeUsToPixel(positionUs)
        if (autoCenter) {
            scrollEngine.centerOn(positionUs, zoomEngine, viewportWidthPx)
        }
        return playheadPx
    }

    fun seekFromTimelinePixel(timelinePixel: Double): Long {
        return zoomEngine.quantizeToFrame(zoomEngine.pixelToTimeUs(timelinePixel))
    }
}
