package com.app.clipsteronline.upload.editor.ui.bottomsheet

class BottomMenuContainer {
    private val menus = linkedMapOf<String, Any>()
    fun configure() = Unit
    fun register(key: String, menu: Any) { menus[key] = menu }
    fun menuKeys(): List<String> = menus.keys.toList()
}
