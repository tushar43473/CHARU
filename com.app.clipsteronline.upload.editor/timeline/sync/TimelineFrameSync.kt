package com.app.clipsteronline.upload.editor.timeline.sync

import android.view.Choreographer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Synchronizes frames with timeline rendering.
 * Uses Choreographer for smooth updates.
 */
class TimelineFrameSync(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
) {
    private val _frameState = MutableStateFlow(FrameState())
    val frameState: StateFlow<FrameState> = _frameState.asStateFlow()

    private var choreographer: Choreographer? = null
    private var frameCallback: Choreographer.FrameCallback? = null
    private var isRunning = false

    private var frameTick = 0L
    private var lastFrameTime = 0L

    private var listener: FrameTickListener? = null

    companion object {
        private const val TARGET_FPS = 60
        private const val TARGET_FRAME_TIME = 1000L / TARGET_FPS
    }

    /**
     * Start frame synchronization.
     */
    fun startSync() {
        if (isRunning) return
        isRunning = true

        choreographer = Choreographer.getInstance()
        frameCallback = object : Choreographer.FrameCallback {
            override fun doFrame(frameTimeNanos: Long) {
                if (!isRunning) return

                val currentTime = System.nanoTime()
                processFrame(currentTime)

                choreographer?.postFrameCallback(this)
            }
        }

        choreographer?.postFrameCallback(frameCallback!!)

        _frameState.value = _frameState.value.copy(
            isSyncing = true
        )
    }

    /**
     * Stop frame synchronization.
     */
    fun stopSync() {
        isRunning = false
        frameCallback?.let { choreographer?.removeFrameCallback(it) }
        frameCallback = null

        _frameState.value = _frameState.value.copy(
            isSyncing = false
        )
    }

    /**
     * Process frame.
     */
    private fun processFrame(frameTimeNanos: Long) {
        val currentTimeMs = frameTimeNanos / 1_000_000

        if (lastFrameTime > 0) {
            val delta = currentTimeMs - lastFrameTime
            frameTick++

            _frameState.value = _frameState.value.copy(
                frameTick = frameTick,
                deltaMs = delta,
                lastFrameTime = currentTimeMs,
                fps = (1000f / delta).coerceIn(0f, TARGET_FPS.toFloat())
            )

            listener?.onFrameTick(frameTick, delta)
        }

        lastFrameTime = currentTimeMs
    }

    /**
     * Set frame tick listener.
     */
    fun setTickListener(listener: FrameTickListener?) {
        this.listener = listener
    }

    /**
     * Get current tick.
     */
    fun getCurrentTick(): Long = frameTick

    /**
     * Release resources.
     */
    fun release() {
        stopSync()
        listener = null
    }
}

/**
 * Frame state.
 */
data class FrameState(
    val frameTick: Long = 0L,
    val deltaMs: Long = 0L,
    val lastFrameTime: Long = 0L,
    val fps: Float = 0f,
    val isSyncing: Boolean = false
)

/**
 * Frame tick listener.
 */
interface FrameTickListener {
    fun onFrameTick(tick: Long, deltaMs: Long)
}