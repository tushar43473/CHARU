package com.app.clipsteronline.upload.editor.timeline.sync

import android.view.Choreographer
import kotlin.math.max

class TimelineFrameSync(
    private val callback: (deltaUs: Long) -> Unit,
    private val choreographer: Choreographer = Choreographer.getInstance(),
) : Choreographer.FrameCallback {
    private var running = false
    private var lastFrameNs = 0L

    fun start() {
        if (running) return
        running = true
        lastFrameNs = 0L
        choreographer.postFrameCallback(this)
    }

    fun stop() {
        running = false
        choreographer.removeFrameCallback(this)
        lastFrameNs = 0L
    }

    override fun doFrame(frameTimeNanos: Long) {
        if (!running) return
        val deltaUs = if (lastFrameNs == 0L) 16_666L else max(1_000L, (frameTimeNanos - lastFrameNs) / 1_000L)
        lastFrameNs = frameTimeNanos
        callback(deltaUs)
        choreographer.postFrameCallback(this)
    }
}
