package com.app.clipsteronline.upload.editor.performance

class FrameDropController(
    private val targetFps: Int = 60,
) {
    private var droppedFrames = 0

    fun configure() = Unit

    fun onFrame(frameTimeMs: Float): FrameDecision {
        val budget = 1000f / targetFps
        val dropped = frameTimeMs > budget * 1.35f
        if (dropped) droppedFrames++
        val qualityScale = when {
            frameTimeMs > budget * 2f -> 0.6f
            frameTimeMs > budget * 1.35f -> 0.8f
            else -> 1f
        }
        return FrameDecision(dropped, qualityScale, droppedFrames)
    }

    data class FrameDecision(val dropped: Boolean, val qualityScale: Float, val droppedFrames: Int)
}
