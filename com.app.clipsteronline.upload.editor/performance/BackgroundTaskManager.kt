package com.app.clipsteronline.upload.editor.performance

import android.content.Context
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Background task manager.
 * Centralized coroutine, task scheduling.
 */
class BackgroundTaskManager(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) {
    private val _taskState = MutableStateFlow(TaskState())
    val taskState: StateFlow<TaskState> = _taskState.asStateFlow()

    private val exportTasks = mutableMapOf<String, Job>()
    private val thumbnailTasks = mutableMapOf<String, Job>()
    private var maxExportTasks = 1
    private var maxThumbnailTasks = 3

    /**
     * Schedule export task.
     */
    fun scheduleExport(
        taskId: String,
        task: suspend () -> Result<Unit>
    ): Job {
        cancelExport(taskId)

        val job = scope.launch {
            _taskState.value = _taskState.value.copy(activeExports = _taskState.value.activeExports + 1)

            try {
                task()
            } catch (e: Exception) {
                _taskState.value = _taskState.value.copy(lastError = e.message)
            } finally {
                exportTasks.remove(taskId)
                updateState()
            }
        }

        exportTasks[taskId] = job
        updateState()

        return job
    }

    /**
     * Schedule thumbnail task.
     */
    fun scheduleThumbnail(
        taskId: String,
        task: suspend () -> Unit
    ): Job {
        cancelThumbnail(taskId)

        if (thumbnailTasks.size >= maxThumbnailTasks) {
            // Wait for slot
        }

        val job = scope.launch {
            try {
                task()
            } catch (e: Exception) {
                // Log error
            } finally {
                thumbnailTasks.remove(taskId)
                updateState()
            }
        }

        thumbnailTasks[taskId] = job
        updateState()

        return job
    }

    /**
     * Cancel export.
     */
    fun cancelExport(taskId: String) {
        exportTasks[taskId]?.cancel()
        exportTasks.remove(taskId)
        updateState()
    }

    /**
     * Cancel thumbnail.
     */
    fun cancelThumbnail(taskId: String) {
        thumbnailTasks[taskId]?.cancel()
        thumbnailTasks.remove(taskId)
        updateState()
    }

    /**
     * Cancel all.
     */
    fun cancelAll() {
        exportTasks.values.forEach { it.cancel() }
        thumbnailTasks.values.forEach { it.cancel() }
        exportTasks.clear()
        thumbnailTasks.clear()
        updateState()
    }

    /**
     * Pause all.
     */
    fun pauseAll() {
        scope.coroutineContext[Job]?.cancelchildren()
        updateState()
    }

    private fun updateState() {
        _taskState.value = _taskState.value.copy(
            activeExports = exportTasks.size,
            activeThumbnails = thumbnailTasks.size,
            isIdle = exportTasks.isEmpty() && thumbnailTasks.isEmpty()
        )
    }
}

/**
 * Task state.
 */
data class TaskState(
    val activeExports: Int = 0,
    val activeThumbnails: Int = 0,
    val isIdle: Boolean = true,
    val lastError: String? = null
)