package com.app.clipsteronline.upload.editor.ui.toolbar

class UndoRedoController {
    private val undoStack = ArrayDeque<String>()
    private val redoStack = ArrayDeque<String>()
    fun configure() = Unit
    fun push(state: String) { undoStack.addLast(state); redoStack.clear() }
    fun undo(): String? { val v = undoStack.removeLastOrNull() ?: return null; redoStack.addLast(v); return undoStack.lastOrNull() }
    fun redo(): String? { val v = redoStack.removeLastOrNull() ?: return null; undoStack.addLast(v); return v }
}
