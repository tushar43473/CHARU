package com.app.clipsteronline.upload.editor.app

import com.app.clipsteronline.upload.editor.core.manager.SessionManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.ArrayDeque
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class EditorViewModel(
    private val sessionManager: SessionManager,
    private val maxUndoDepth: Int = 200,
) {
    private val stateLock = ReentrantLock()
    private val undoStack = ArrayDeque<EditorState>()
    private val redoStack = ArrayDeque<EditorState>()

    private val _state = MutableStateFlow(EditorState())
    val state: StateFlow<EditorState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 32)
    val events: SharedFlow<String> = _events.asSharedFlow()

    fun bindSession(projectId: String) {
        val session = sessionManager.startSession(projectId)
        reduce { it.copy(sessionId = session.id, version = it.version + 1) }
    }

    fun setPlayback(isPlaying: Boolean, positionMs: Long, durationMs: Long, speed: Float, looping: Boolean) = reduce {
        val safeDuration = durationMs.coerceAtLeast(0L)
        val safePosition = positionMs.coerceIn(0L, safeDuration)
        it.copy(
            playback = it.playback.copy(
                isPlaying = isPlaying,
                isLooping = looping,
                durationMs = safeDuration,
                positionMs = safePosition,
                speed = speed.coerceIn(0.25f, 4f),
            ),
            timeline = it.timeline.copy(playheadMs = safePosition),
            version = it.version + 1,
        )
    }

    fun updateTimeline(zoom: Float, scrollPx: Float, playheadMs: Long, activeTrackId: String?) = reduce {
        it.copy(
            timeline = it.timeline.copy(
                zoom = zoom.coerceIn(0.25f, 12f),
                scrollPx = scrollPx.coerceAtLeast(0f),
                playheadMs = playheadMs.coerceAtLeast(0L),
                activeTrackId = activeTrackId,
            ),
            playback = it.playback.copy(positionMs = playheadMs.coerceAtLeast(0L)),
            version = it.version + 1,
        )
    }

    fun setSelection(activeLayerId: String?, selectedClipIds: Set<String>) = reduce {
        it.copy(selection = it.selection.copy(activeLayerId = activeLayerId, selectedClipIds = selectedClipIds), version = it.version + 1)
    }

    fun setTool(tool: EditorState.ActiveTool) = reduce { it.copy(activeTool = tool, version = it.version + 1) }

    fun setExportState(exportState: EditorState.ExportState) = reduce { it.copy(export = exportState, version = it.version + 1) }

    fun undo() = stateLock.withLock {
        if (undoStack.isEmpty()) return
        val previous = undoStack.removeLast()
        redoStack.addLast(_state.value)
        _state.value = previous.withHistory(canUndo = undoStack.isNotEmpty(), canRedo = true, undoDepth = undoStack.size)
        sessionManager.updateSnapshot(_state.value)
    }

    fun redo() = stateLock.withLock {
        if (redoStack.isEmpty()) return
        val next = redoStack.removeLast()
        undoStack.addLast(_state.value)
        _state.value = next.withHistory(canUndo = true, canRedo = redoStack.isNotEmpty(), undoDepth = undoStack.size)
        sessionManager.updateSnapshot(_state.value)
    }

    private inline fun reduce(transform: (EditorState) -> EditorState) = stateLock.withLock {
        val current = _state.value
        val updated = transform(current)
        if (updated == current) return

        undoStack.addLast(current)
        while (undoStack.size > maxUndoDepth) undoStack.removeFirst()
        redoStack.clear()

        _state.update { updated.withHistory(canUndo = true, canRedo = false, undoDepth = undoStack.size) }
        sessionManager.updateSnapshot(_state.value)
    }

    private fun EditorState.withHistory(canUndo: Boolean, canRedo: Boolean, undoDepth: Int): EditorState =
        copy(history = history.copy(canUndo = canUndo, canRedo = canRedo, undoDepth = undoDepth))
}
