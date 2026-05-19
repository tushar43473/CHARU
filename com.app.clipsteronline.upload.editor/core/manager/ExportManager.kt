package com.app.clipsteronline.upload.editor.core.manager

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.app.clipsteronline.upload.editor.core.model.ExportConfig
import java.io.File
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Manager for export operations.
 * Handles FFmpeg execution, progress tracking, and export sessions.
 */
class ExportManager(
    private val context: Context
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _exportState = MutableStateFlow<ExportState>(ExportState.Idle)
    val exportState: StateFlow<ExportState> = _exportState.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    private val exportQueue = ConcurrentLinkedQueue<ExportTask>()
    private var currentExportJob: Job? = null
    private var isCancelled = false

    companion object {
        private const val PROGRESS_UPDATE_INTERVAL = 500L
    }

    /**
     * Start export with config.
     */
    fun startExport(config: ExportConfig, outputUri: Uri): Boolean {
        if (_exportState.value is ExportState.Exporting) {
            return false
        }

        isCancelled = false
        _exportState.value = ExportState.Preparing
        _progress.value = 0f

        currentExportJob = scope.launch {
            executeExport(config, outputUri)
        }

        return true
    }

    /**
     * Start export to file.
     */
    fun startExportToFile(config: ExportConfig, outputFile: File): Boolean {
        if (_exportState.value is ExportState.Exporting) {
            return false
        }

        isCancelled = false
        _exportState.value = ExportState.Preparing
        _progress.value = 0f

        currentExportJob = scope.launch {
            executeExportToFile(config, outputFile)
        }

        return true
    }

    /**
     * Cancel export.
     */
    fun cancelExport() {
        isCancelled = true
        currentExportJob?.cancel()
        _exportState.value = ExportState.Cancelled
    }

    /**
     * Pause export.
     */
    fun pauseExport() {
        if (_exportState.value is ExportState.Exporting) {
            _exportState.value = ExportState.Paused
        }
    }

    /**
     * Resume export.
     */
    fun resumeExport() {
        if (_exportState.value is ExportState.Paused) {
            _exportState.value = ExportState.Exporting(0f, "Encoding")
        }
    }

    /**
     * Add export to queue.
     */
    fun queueExport(config: ExportConfig, outputUri: Uri, priority: Int = 0) {
        exportQueue.offer(ExportTask(config, outputUri, priority))
        processQueue()
    }

    /**
     * Get queue size.
     */
    fun getQueueSize(): Int = exportQueue.size

    /**
     * Clear export queue.
     */
    fun clearQueue() {
        exportQueue.clear()
    }

    /**
     * Execute export to URI.
     */
    private suspend fun executeExport(config: ExportConfig, outputUri: Uri) {
        _exportState.value = ExportState.Exporting(0f, "Starting")

        withContext(Dispatchers.IO) {
            try {
                val outputFile = File(outputUri.path ?: return@withContext)

                // Execute FFmpeg command
                executeFFmpeg(config, outputFile)

                _exportState.value = ExportState.Completed
                _progress.value = 1f
            } catch (e: Exception) {
                if (isCancelled) {
                    _exportState.value = ExportState.Cancelled
                } else {
                    _exportState.value = ExportState.Failed(e.message ?: "Export failed")
                }
            }
        }
    }

    /**
     * Execute export to file.
     */
    private suspend fun executeExportToFile(config: ExportConfig, outputFile: File) {
        _exportState.value = ExportState.Exporting(0f, "Starting")

        withContext(Dispatchers.IO) {
            try {
                executeFFmpeg(config, outputFile)

                _exportState.value = ExportState.Completed
                _progress.value = 1f
            } catch (e: Exception) {
                if (isCancelled) {
                    _exportState.value = ExportState.Cancelled
                } else {
                    _exportState.value = ExportState.Failed(e.message ?: "Export failed")
                }
            }
        }
    }

    /**
     * Execute FFmpeg command.
     */
    private suspend fun executeFFmpeg(config: ExportConfig, outputFile: File) {
        val command = buildFFmpegCommand(config, outputFile)

        _exportState.value = ExportState.Exporting(0f, "Encoding")

        val process = Runtime.getRuntime().exec(command)
        val inputStream = process.inputStream
        val errorStream = process.errorStream

        // Read output
        val buffer = ByteArray(4096)
        var bytesRead: Int

        while (true) {
            if (isCancelled) {
                process.destroy()
                throw Exception("Export cancelled")
            }

            bytesRead = inputStream.read(buffer)
            if (bytesRead == -1) break

            // Parse progress from output
            parseProgress(String(buffer, 0, bytesRead))

            kotlinx.coroutines.delay(PROGRESS_UPDATE_INTERVAL)
        }

        process.waitFor()
    }

    /**
     * Build FFmpeg command.
     */
    private fun buildFFmpegCommand(config: ExportConfig, outputFile: File): List<String> {
        return listOf(
            "ffmpeg",
            "-y",
            "-i", config.inputPath,
            "-c:v", config.codec.name.lowercase(),
            "-c:a", config.audioCodec.name.lowercase(),
            "-b:v", "${config.bitRate}",
            "-r", "${config.frameRate}",
            "-s", "${config.resolution.width}x${config.resolution.height}",
            "-b:a", "${config.audioSettings.bitRate}",
            "-ar", "${config.audioSettings.sampleRate}",
            "-ac", "${config.audioSettings.channelCount}",
            outputFile.absolutePath
        )
    }

    /**
     * Parse progress from FFmpeg output.
     */
    private fun parseProgress(output: String) {
        // Extract time and calculate progress
        val timeMatch = Regex("time=(\\d+):(\\d+):(\\d+\\.\\d+)").find(output)
        if (timeMatch != null) {
            val hours = timeMatch.groupValues[1].toIntOrNull() ?: 0
            val minutes = timeMatch.groupValues[2].toIntOrNull() ?: 0
            val seconds = timeMatch.groupValues[3].toDoubleOrNull() ?: 0.0

            val currentTimeMs = ((hours * 3600 + minutes * 60 + seconds) * 1000).toLong()
            // Progress would be calculated from total duration
            // This is a simplified version
        }

        _exportState.value = ExportState.Exporting(_progress.value, "Encoding")
    }

    /**
     * Process export queue.
     */
    private fun processQueue() {
        if (exportQueue.isEmpty() || _exportState.value is ExportState.Exporting) {
            return
        }

        val task = exportQueue.poll() ?: return
        startExport(task.config, task.outputUri)
    }
}

/**
 * Export state.
 */
sealed class ExportState {
    data object Idle : ExportState()
    data object Preparing : ExportState()
    data class Exporting(val progress: Float, val step: String) : ExportState()
    data object Paused : ExportState()
    data object Cancelled : ExportState()
    data object Completed : ExportState()
    data class Failed(val message: String) : ExportState()
}

/**
 * Export task.
 */
data class ExportTask(
    val config: ExportConfig,
    val outputUri: Uri,
    val priority: Int = 0
)