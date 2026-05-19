package com.app.clipsteronline.upload.editor.core.manager

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.app.clipsteronline.upload.editor.core.model.Project
import com.app.clipsteronline.upload.editor.core.model.Timeline
import com.app.clipsteronline.upload.editor.core.model.TimelineTrack
import java.io.File

/**
 * Manager for editor projects.
 * Handles creating, opening, saving, and managing project state.
 */
class ProjectManager(
    private val context: Context
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _activeProject = MutableStateFlow<Project?>(null)
    val activeProject: StateFlow<Project?> = _activeProject.asStateFlow()

    private val _projectState = MutableStateFlow(ProjectState.IDLE)
    val projectState: StateFlow<ProjectState> = _projectState.asStateFlow()

    private val undoStack = mutableListOf<Project>()
    private val redoStack = mutableListOf<Project>()
    private val maxUndoSize = 50

    private var autoSaveEnabled = true
    private var lastAutoSaveTime = 0L

    init {
        setupAutoSave()
    }

    /**
     * Create new project.
     */
    fun createProject(name: String = "Untitled Project"): Project {
        val project = Project(
            id = generateProjectId(),
            name = name,
            timeline = Timeline(),
            metadata = upload.editor.core.model.ProjectMetadata()
        )

        _activeProject.value = project
        _projectState.value = ProjectState.READY
        return project
    }

    /**
     * Open project from file.
     */
    suspend fun openProject(file: File): Project? {
        _projectState.value = ProjectState.LOADING

        return withContext(Dispatchers.IO) {
            try {
                val project = deserializeProject(file)
                project?.let {
                    _activeProject.value = it
                    _projectState.value = ProjectState.READY
                    lastAutoSaveTime = System.currentTimeMillis()
                }
                project
            } catch (e: Exception) {
                _projectState.value = ProjectState.ERROR
                null
            }
        }
    }

    /**
     * Open project from URI.
     */
    suspend fun openProject(uri: Uri): Project? {
        val file = File(uri.path ?: return null)
        return openProject(file)
    }

    /**
     * Save current project.
     */
    suspend fun saveProject(saveUri: Uri? = null): Boolean {
        val project = _activeProject.value ?: return false
        _projectState.value = ProjectState.SAVING

        return withContext(Dispatchers.IO) {
            try {
                serializeProject(project, saveUri?.let { File(it.path!!) })
                _projectState.value = ProjectState.READY
                lastAutoSaveTime = System.currentTimeMillis()
                true
            } catch (e: Exception) {
                _projectState.value = ProjectState.ERROR
                false
            }
        }
    }

    /**
     * Save project to file.
     */
    suspend fun saveProjectToFile(file: File): Boolean {
        val project = _activeProject.value ?: return false
        _projectState.value = ProjectState.SAVING

        return withContext(Dispatchers.IO) {
            try {
                serializeProject(project, file)
                _projectState.value = ProjectState.READY
                true
            } catch (e: Exception) {
                _projectState.value = ProjectState.ERROR
                false
            }
        }
    }

    /**
     * Close current project.
     */
    fun closeProject() {
        pushUndoState()
        _activeProject.value = null
        _projectState.value = ProjectState.IDLE
        undoStack.clear()
        redoStack.clear()
    }

    /**
     * Add track to timeline.
     */
    fun addTrack(track: TimelineTrack) {
        val project = _activeProject.value ?: return

        pushUndoState()
        val currentTracks = project.timeline.tracks.toMutableList()
        currentTracks.add(track)
        val updatedTimeline = project.timeline.copy(tracks = currentTracks)

        _activeProject.value = project.copy(
            timeline = updatedTimeline,
            metadata = project.metadata.withUpdate()
        )
    }

    /**
     * Remove track from timeline.
     */
    fun removeTrack(trackId: String) {
        val project = _activeProject.value ?: return

        pushUndoState()
        val currentTracks = project.timeline.tracks.filter { it.id != trackId }
        val updatedTimeline = project.timeline.copy(tracks = currentTracks)

        _activeProject.value = project.copy(
            timeline = updatedTimeline,
            metadata = project.metadata.withUpdate()
        )
    }

    /**
     * Update track.
     */
    fun updateTrack(trackId: String, update: (TimelineTrack) -> TimelineTrack) {
        val project = _activeProject.value ?: return

        pushUndoState()
        val currentTracks = project.timeline.tracks.map {
            if (it.id == trackId) update(it) else it
        }
        val updatedTimeline = project.timeline.copy(tracks = currentTracks)

        _activeProject.value = project.copy(
            timeline = updatedTimeline,
            metadata = project.metadata.withUpdate()
        )
    }

    /**
     * Get track by ID.
     */
    fun getTrack(trackId: String): TimelineTrack? {
        return _activeProject.value?.timeline?.getTrackById(trackId)
    }

    /**
     * Undo last action.
     */
    fun undo(): Boolean {
        if (undoStack.isEmpty()) return false

        val currentState = _activeProject.value ?: return false
        redoStack.add(currentState)

        val previousState = undoStack.removeLast()
        _activeProject.value = previousState

        return true
    }

    /**
     * Redo last undone action.
     */
    fun redo(): Boolean {
        if (redoStack.isEmpty()) return false

        val currentState = _activeProject.value ?: return false
        undoStack.add(currentState)

        val nextState = redoStack.removeLast()
        _activeProject.value = nextState

        return true
    }

    /**
     * Set autosave enabled.
     */
    fun setAutoSaveEnabled(enabled: Boolean) {
        autoSaveEnabled = enabled
    }

    /**
     * Get project name.
     */
    fun getProjectName(): String {
        return _activeProject.value?.name ?: "No Project"
    }

    /**
     * Check if project has unsaved changes.
     */
    fun hasUnsavedChanges(): Boolean {
        return lastAutoSaveTime == 0L ||
            _activeProject.value?.metadata?.updatedAt != null &&
            _activeProject.value?.metadata?.updatedAt!! > lastAutoSaveTime
    }

    /**
     * Update timeline.
     */
    fun updateTimeline(timeline: Timeline) {
        val project = _activeProject.value ?: return
        _activeProject.value = project.copy(
            timeline = timeline,
            metadata = project.metadata.withUpdate()
        )
    }

    /**
     * Project state.
     */
    private fun setupAutoSave() {
        if (!autoSaveEnabled) return

        scope.launch {
            while (true) {
                kotlinx.coroutines.delay(30000) // 30 seconds
                if (autoSaveEnabled && hasUnsavedChanges()) {
                    // Auto-save logic would go here
                }
            }
        }
    }

    /**
     * Generate unique project ID.
     */
    private fun generateProjectId(): String {
        return "project_${System.currentTimeMillis()}"
    }

    /**
     * Serialize project to file.
     */
    private suspend fun serializeProject(project: Project, file: File?) {
        // Serialize project to JSON (simplified)
        withContext(Dispatchers.IO) {
            // Project serialization would use JSON/GSON
        }
    }

    /**
     * Deserialize project from file.
     */
    private suspend fun deserializeProject(file: File): Project? {
        return withContext(Dispatchers.IO) {
            // Project deserialization
            null
        }
    }

    /**
     * Push current state to undo stack.
     */
    private fun pushUndoState() {
        val current = _activeProject.value ?: return
        undoStack.add(current)

        while (undoStack.size > maxUndoSize) {
            undoStack.removeAt(0)
        }

        redoStack.clear()
    }
}

/**
 * Project state.
 */
sealed class ProjectState {
    data object IDLE : ProjectState()
    data object LOADING : ProjectState()
    data object READY : ProjectState()
    data object SAVING : ProjectState()
    data class ERROR(val message: String) : ProjectState()
}