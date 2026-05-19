package com.app.clipsteronline.upload.editor.ui.toolbar

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Undo/redo controller.
 * Timeline state history, action stack, rollback.
 */
class UndoRedoController(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {
    private val undoStack = mutableListOf<EditorAction>()
    private val redoStack = mutableListOf<EditorAction>()

    private var maxHistorySize = 50

    /**
     * Push action to undo stack.
     */
    fun push(action: EditorAction) {
        undoStack.add(action)

        // Clear redo stack on new action
        redoStack.clear()

        // Trim if too large
        while (undoStack.size > maxHistorySize) {
            undoStack.removeAt(0)
        }
    }

    /**
     * Undo last action.
     */
    fun undo(): EditorAction? {
        if (!canUndo()) return null

        val action = undoStack.removeLast()
        redoStack.add(action)

        return action
    }

    /**
     * Redo last undone action.
     */
    fun redo(): EditorAction? {
        if (!canRedo()) return null

        val action = redoStack.removeLast()
        undoStack.add(action)

        return action
    }

    /**
     * Can undo.
     */
    fun canUndo(): Boolean = undoStack.isNotEmpty()

    /**
     * Can redo.
     */
    fun canRedo(): Boolean = redoStack.isNotEmpty()

    /**
     * Clear all history.
     */
    fun clear() {
        undoStack.clear()
        redoStack.clear()
    }

    /**
     * Set history size limit.
     */
    fun setMaxHistorySize(size: Int) {
        maxHistorySize = size.coerceIn(10, 200)
    }

    /**
     * Merge last action.
     */
    fun merge(action: EditorAction) {
        if (undoStack.isNotEmpty()) {
            val last = undoStack.last()
            if (last.type == action.type && last.clipId == action.clipId) {
                // Merge with last
                undoStack.removeLast()
                push(EditorAction(
                    type = last.type + "_merged",
                    clipId = last.clipId,
                    data = action.data
                ))
                return
            }
        }
        push(action)
    }
}

/**
 * Editor action for undo/redo.
 */
data class EditorAction(
    val type: String,
    val clipId: String = "",
    val data: ActionData = ActionData(),
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Action data.
 */
data class ActionData(
    val startMs: Long = 0L,
    val endMs: Long = 0L,
    val trackId: String = "",
    val properties: Map<String, Any> = emptyMap()
)

/**
 * Action types.
 */
object ActionTypes {
    const val CLIP_ADD = "clip_add"
    const val CLIP_DELETE = "clip_delete"
    const val CLIP_MOVE = "clip_move"
    const val CLIP_TRIM = "clip_trim"
    const val CLIP_SPLIT = "clip_split"
    const val CLIPS_MERGE = "clips_merge"
    const val TEXT_ADD = "text_add"
    const val TEXT_UPDATE = "text_update"
    const val TEXT_DELETE = "text_delete"
    const val STICKER_ADD = "sticker_add"
    const val FILTER_CHANGE = "filter_change"
    const val EFFECT_CHANGE = "effect_change"
    const val SPEED_CHANGE = "speed_change"
    const val AUDIO_ADJUST = "audio_adjust"
}