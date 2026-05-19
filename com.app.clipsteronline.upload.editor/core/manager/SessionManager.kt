package com.app.clipsteronline.upload.editor.core.manager

import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Manager for editor session lifecycle.
 * Handles temporary files, background tasks, and memory cleanup.
 */
class SessionManager(
    private val context: Context
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val mainHandler = Handler(Looper.getMainLooper())

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _sessionState = MutableStateFlow<SessionState>(SessionState.Idle)
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    private val _isInBackground = MutableStateFlow(false)
    val isInBackground: StateFlow<Boolean> = _isInBackground.asStateFlow()

    private val _previousSessionUri = MutableStateFlow<String?>(null)
    val previousSessionUri: StateFlow<String?> = _previousSessionUri.asStateFlow()

    private val tempFiles = CopyOnWriteArrayList<File>()
    private val cleanupCallbacks = CopyOnWriteArrayList<() -> Unit>()

    private var foregroundTask: Runnable? = null
    private var backgroundTask: Runnable? = null

    companion object {
        private const val PREFS_NAME = "editor_session"
        private const val KEY_PREVIOUS_PROJECT_URI = "previous_project_uri"
        private const val KEY_LAST_SESSION_TIME = "last_session_time"
        private const val KEY_UNSAVED_CHANGES = "unsaved_changes"

        private const val FOREGROUND_CHECK_INTERVAL = 1000L
        private const val BACKGROUND_CLEANUP_DELAY = 30000L
    }

    /**
     * Start new session.
     */
    fun startSession() {
        _sessionState.value = SessionState.Active

        loadPreviousSession()
        startForegroundMonitoring()
    }

    /**
     * Resume previous session.
     */
    fun resumeSession(projectUri: String) {
        _previousSessionUri.value = projectUri
        _sessionState.value = SessionState.Resuming
    }

    /**
     * Enter foreground.
     */
    fun onForeground() {
        _isInBackground.value = false
        _sessionState.value = SessionState.Active

        prefs.edit().putLong(KEY_LAST_SESSION_TIME, System.currentTimeMillis()).apply()

        // Resume background tasks
    }

    /**
     * Enter background.
     */
    fun onBackground() {
        _isInBackground.value = true
        _sessionState.value = SessionState.Background

        mainHandler.postDelayed({
            performBackgroundCleanup()
        }, BACKGROUND_CLEANUP_DELAY)
    }

    /**
     * End session.
     */
    fun endSession(saveState: Boolean = true) {
        if (saveState) {
            saveSessionState()
        }

        cleanupTempFiles()
        cleanupCallbacks.forEach { it() }

        _sessionState.value = SessionState.Ended
    }

    /**
     * Save temporary file.
     */
    fun saveTempFile(file: File): File {
        tempFiles.add(file)
        return file
    }

    /**
     * Create temp directory.
     */
    fun createTempDirectory(name: String): File {
        val directory = File(context.cacheDir, "temp_$name")
        directory.mkdirs()
        tempFiles.add(directory)
        return directory
    }

    /**
     * Register cleanup callback.
     */
    fun registerCleanup(callback: () -> Unit) {
        cleanupCallbacks.add(callback)
    }

    /**
     * Unregister cleanup callback.
     */
    fun unregisterCleanup(callback: () -> Unit) {
        cleanupCallbacks.remove(callback)
    }

    /**
     * Save session state.
     */
    private fun saveSessionState() {
        prefs.edit().putLong(KEY_LAST_SESSION_TIME, System.currentTimeMillis()).apply()
    }

    /**
     * Load previous session.
     */
    private fun loadPreviousSession() {
        val previousUri = prefs.getString(KEY_PREVIOUS_PROJECT_URI, null)
        if (previousUri != null) {
            _previousSessionUri.value = previousUri
        }
    }

    /**
     * Save previous project URI.
     */
    fun savePreviousProjectUri(uri: String) {
        prefs.edit().putString(KEY_PREVIOUS_PROJECT_URI, uri).apply()
        _previousSessionUri.value = uri
    }

    /**
     * Mark unsaved changes.
     */
    fun markUnsavedChanges(hasChanges: Boolean) {
        prefs.edit().putBoolean(KEY_UNSAVED_CHANGES, hasChanges).apply()
    }

    /**
     * Check if has unsaved changes.
     */
    fun hasUnsavedChanges(): Boolean {
        return prefs.getBoolean(KEY_UNSAVED_CHANGES, false)
    }

    /**
     * Get last session time.
     */
    fun getLastSessionTime(): Long {
        return prefs.getLong(KEY_LAST_SESSION_TIME, 0L)
    }

    /**
     * Start foreground monitoring.
     */
    private fun startForegroundMonitoring() {
        foregroundTask = object : Runnable {
            override fun run() {
                if (!_isInBackground.value) {
                    // Check if in foreground
                }
                mainHandler.postDelayed(this, FOREGROUND_CHECK_INTERVAL)
            }
        }

        mainHandler.post(foregroundTask!!)
    }

    /**
     * Perform background cleanup.
     */
    private fun performBackgroundCleanup() {
        if (!_isInBackground.value) return

        scope.launch(Dispatchers.IO) {
            // Clean up old temp files
            val cutoffTime = System.currentTimeMillis() - BACKGROUND_CLEANUP_DELAY
            context.cacheDir.listFiles()?.forEach { file ->
                if (file.lastModified() < cutoffTime && file.name.startsWith("temp_")) {
                    file.deleteRecursively()
                }
            }

            // Call cleanup callbacks
           cleanupCallbacks.forEach { it() }
        }
    }

    /**
     * Cleanup temp files.
     */
    private fun cleanupTempFiles() {
        tempFiles.forEach { file ->
            if (file.isDirectory) {
                file.deleteRecursively()
            } else {
                file.delete()
            }
        }
        tempFiles.clear()
    }

    /**
     * Restore previous session.
     */
    fun restorePreviousSession(): String? {
        return _previousSessionUri.value
    }

    /**
     * Clear session state.
     */
    fun clearSessionState() {
        prefs.edit()
            .remove(KEY_PREVIOUS_PROJECT_URI)
            .remove(KEY_LAST_SESSION_TIME)
            .remove(KEY_UNSAVED_CHANGES)
            .apply()

        _previousSessionUri.value = null
    }

    /**
     * Release all resources.
     */
    fun release() {
        foregroundTask?.let { mainHandler.removeCallbacks(it) }
        backgroundTask?.let { mainHandler.removeCallbacks(it) }

        cleanupTempFiles()
        cleanupCallbacks.clear()

        endSession(saveState = false)
    }
}

/**
 * Session state.
 */
sealed class SessionState {
    data object Idle : SessionState()
    data object Active : SessionState()
    data object Background : SessionState()
    data object Resuming : SessionState()
    data object Ended : SessionState()
}