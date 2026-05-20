package com.app.clipsteronline.upload.editor.ui.widgets

class TimelineScrollbar {
    var position: Float = 0f
    var viewportFraction: Float = 0.2f
    fun configure() = Unit
    fun update(position: Float, viewportFraction: Float) { this.position = position.coerceIn(0f, 1f); this.viewportFraction = viewportFraction.coerceIn(0.05f, 1f) }
}
