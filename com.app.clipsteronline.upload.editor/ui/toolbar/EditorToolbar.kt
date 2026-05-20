package com.app.clipsteronline.upload.editor.ui.toolbar

class EditorToolbar {
    private val actions = mutableListOf<String>()
    fun configure() = Unit
    fun setActions(items: List<String>) { actions.clear(); actions.addAll(items) }
    fun visibleActions(): List<String> = actions.toList()
}
