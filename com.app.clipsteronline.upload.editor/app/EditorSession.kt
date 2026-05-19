package com.app.clipsteronline.upload.editor.app

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Editor session.
 * Active session management.
 */
class EditorSession(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {
    private val _sessionState = MutableStateFlow(SessionState())
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    private var projectId: Long? = null
    private var projectUri: Uri? = null

    /**
     * Start new project.
     */
    fun startNewProject(name: String) {
        projectId = null
        projectUri = null

        _sessionState.value = SessionState(
            projectName = name,
            state = SessionStateType.NEW_PROJECT
        )
    }

    /**
     * Open existing project.
     */
    fun openProject(id: Long, uri: Uri) {
        projectId = id
        projectUri = uri

        _sessionState.value = SessionState(
            projectId = id,
            projectUri = uri.toString(),
            state = SessionStateType.OPEN
        )
    }

    /**
     * Close project.
     */
    fun closeProject() {
        projectId = null
        projectUri = null

        _sessionState.value = SessionState(
            state = SessionStateType.CLOSED
        )
    }

    /**
     * Set playing.
     */
    fun setPlaying(playing: Boolean) {
        _sessionState.value = _sessionState.value.copy(isPlaying = playing)
    }

    /**
     * Update playhead.
     */
    fun updatePlayhead(positionMs: Long) {
        _sessionState.value = _sessionState.value.copy(playheadPosition = positionMs)
    }

    /**
     * Save state.
     */
    fun saveState() {
        _sessionState.value = _sessionState.value.copy(
            savedAt = System.currentTimeMillis()
        )
    }

    /**
     * Get project ID.
     */
    fun getProjectId(): Long? = projectId

    /**
     * Get project URI.
     */
    fun getProjectUri(): Uri? = projectUri
}

/**
 * Session state.
 */
data class SessionState(
    val projectId: Long? = null,
    val projectUri: String? = null,
    val projectName: String = "Untitled",
    val playheadPosition: Long = 0L,
    val state: SessionStateType = SessionStateType.IDLE,
    val isPlaying: Boolean = false,
    val savedAt: Long = 0L,
    val isDirty: Boolean = false
)

/**
 * Session type.
 */
enum class SessionStateType {
    IDLE,
    NEW_PROJECT,
    OPEN,
    EDITING,
    EXPORTING,
    CLOSED
}