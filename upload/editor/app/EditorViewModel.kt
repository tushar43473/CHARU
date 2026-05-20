package upload.editor.app

import android.net.Uri
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class EditorViewModel : ViewModel() {
    private val _state = MutableStateFlow(EditorState())
    val state: StateFlow<EditorState> = _state.asStateFlow()

    private val lock = ReentrantLock()
    private val undoStack = ArrayDeque<EditorState>()
    private val redoStack = ArrayDeque<EditorState>()
    private val maxHistory = 150

    fun initializeSession(sourceUri: String?) = reduce { current ->
        current.copy(session = current.session.copy(sourceUri = sourceUri, isInitialized = true, lastError = null))
    }

    fun setPlayback(isPlaying: Boolean, positionMs: Long, durationMs: Long, bufferedPercent: Int, speed: Float = 1f) =
        reduce { current ->
            val safeDuration = durationMs.coerceAtLeast(0L)
            val safePosition = positionMs.coerceIn(0L, safeDuration)
            current.copy(
                playback = current.playback.copy(
                    isPlaying = isPlaying,
                    positionMs = safePosition,
                    durationMs = safeDuration,
                    bufferedPercent = bufferedPercent.coerceIn(0, 100),
                    speed = speed.coerceIn(0.25f, 4f),
                ),
                timeline = current.timeline.copy(positionMs = safePosition),
            )
        }

    fun seekTo(positionMs: Long) = reduce { current ->
        val safePosition = positionMs.coerceIn(0L, current.playback.durationMs)
        current.copy(
            playback = current.playback.copy(positionMs = safePosition),
            timeline = current.timeline.copy(positionMs = safePosition),
        )
    }

    fun setTimelineZoom(zoom: Float) = reduce { it.copy(timeline = it.timeline.copy(zoom = zoom.coerceIn(0.25f, 12f))) }

    fun toggleSnap(enabled: Boolean) = reduce { it.copy(timeline = it.timeline.copy(snapEnabled = enabled)) }

    fun select(trackId: String?, clipId: String?) = reduce {
        it.copy(selection = it.selection.copy(selectedTrackId = trackId, selectedClipId = clipId))
    }

    fun setExportProgress(progressPercent: Int) = reduce {
        it.copy(export = EditorState.ExportState.InProgress(progressPercent.coerceIn(0, 100)))
    }

    fun completeExport(outputUri: Uri) = reduce { it.copy(export = EditorState.ExportState.Completed(outputUri)) }

    fun failExport(reason: String) = reduce {
        it.copy(
            export = EditorState.ExportState.Failed(reason),
            session = it.session.copy(lastError = reason),
        )
    }

    fun undo() = lock.withLock {
        if (undoStack.isEmpty()) return
        val previous = undoStack.removeLast()
        redoStack.addLast(_state.value)
        _state.value = previous.withHistory(undoStack.size, canUndo = undoStack.isNotEmpty(), canRedo = true)
    }

    fun redo() = lock.withLock {
        if (redoStack.isEmpty()) return
        val next = redoStack.removeLast()
        undoStack.addLast(_state.value)
        _state.value = next.withHistory(undoStack.size, canUndo = true, canRedo = redoStack.isNotEmpty())
    }

    private inline fun reduce(transform: (EditorState) -> EditorState) = lock.withLock {
        val current = _state.value
        val updated = transform(current)
        if (updated == current) return

        undoStack.addLast(current)
        if (undoStack.size > maxHistory) undoStack.removeFirst()
        redoStack.clear()
        _state.update { updated.withHistory(undoStack.size, canUndo = true, canRedo = false) }
    }

    private fun EditorState.withHistory(size: Int, canUndo: Boolean, canRedo: Boolean): EditorState {
        return copy(history = history.copy(stackSize = size, canUndo = canUndo, canRedo = canRedo))
    }
}
