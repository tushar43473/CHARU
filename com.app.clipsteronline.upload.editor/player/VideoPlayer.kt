package com.app.clipsteronline.upload.editor.player

import android.net.Uri
import android.view.Surface

class VideoPlayer {
    private var mediaUri: Uri? = null
    private var outputSurface: Surface? = null

    fun prepare(uri: Uri) {
        mediaUri = uri
    }

    fun attachSurface(surface: Surface?) {
        outputSurface = surface
    }

    fun clearSurface() {
        outputSurface = null
    }

    fun release() {
        mediaUri = null
        outputSurface = null
    }

    fun hasMediaLoaded(): Boolean = mediaUri != null
    fun hasSurfaceAttached(): Boolean = outputSurface != null
}
