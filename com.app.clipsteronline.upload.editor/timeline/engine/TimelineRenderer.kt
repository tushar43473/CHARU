package com.app.clipsteronline.upload.editor.timeline.engine

class TimelineRenderer(
    private val calculator: TimelineCalculator = TimelineCalculator(),
) {
    fun buildFrame(state: TimelineState, clips: List<TimelineEngine.ClipLayout>): RenderFrame {
        val visibleRange = calculator.visibleRange(state.scrollPx, state.viewportWidthPx, state.zoom)
        val visibleClips = clips.filter { calculator.isOverlapping(it.startMs, it.endMs, visibleRange.first, visibleRange.last) }
            .map {
                val leftPx = calculator.timeToPx(it.startMs, state.zoom) - state.scrollPx
                val rightPx = calculator.timeToPx(it.endMs, state.zoom) - state.scrollPx
                ClipRenderItem(it.clipId, it.trackId, leftPx, rightPx, it.layer)
            }
            .sortedWith(compareBy<ClipRenderItem> { it.layer }.thenBy { it.leftPx })

        val playheadPx = calculator.timeToPx(state.playheadMs, state.zoom) - state.scrollPx
        return RenderFrame(playheadPx = playheadPx, visibleRangeMs = visibleRange, clips = visibleClips)
    }

    data class RenderFrame(
        val playheadPx: Float,
        val visibleRangeMs: LongRange,
        val clips: List<ClipRenderItem>,
    )

    data class ClipRenderItem(
        val clipId: String,
        val trackId: String,
        val leftPx: Float,
        val rightPx: Float,
        val layer: Int,
    ) {
        val widthPx: Float get() = (rightPx - leftPx).coerceAtLeast(0f)
    }
}
