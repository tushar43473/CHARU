package com.app.clipsteronline.upload.editor.app

import android.net.Uri

/**
 * Immutable UI state for the video editor.
 * Contains all state needed for rendering the editor UI and managing editor operations.
 */
data class EditorState(
    val playbackInfo: PlaybackInfo = PlaybackInfo(),
    val timelinePosition: TimelinePosition = TimelinePosition(),
    val selectedTrack: Track? = null,
    val selectedClip: Clip? = null,
    val exportState: ExportState = ExportState.Idle,
    val timelineZoom: Float = 1.0f,
    val isPlaying: Boolean = false,
    val isSaving: Boolean = false,
    val error: EditorError? = null
)

/**
 * Information about the current playback state.
 */
data class PlaybackInfo(
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val isLooping: Boolean = false
)

/**
 * Position and navigation within the timeline.
 */
data class TimelinePosition(
    val horizontalScroll: Float = 0f,
    val verticalScroll: Float = 0f,
    val currentTimeMs: Long = 0L
)

/**
 * Represents a track in the timeline.
 */
data class Track(
    val id: String,
    val type: TrackType,
    val clips: List<Clip> = emptyList(),
    val isMuted: Boolean = false,
    val isLocked: Boolean = false,
    val volume: Float = 1.0f
)

/**
 * Type of media track.
 */
enum class TrackType {
    VIDEO,
    AUDIO,
    IMAGE
}

/**
 * Represents a clip within a track.
 */
data class Clip(
    val id: String,
    val uri: Uri,
    val startTimeMs: Long,
    val endTimeMs: Long,
    val trimStartMs: Long = 0L,
    val trimEndMs: Long = 0L,
    val volume: Float = 1.0f,
    val speed: Float = 1.0f,
    val effects: List<ClipEffect> = emptyList()
)

/**
 * Effect applied to a clip.
 */
sealed class ClipEffect {
    data class Filter(val name: String, val intensity: Float = 1.0f) : ClipEffect()
    data class Transition(val type: TransitionType, val durationMs: Long) : ClipEffect()
}

/**
 * Type of transition between clips.
 */
enum class TransitionType {
    NONE,
    CROSSFADE,
    FADE_TO_BLACK,
    DISSOLVE
}

/**
 * Export state for the editor.
 */
sealed class ExportState {
    data object Idle : ExportState()
    data class Preparing(val message: String) : ExportState()
    data class InProgress(val progress: Float, val currentStep: String) : ExportState()
    data class Success(val outputUri: Uri) : ExportState()
    data class Error(val message: String, val recoverable: Boolean = false) : ExportState()
}

/**
 * Editor error representation.
 */
sealed class EditorError {
    data class LoadFailed(val message: String, val clipId: String?) : EditorError()
    data class ExportFailed(val message: String) : EditorError()
    data class RenderFailed(val message: String) : EditorError()
    data class StorageError(val message: String) : EditorError()
}

/**
 * Represents the result of an undo/redo operation.
 */
sealed class UndoRedoResult {
    data object Success : UndoRedoResult()
    data object NothingToUndo : UndoRedoResult()
    data object NothingToRedo : UndoRedoResult()
    data class Error(val message: String) : UndoRedoResult()
}