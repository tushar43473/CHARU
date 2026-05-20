package com.app.clipsteronline.upload.editor.app

import java.util.UUID

data class EditorState(
    val sessionId: String = UUID.randomUUID().toString(),
    val playback: PlaybackState = PlaybackState(),
    val timeline: TimelineState = TimelineState(),
    val selection: SelectionState = SelectionState(),
    val activeTool: ActiveTool = ActiveTool.SELECT,
    val history: HistoryState = HistoryState(),
    val export: ExportState = ExportState.Idle,
    val version: Long = 0L,
) {
    data class PlaybackState(
        val isPlaying: Boolean = false,
        val isLooping: Boolean = false,
        val positionMs: Long = 0L,
        val durationMs: Long = 0L,
        val speed: Float = 1f,
    )

    data class TimelineState(
        val zoom: Float = 1f,
        val scrollPx: Float = 0f,
        val playheadMs: Long = 0L,
        val activeTrackId: String? = null,
    )

    data class SelectionState(
        val activeLayerId: String? = null,
        val selectedClipIds: Set<String> = emptySet(),
    )

    enum class ActiveTool { SELECT, TRIM, SPLIT, SPEED, FILTER, EFFECT, TEXT, AUDIO }

    data class HistoryState(val canUndo: Boolean = false, val canRedo: Boolean = false, val undoDepth: Int = 0)

    sealed interface ExportState {
        data object Idle : ExportState
        data class InProgress(val progressPercent: Int) : ExportState
        data class Failed(val message: String) : ExportState
        data class Completed(val outputPath: String) : ExportState
    }
}
