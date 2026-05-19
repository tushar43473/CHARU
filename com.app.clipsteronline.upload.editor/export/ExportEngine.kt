package com.app.clipsteronline.upload.editor.export

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Central export coordinator.
 * Manages export lifecycle and execution.
 */
class ExportEngine(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) {
    private val _exportState = MutableStateFlow(ExportEngineState())
    val exportState: StateFlow<ExportEngineState> = _exportState.asStateFlow()

    private var currentSession: ExportSession? = null
    private var exportJob: Job? = null

    private val pipeline = ExportPipeline()
    private val progressTracker = ExportProgressTracker()

    /**
     * Start export.
     */
    fun startExport(config: ExportConfig, outputUri: Uri): ExportSession {
        val session = ExportSession(
            config = config,
            outputUri = outputUri,
            startTime = System.currentTimeMillis()
        )

        currentSession = session

        _exportState.value = _exportState.value.copy(
            currentSession = session,
            isExporting = true,
            error = null
        )

        exportJob = scope.launch {
            executeExport(session)
        }

        return session
    }

    /**
     * Execute export.
     */
    private suspend fun executeExport(session: ExportSession) = withContext(Dispatchers.IO) {
        try {
            _exportState.value = _exportState.value.copy(
                status = ExportStatus.EXPORTING
            )

            // Process pipeline
            pipeline.process(session) { progress ->
                _exportState.value = _exportState.value.copy(
                    progress = progress
                )
            }

            // Complete
            _exportState.value = _exportState.value.copy(
                status = ExportStatus.COMPLETED,
                isExporting = false,
                currentSession = session.copy(
                    endTime = System.currentTimeMillis()
                )
            )
        } catch (e: Exception) {
            _exportState.value = _exportState.value.copy(
                status = ExportStatus.FAILED,
                isExporting = false,
                error = e.message
            )
        }
    }

    /**
     * Cancel export.
     */
    fun cancelExport() {
        exportJob?.cancel()
        currentSession?.let {
            _exportState.value = _exportState.value.copy(
                status = ExportStatus.CANCELLED,
                isExporting = false,
                currentSession = it.copy(
                    endTime = System.currentTimeMillis()
                )
            )
        }
        currentSession = null
    }

    /**
     * Pause export.
     */
    fun pauseExport() {
        if (_exportState.value.isExporting) {
            _exportState.value = _exportState.value.copy(
                status = ExportStatus.PAUSED,
                isPaused = true
            )
        }
    }

    /**
     * Resume export.
     */
    fun resumeExport() {
        if (_exportState.value.isPaused) {
            _exportState.value = _exportState.value.copy(
                status = ExportStatus.EXPORTING,
                isPaused = false
            )
        }
    }

    /**
     * Get current session.
     */
    fun getCurrentSession(): ExportSession? = currentSession

    /**
     * Release resources.
     */
    fun release() {
        cancelExport()
    }
}

/**
 * Export engine state.
 */
data class ExportEngineState(
    val status: ExportStatus = ExportStatus.IDLE,
    val isExporting: Boolean = false,
    val isPaused: Boolean = false,
    val progress: Float = 0f,
    val currentSession: ExportSession? = null,
    val error: String? = null
)

/**
 * Export status.
 */
enum class ExportStatus {
    IDLE,
    PREPARING,
    EXPORTING,
    PAUSED,
    COMPLETED,
    FAILED,
    CANCELLED
}

/**
 * Export configuration.
 */
data class ExportConfig(
    val quality: ExportQuality = ExportQuality.MEDIUM,
    val format: ExportFormat = ExportFormat.MP4,
    val resolution: Pair<Int, Int> = 1920 to 1080,
    val frameRate: Int = 30,
    val videoBitrate: String = "8M",
    val audioBitrate: String = "192k",
    val useHardwareAcceleration: Boolean = false,
    val includeAudio: Boolean = true,
    val includeVideo: Boolean = true
)

/**
 * Export format.
 */
enum class ExportFormat {
    MP4,
    MOV,
    WEBM
}