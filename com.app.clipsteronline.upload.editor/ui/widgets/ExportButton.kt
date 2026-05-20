package com.app.clipsteronline.upload.editor.ui.widgets

class ExportButton {
    var enabled: Boolean = true
    var loading: Boolean = false
    fun configure() = Unit
    fun setExporting(exporting: Boolean) { loading = exporting; enabled = !exporting }
}
