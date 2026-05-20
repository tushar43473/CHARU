package upload.editor.app

import android.net.Uri

data class EditorState(
    val session: SessionState = SessionState(),
    val playback: PlaybackState = PlaybackState(),
    val timeline: TimelineState = TimelineState(),
    val selection: SelectionState = SelectionState(),
    val history: HistoryState = HistoryState(),
    val export: ExportState = ExportState.Idle,
) {
    data class SessionState(
        val sourceUri: String? = null,
        val isInitialized: Boolean = false,
        val lastError: String? = null,
    )

    data class PlaybackState(
        val isPlaying: Boolean = false,
        val durationMs: Long = 0L,
        val positionMs: Long = 0L,
        val bufferedPercent: Int = 0,
        val speed: Float = 1f,
    )

    data class TimelineState(
        val zoom: Float = 1f,
        val positionMs: Long = 0L,
        val snapEnabled: Boolean = true,
    )

    data class SelectionState(
        val selectedTrackId: String? = null,
        val selectedClipId: String? = null,
    )

    data class HistoryState(
        val canUndo: Boolean = false,
        val canRedo: Boolean = false,
        val stackSize: Int = 0,
    )

    sealed interface ExportState {
        data object Idle : ExportState
        data class InProgress(val progressPercent: Int) : ExportState
        data class Completed(val outputUri: Uri) : ExportState
        data class Failed(val reason: String) : ExportState
    }
}
