package com.app.clipsteronline.upload.editor.project

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Autosave engine.
 * Timed autosave, change detection, throttling.
 */
class AutosaveEngine(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {
    private val _autosaveState = MutableStateFlow(AutosaveState())
    val autosaveState: StateFlow<AutosaveState> = _autosaveState.asStateFlow()

    private var autosaveJob: Job? = null
    private var intervalMs = 30_000L // 30 seconds default

    private var lastSavedHash = 0
    private var isEnabled = true
    private var pendingChanges = false

    private var saveCallback: ((ProjectData, File) -> kotlinx.coroutines.Deferred<SaveResult>)? = null

    /**
     * Start autosave.
     */
    fun start(intervalSeconds: Long = 30, saveCallback: (ProjectData, File) -> kotlinx.coroutines.Deferred<SaveResult>) {
        this.intervalMs = intervalSeconds * 1000L
        this.saveCallback = saveCallback

        stop()

        autosaveJob = scope.launch {
            while (isActive && isEnabled) {
                delay(intervalMs)

                if (pendingChanges && saveCallback != null) {
                    performAutosave()
                }
            }
        }

        _autosaveState.value = _autosaveState.value.copy(isRunning = true)
    }

    /**
     * Stop autosave.
     */
    fun stop() {
        autosaveJob?.cancel()
        autosaveJob = null
        _autosaveState.value = _autosaveState.value.copy(isRunning = false)
    }

    /**
     * Mark changed.
     */
    fun markChanged(project: ProjectData) {
        pendingChanges = true

        val newHash = calculateProjectHash(project)
        if (newHash != lastSavedHash) {
            pendingChanges = true
            _autosaveState.value = _autosaveState.value.copy(
                pendingChanges = true,
                lastChangedAt = System.currentTimeMillis()
            )
        }
    }

    /**
     * Force autosave now.
     */
    suspend fun forceSaveNow(outputDir: File, project: ProjectData): SaveResult {
        pendingChanges = false
        _autosaveState.value = _autosaveState.value.copy(status = SaveStatus.SAVING)

        val result = saveCallback?.invoke(project, outputDir)?.await() ?: SaveResult.Error("No save callback")

        if (result is SaveResult.Success) {
            lastSavedHash = calculateProjectHash(project)
            pendingChanges = false

            _autosaveState.value = _autosaveState.value.copy(
                status = SaveStatus.IDLE,
                lastSavedAt = System.currentTimeMillis(),
                pendingChanges = false,
                saveCount = _autosaveState.value.saveCount + 1
            )
        } else {
            _autosaveState.value = _autosaveState.value.copy(
                status = SaveStatus.ERROR,
                lastError = (result as? SaveResult.Error)?.message
            )
        }

        return result
    }

    /**
     * Set enabled.
     */
    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
        _autosaveState.value = _autosaveState.value.copy(isEnabled = enabled)
    }

    /**
     * Set interval.
     */
    fun setInterval(seconds: Long) {
        intervalMs = (seconds * 1000L).coerceIn(10_000L, 300_000L)

        // Restart with new interval if running
        if (_autosaveState.value.isRunning && saveCallback != null) {
            // Note: Would need to restart - simplified here
        }
    }

    /**
     * Perform autosave.
     */
    private suspend fun performAutosave() {
        // Note: Would need to get current project and output dir
        // Simplified - actual implementation would integrate with editor state
    }

    /**
     * Calculate simple project hash.
     */
    private fun calculateProjectHash(project: ProjectData): Int {
        var hash = project.name.hashCode()
        hash = hash * 31 + project.modifiedAt.hashCode()
        hash = hash * 31 + project.clips.size
        hash = hash * 31 + project.effects.size
        hash = hash * 31 + project.timeline.duration.toInt()
        return hash
    }
}

/**
 * Autosave state.
 */
data class AutosaveState(
    val isRunning: Boolean = false,
    val isEnabled: Boolean = true,
    val status: SaveStatus = SaveStatus.IDLE,
    val pendingChanges: Boolean = false,
    val lastSavedAt: Long = 0L,
    val lastChangedAt: Long = 0L,
    val saveCount: Int = 0,
    val lastError: String? = null
)

/**
 * Save status.
 */
enum class SaveStatus {
    IDLE,
    SAVING,
    ERROR
}