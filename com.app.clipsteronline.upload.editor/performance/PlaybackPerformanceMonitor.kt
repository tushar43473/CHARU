package com.app.clipsteronline.upload.editor.performance

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Playback performance monitor.
 * FPS tracking, render latency, jank detection.
 */
class PlaybackPerformanceMonitor(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {
    private val _perfState = MutableStateFlow(PerformanceState())
    val perfState: StateFlow<PerformanceState> = _perfState.asStateFlow()

    private var currentFPS = 60f
    private var avgFrameTime = 16.67f
    private var maxFrameTime = 16.67f

    private var droppedFrames = 0
    private var totalFrames = 0
    private var jankyFrames = 0

    private val frameTimes = ArrayDeque<Float>(120)
    private var isRecording = false

    /**
     * Start recording.
     */
    fun startRecording() {
        isRecording = true
    }

    /**
     * Stop recording.
     */
    fun stopRecording() {
        isRecording = false
    }

    /**
     * Record frame render.
     */
    fun recordFrame(frameTimeMs: Float) {
        if (!isRecording) return

        frameTimes.addLast(frameTimeMs)
        if (frameTimes.size > 120) {
            frameTimes.removeFirst()
        }

        totalFrames++

        // Calculate FPS
        if (frameTimes.size >= 2) {
            avgFrameTime = frameTimes.average().toFloat()
            currentFPS = 1000f / avgFrameTime
        }

        // Track jank (>17ms)
        if (frameTimeMs > 16.67f) {
            jankyFrames++
            if (frameTimeMs > 33f) {
                droppedFrames++
            }
        }

        // Update max
        if (frameTimeMs > maxFrameTime) {
            maxFrameTime = frameTimeMs
        }

        updateState()
    }

    /**
     * Get smoothness score.
     */
    fun getSmoothnessScore(): Int {
        val jankPercent = if (totalFrames > 0) jankyFrames.toFloat() / totalFrames * 100 else 0f
        return when {
            jankPercent < 1 -> 100
            jankPercent < 5 -> 90
            jankPercent < 10 -> 80
            jankPercent < 20 -> 70
            else -> 50
        }
    }

    /**
     * Should lower quality.
     */
    fun shouldLowerQuality(): Boolean {
        return getSmoothnessScore() < 70 && totalFrames > 60
    }

    /**
     * Reset statistics.
     */
    fun reset() {
        frameTimes.clear()
        totalFrames = 0
        droppedFrames = 0
        jankyFrames = 0
        maxFrameTime = 16.67f
        currentFPS = 60f
        updateState()
    }

    /**
     * Get FPS.
     */
    fun getFPS(): Float = currentFPS

    /**
     * Get average frame time.
     */
    fun getAvgFrameTime(): Float = avgFrameTime

    /**
     * Get max frame time.
     */
    fun getMaxFrameTime(): Float = maxFrameTime

    /**
     * Get dropped frame count.
     */
    fun getDroppedFrames(): Int = droppedFrames

    private fun updateState() {
        _perfState.value = PerformanceState(
            fps = currentFPS,
            avgFrameTime = avgFrameTime,
            maxFrameTime = maxFrameTime,
            droppedFrames = droppedFrames,
            jankyFrames = jankyFrames,
            smoothness = getSmoothnessScore()
        )
    }
}

/**
 * Performance state.
 */
data class PerformanceState(
    val fps: Float = 60f,
    val avgFrameTime: Float = 16.67f,
    val maxFrameTime: Float = 16.67f,
    val droppedFrames: Int = 0,
    val jankyFrames: Int = 0,
    val smoothness: Int = 100
)

/**
 * Performance reporter.
 */
class PerformanceReporter {
    fun generateReport(state: PerformanceState): String {
        return buildString {
            appendLine("=== Playback Report ===")
            appendLine("FPS: ${state.fps}")
            appendLine("Avg Frame Time: ${state.avgFrameTime}ms")
            appendLine("Max Frame Time: ${state.maxFrameTime}ms")
            appendLine("Dropped Frames: ${state.droppedFrames}")
            appendLine("Janky Frames: ${state.jankyFrames}")
            appendLine("Smoothness: ${state.smoothness}%")
        }
    }
}