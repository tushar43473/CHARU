package com.app.clipsteronline.upload.editor.timeline.engine

import com.app.clipsteronline.upload.editor.core.model.TransitionModel

class TimelineRenderer(
    private val calculator: TimelineCalculator = TimelineCalculator(),
) {
    fun buildFrame(
        state: TimelineState,
        clips: List<TimelineEngine.ClipLayout>,
        transitions: List<TransitionWindow> = emptyList(),
        beatMarkersMs: List<Long> = emptyList(),
        waveform: WaveformOverlay? = null,
    ): RenderFrame {
        val visibleRange = calculator.visibleRange(state.scrollPx, state.viewportWidthPx, state.zoom)
        val visibleClips = clips.filter { calculator.isOverlapping(it.startMs, it.endMs, visibleRange.first, visibleRange.last) }
            .map {
                val leftPx = calculator.timeToPx(it.startMs, state.zoom) - state.scrollPx
                val rightPx = calculator.timeToPx(it.endMs, state.zoom) - state.scrollPx
                ClipRenderItem(it.clipId, it.trackId, leftPx, rightPx, it.layer)
            }
            .sortedWith(compareBy<ClipRenderItem> { it.layer }.thenBy { it.leftPx })

        val transitionItems = transitions
            .filter { calculator.isOverlapping(it.startMs, it.endMs, visibleRange.first, visibleRange.last) }
            .map {
                TransitionRenderItem(
                    id = it.id,
                    fromClipId = it.fromClipId,
                    toClipId = it.toClipId,
                    type = it.transition.type,
                    leftPx = calculator.timeToPx(it.startMs, state.zoom) - state.scrollPx,
                    rightPx = calculator.timeToPx(it.endMs, state.zoom) - state.scrollPx,
                    progress = it.transition.progressAt((state.playheadMs - it.startMs).coerceAtLeast(0L)),
                )
            }

        val beatItems = beatMarkersMs.filter { it in visibleRange }
            .map { BeatMarkerRenderItem(it, calculator.timeToPx(it, state.zoom) - state.scrollPx) }

        val waveformItems = waveform?.let { overlay ->
            overlay.samples.mapIndexed { index, amp ->
                val timeMs = visibleRange.first + ((visibleRange.last - visibleRange.first) * (index / overlay.samples.size.toFloat())).toLong()
                WaveformSampleRenderItem(timeMs, amp.coerceIn(0f, 1f), calculator.timeToPx(timeMs, state.zoom) - state.scrollPx)
            }
        } ?: emptyList()

        val playheadPx = calculator.timeToPx(state.playheadMs, state.zoom) - state.scrollPx
        return RenderFrame(playheadPx, visibleRange, visibleClips, transitionItems, beatItems, waveformItems)
    }

    data class WaveformOverlay(val samples: List<Float>)
    data class TransitionWindow(val id: String, val fromClipId: String, val toClipId: String, val startMs: Long, val endMs: Long, val transition: TransitionModel)
    data class RenderFrame(
        val playheadPx: Float,
        val visibleRangeMs: LongRange,
        val clips: List<ClipRenderItem>,
        val transitions: List<TransitionRenderItem>,
        val beats: List<BeatMarkerRenderItem>,
        val waveform: List<WaveformSampleRenderItem>,
    )

    data class ClipRenderItem(val clipId: String, val trackId: String, val leftPx: Float, val rightPx: Float, val layer: Int) { val widthPx: Float get() = (rightPx - leftPx).coerceAtLeast(0f) }
    data class TransitionRenderItem(val id: String, val fromClipId: String, val toClipId: String, val type: TransitionModel.Type, val leftPx: Float, val rightPx: Float, val progress: Float) { val widthPx: Float get() = (rightPx - leftPx).coerceAtLeast(0f) }
    data class BeatMarkerRenderItem(val timeMs: Long, val xPx: Float)
    data class WaveformSampleRenderItem(val timeMs: Long, val amplitude: Float, val xPx: Float)
}
