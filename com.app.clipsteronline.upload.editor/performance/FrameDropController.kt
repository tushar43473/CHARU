package com.app.clipsteronline.upload.editor.performance

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Frame drop controller.
 * Adaptive rendering, FPS monitoring.
 */
class FrameDropController(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {
    private val _dropState = MutableStateFlow(DropState())
    val dropState: StateFlow<DropState> = _dropState.asStateFlow()

    private var targetFPS = 60
    private var minFPS = 30
    private var frameDropCount = 0
    private var lastFrameTime = 0L
    private var currentFPS = 60f

    private val frameTimestamps = ArrayDeque<Long>(60)
    private var isAdaptiveMode = true
    private var qualityLevel = QualityLevel.HIGH

    /**
     * Record frame.
     */
    fun recordFrame() {
        val now = System.nanoTime()
        frameTimestamps.addLast(now)

        if (frameTimestamps.size > 60) {
            frameTimestamps.removeFirst()
        }

        // Calculate FPS
        if (frameTimestamps.size >= 2) {
            val oldest = frameTimestamps.first()
            val duration = (now - oldest) / 1_000_000_000f
            currentFPS = (frameTimestamps.size - 1) / duration

            // Detect drops
            if (currentFPS < minFPS) {
                frameDropCount++
            }
        }

        updateState()
    }

    /**
     * Adapt quality based on FPS.
     */
    suspend fun adaptQuality() {
        if (!isAdaptiveMode) return

        when {
            currentFPS < minFPS -> {
                qualityLevel = qualityLevel.previousOrLow()
                frameDropCount = 0
            }
            currentFPS >= targetFPS - 5 -> {
                if (qualityLevel != QualityLevel.HIGH && frameDropCount < 3) {
                    qualityLevel = qualityLevel.nextOrHigh()
                }
            }
        }

        delay(1000) // Wait before adjusting again
        updateState()
    }

    /**
     * Set target FPS.
     */
    fun setTargetFPS(fps: Int) {
        targetFPS = fps.coerceIn(24, 60)
    }

    /**
     * Set adaptive mode.
     */
    fun setAdaptiveMode(enabled: Boolean) {
        isAdaptiveMode = enabled
    }

    /**
     * Get quality level.
     */
    fun getQualityLevel(): QualityLevel = qualityLevel

    /**
     * Should skip frames.
     */
    fun shouldSkipFrames(): Boolean {
        return qualityLevel <= QualityLevel.MEDIUM && isAdaptiveMode
    }

    /**
     * Get frame skip interval.
     */
    fun getFrameSkipInterval(): Int {
        return when (qualityLevel) {
            QualityLevel.HIGH -> 1
            QualityLevel.MEDIUM -> 2
            QualityLevel.LOW -> 3
        }
    }

    /**
     * Reset stats.
     */
    fun resetStats() {
        frameDropCount = 0
        frameTimestamps.clear()
        currentFPS = targetFPS.toFloat()
        updateState()
    }

    /**
     * Get current FPS.
     */
    fun getCurrentFPS(): Float = currentFPS

    /**
     * Get drop percentage.
     */
    fun getDropPercentage(): Float {
        return if (targetFPS > 0) {
            ((targetFPS - currentFPS).coerceAtLeast(0f) / targetFPS * 100)
        } else 0f
    }

    private fun updateState() {
        _dropState.value = DropState(
            currentFPS = currentFPS,
            targetFPS = targetFPS,
            droppedFrames = frameDropCount,
            qualityLevel = qualityLevel,
            dropPercentage = getDropPercentage()
        )
    }
}

/**
 * Drop state.
 */
data class DropState(
    val currentFPS: Float = 60f,
    val targetFPS: Int = 60,
    val droppedFrames: Int = 0,
    val qualityLevel: QualityLevel = QualityLevel.HIGH,
    val dropPercentage: Float = 0f
)

/**
 * Quality levels.
 */
enum class QualityLevel {
    HIGH,
    MEDIUM,
    LOW;

    fun previousOrLow(): QualityLevel = when (this) {
        HIGH -> MEDIUM
        else -> LOW
    }

    fun nextOrHigh(): QualityLevel = when (this) {
        LOW -> MEDIUM
        else -> HIGH
    }
}