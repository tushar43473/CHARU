package com.app.clipsteronline.upload.editor.performance

class PlaybackPerformanceMonitor {
    private var totalFrames = 0
    private var totalFrameTimeMs = 0f

    fun configure() = Unit

    fun record(frameTimeMs: Float): Snapshot {
        totalFrames += 1
        totalFrameTimeMs += frameTimeMs
        val avg = if (totalFrames == 0) 0f else totalFrameTimeMs / totalFrames
        val fps = if (avg <= 0f) 0f else 1000f / avg
        return Snapshot(totalFrames, avg, fps)
    }

    data class Snapshot(val frames: Int, val averageFrameTimeMs: Float, val estimatedFps: Float)
}
