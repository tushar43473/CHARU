package com.app.clipsteronline.upload.editor.ui.widgets

class ClipTrimHandle {
    var startMs: Long = 0L
    var endMs: Long = 0L
    fun configure() = Unit
    fun setRange(startMs: Long, endMs: Long) { this.startMs = startMs.coerceAtLeast(0L); this.endMs = endMs.coerceAtLeast(this.startMs) }
}
