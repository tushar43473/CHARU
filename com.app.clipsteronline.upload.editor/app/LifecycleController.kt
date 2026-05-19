package com.app.clipsteronline.upload.editor.app

import android.app.Activity
import android.app.Service
import android.content.ComponentCallbacks2
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Lifecycle controller.
 * Activity lifecycle handling.
 */
class LifecycleController(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {
    private val _lifecycleState = MutableStateFlow(LifecycleState())
    val lifecycleState: StateFlow<LifecycleState> = _lifecycleState.asStateFlow()

    private var isPaused = false
    private var isStopped = false

    /**
     * On activity start.
     */
    fun onStart() {
        isPaused = false
        isStopped = false

        _lifecycleState.value = LifecycleState(
            state = LifecycleStateType.STARTED,
            isForeground = true
        )

        resumePlayback()
    }

    /**
     * On activity resume.
     */
    fun onResume() {
        isPaused = false

        _lifecycleState.value = _lifecycleState.value.copy(
            state = LifecycleStateType.RESUMED
        )

        resumePlayback()
        resumeRender()
    }

    /**
     * On activity pause.
     */
    fun onPause() {
        isPaused = true

        _lifecycleState.value = _lifecycleState.value.copy(
            state = LifecycleStateType.PAUSED
        )

        pausePlayback()
    }

    /**
     * On activity stop.
     */
    fun onStop() {
        isStopped = true

        _lifecycleState.value = _lifecycleState.value.copy(
            state = LifecycleStateType.STOPPED,
            isForeground = false
        )

        pausePlayback()
        pauseRender()
    }

    /**
     * On destroy.
     */
    fun onDestroy() {
        _lifecycleState.value = _lifecycleState.value.copy(
            state = LifecycleStateType.DESTROYED
        )

        releaseResources()
    }

    /**
     * On low memory.
     */
    fun onLowMemory() {
        _lifecycleState.value = _lifecycleState.value.copy(
            isLowMemory = true
        )

        performMemoryCleanup()
    }

    /**
     * On trim memory.
     */
    fun onTrimMemory(level: Int) {
        when {
            level >= ComponentCallbacks2.TRIM_MEMORY_MODERATE -> {
                _lifecycleState.value = _lifecycleState.value.copy(
                    isLowMemory = true
                )
                performMemoryCleanup()
            }
        }
    }

    private fun pausePlayback() {
        _lifecycleState.value = _lifecycleState.value.copy(
            isPlaying = false
        )
    }

    private fun resumePlayback() {
        // Resume playback if was playing before
    }

    private fun pauseRender() {
        _lifecycleState.value = _lifecycleState.value.copy(
            isRendering = false
        )
    }

    private fun resumeRender() {
        _lifecycleState.value = _lifecycleState.value.copy(
            isRendering = true
        )
    }

    private fun performMemoryCleanup() {
        _lifecycleState.value = _lifecycleState.value.copy(
            memoryCleanupCount = _lifecycleState.value.memoryCleanupCount + 1
        )
    }

    private fun releaseResources() {
        _lifecycleState.value = _lifecycleState.value.copy(
            resourcesReleased = true
        )
    }
}

/**
 * Lifecycle state.
 */
data class LifecycleState(
    val state: LifecycleStateType = LifecycleStateType.IDLE,
    val isForeground: Boolean = false,
    val isPaused: Boolean = false,
    val isPlaying: Boolean = false,
    val isRendering: Boolean = false,
    val isLowMemory: Boolean = false,
    val memoryCleanupCount: Int = 0,
    val resourcesReleased: Boolean = false
)

/**
 * Lifecycle type.
 */
enum class LifecycleStateType {
    IDLE,
    STARTED,
    RESUMED,
    PAUSED,
    STOPPED,
    DESTROYED
}