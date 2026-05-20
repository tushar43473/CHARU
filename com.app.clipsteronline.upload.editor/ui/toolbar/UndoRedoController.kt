package com.app.clipsteronline.upload.editor.ui.toolbar

class UndoRedoController<T>(private val maxSteps: Int = 50) {
    private val undo = ArrayDeque<T>()
    private val redo = ArrayDeque<T>()

    fun push(state: T) {
        undo.addLast(state)
        if (undo.size > maxSteps) undo.removeFirst()
        redo.clear()
    }

    fun undo(current: T): T? {
        if (undo.isEmpty()) return null
        val previous = undo.removeLast()
        redo.addLast(current)
        return previous
    }

    fun redo(current: T): T? {
        if (redo.isEmpty()) return null
        val next = redo.removeLast()
        undo.addLast(current)
        return next
    }
}
