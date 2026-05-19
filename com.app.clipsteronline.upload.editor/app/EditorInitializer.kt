package com.app.clipsteronline.upload.editor.app

import android.content.Context
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Editor initializer.
 * Startup sequence, background initialization.
 */
class EditorInitializer(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) {
    private val _initState = MutableStateFlow(InitState())
    val initState: StateFlow<InitState> = _initState.asStateFlow()

    private var initJob: Job? = null

    /**
     * Start initialization.
     */
    fun initialize(): Deferred<Boolean> = scope.async {
        _initState.value = InitState(status = InitStatus.INITIALIZING)

        try {
            // Initialize database
            _initState.value = _initState.value.copy(progress = 10)
            initializeDatabase()

            // Initialize caches
            _initState.value = _initState.value.copy(progress = 30)
            initializeCaches()

            // Initialize render
            _initState.value = _initState.value.copy(progress = 50)
            initializeRender()

            // Initialize media
            _initState.value = _initState.value.copy(progress = 70)
            initializeMedia()

            // Finalize
            _initState.value = _initState.value.copy(progress = 100)

            _initState.value = InitState(status = InitStatus.COMPLETE)
            true
        } catch (e: Exception) {
            _initState.value = InitState(status = InitStatus.ERROR, error = e.message)
            false
        }
    }

    /**
     * Initialize database.
     */
    private suspend fun initializeDatabase() = withContext(Dispatchers.IO) {
        val db = EditorDatabase.getInstance(context)
        _initState.value = _initState.value.copy(databaseReady = true)
    }

    /**
     * Initialize caches.
     */
    private suspend fun initializeCaches() = withContext(Dispatchers.IO) {
        val memory = upload.editor.performance.MemoryManager(context)
        _initState.value = _initState.value.copy(cachesReady = true)
    }

    /**
     * Initialize render.
     */
    private suspend fun initializeRender() = withContext(Dispatchers.IO) {
        _initState.value = _initState.value.copy(renderReady = true)
    }

    /**
     * Initialize media.
     */
    private suspend fun initializeMedia() = withContext(Dispatchers.IO) {
        _initState.value = _initState.value.copy(mediaReady = true)
    }

    /**
     * Cancel initialization.
     */
    fun cancel() {
        initJob?.cancel()
        _initState.value = InitState(status = InitStatus.CANCELLED)
    }
}

/**
 * Initialization state.
 */
data class InitState(
    val status: InitStatus = InitStatus.IDLE,
    val progress: Int = 0,
    val databaseReady: Boolean = false,
    val cachesReady: Boolean = false,
    val renderReady: Boolean = false,
    val mediaReady: Boolean = false,
    val error: String? = null
)

/**
 * Initialization status.
 */
enum class InitStatus {
    IDLE,
    INITIALIZING,
    COMPLETE,
    ERROR,
    CANCELLED
}