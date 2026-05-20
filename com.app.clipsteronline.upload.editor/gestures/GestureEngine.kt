package com.app.clipsteronline.upload.editor.gestures

import android.view.Choreographer
import com.app.clipsteronline.upload.editor.timeline.engine.TimelineScrollEngine
import com.app.clipsteronline.upload.editor.timeline.engine.TimelineScrollState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GestureEngine(
    private val scrollEngine: TimelineScrollEngine,
    private val choreographer: Choreographer = Choreographer.getInstance(),
) : Choreographer.FrameCallback {

    private var lastFrameNs: Long = 0L
    private var ticking = false

    private val _motion = MutableStateFlow(scrollEngine.state.value)
    val motion: StateFlow<TimelineScrollState> = _motion.asStateFlow()

    @Synchronized
    fun startMotionLoop() {
        if (ticking) return
        ticking = true
        lastFrameNs = 0L
        choreographer.postFrameCallback(this)
    }

    @Synchronized
    fun stopMotionLoop() {
        if (!ticking) return
        ticking = false
        choreographer.removeFrameCallback(this)
        scrollEngine.stop()
        _motion.value = scrollEngine.state.value
    }

    override fun doFrame(frameTimeNanos: Long) {
        if (!ticking) return
        val deltaMs = if (lastFrameNs == 0L) 16L else ((frameTimeNanos - lastFrameNs) / 1_000_000L).coerceIn(8L, 33L)
        lastFrameNs = frameTimeNanos

        val state = scrollEngine.computeNextFrame(deltaMs)
        _motion.value = state

        if (state.isFlinging || kotlin.math.abs(state.velocityX) > 0.1 || kotlin.math.abs(state.velocityY) > 0.1) {
            choreographer.postFrameCallback(this)
        } else {
            ticking = false
        }
    }
}
