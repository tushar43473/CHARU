package com.app.clipsteronline.upload.editor.export

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Tracks export progress from FFmpeg output.
 * Parses logs and calculates ETA.
 */
class ExportProgressTracker(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) {
    private val _progressState = MutableStateFlow(ProgressState())
    val progressState: StateFlow<ProgressState> = _progressState.asStateFlow()

    private var startTimeMs = 0L
    private var lastTimeMs = 0L
    private var processedFrames = 0
    private var totalFrames = 0

    /**
     * Start tracking.
     */
    fun start(totalFrames: Int) {
        startTimeMs = System.currentTimeMillis()
        lastTimeMs = startTimeMs
        this.totalFrames = totalFrames
        processedFrames = 0

        _progressState.value = ProgressState(
            progress = 0f,
            isTracking = true
        )
    }

    /**
     * Parse FFmpeg output.
     */
    fun parseFFmpegOutput(output: String) {
        // Parse frame count
        val frameMatch = Regex("frame=\\s*(\\d+)").find(output)
        frameMatch?.let {
            processedFrames = it.groupValues[1].toIntOrNull() ?: processedFrames
        }

        // Parse time
        val timeMatch = Regex("time=(\\d+):(\\d+):(\\d+\\.\\d+)").find(output)
        timeMatch?.let {
            val hours = it.groupValues[1].toLongOrNull() ?: 0L
            val minutes = it.groupValues[2].toLongOrNull() ?: 0L
            val seconds = it.groupValues[3].toDoubleOrNull() ?: 0.0

            val currentTimeMs = ((hours * 3600 + minutes * 60 + seconds) * 1000).toLong()
            updateProgress(currentTimeMs)
        }

        // Parse speed
        val speedMatch = Regex("speed=\\s*([\\d.]+)x").find(output)
        speedMatch?.let {
            val speed = it.groupValues[1].toFloatOrNull() ?: 1f
            _progressState.value = _progressState.value.copy(speed = speed)
        }
    }

    /**
     * Update progress from time.
     */
    fun updateProgress(currentTimeMs: Long) {
        val elapsed = currentTimeMs - startTimeMs
        val progress = if (totalFrames > 0) {
            processedFrames.toFloat() / totalFrames
        } else {
            0f
        }

        // Calculate ETA
        val estimatedRemaining = if (progress > 0) {
            ((elapsed / progress) - elapsed)
        } else {
            0L
        }

        val currentTime = System.currentTimeMillis()
        val delta = currentTime - lastTimeMs
        val fps = if (delta > 0) 1000f / delta else 0f

        lastTimeMs = currentTime

        _progressState.value = _progressState.value.copy(
            progress = progress.coerceIn(0f, 1f),
            processedFrames = processedFrames,
            elapsedTimeMs = elapsed,
            estimatedRemainingMs = estimatedRemaining,
            fps = fps
        )
    }

    /**
     * Get ETA string.
     */
    fun getETAString(): String {
        val remaining = _progressState.value.estimatedRemainingMs
        if (remaining <= 0) return "Calculating..."

        val seconds = remaining / 1000
        val minutes = seconds / 60
        val hours = minutes / 60

        return when {
            hours > 0 -> "${hours}h ${minutes % 60}m"
            minutes > 0 -> "${minutes}m ${seconds % 60}s"
            else -> "${seconds}s"
        }
    }

    /**
     * Stop tracking.
     */
    fun stop() {
        _progressState.value = _progressState.value.copy(
            isTracking = false,
            progress = 1f
        )
    }

    /**
     * Reset.
     */
    fun reset() {
        startTimeMs = 0L
        lastTimeMs = 0L
        processedFrames = 0
        totalFrames = 0
        _progressState.value = ProgressState()
    }
}

/**
 * Progress state.
 */
data class ProgressState(
    val progress: Float = 0f,
    val processedFrames: Int = 0,
    val elapsedTimeMs: Long = 0L,
    val estimatedRemainingMs: Long = 0L,
    val speed: Float = 0f,
    val fps: Float = 0f,
    val isTracking: Boolean = false
) {
    /**
     * Get progress percentage.
     */
    fun getProgressPercent(): Int = (progress * 100).toInt()

    /**
     * Get elapsed string.
     */
    fun getElapsedString(): String {
        val seconds = elapsedTimeMs / 1000
        val minutes = seconds / 60
        val hours = minutes / 60

        return when {
            hours > 0 -> String.format("%d:%02d:%02d", hours, minutes % 60, seconds % 60)
            else -> String.format("%d:%02d", minutes, seconds % 60)
        }
    }
}