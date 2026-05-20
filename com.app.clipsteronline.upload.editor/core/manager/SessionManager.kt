package com.app.clipsteronline.upload.editor.core.manager

class SessionManager {
    var active: Boolean = false
        private set
    var sampleRate: Int = 0
        private set

    fun startSession(sampleRate: Int) {
        require(sampleRate in 8_000..192_000) { "Invalid sample rate: $sampleRate" }
        this.sampleRate = sampleRate
        active = true
    }

    fun stopSession() {
        active = false
    }
}
