package com.app.clipsteronline.upload.editor.project

import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Collections

/**
 * Draft manager.
 * Manage drafts, recent projects, cleanup.
 */
class DraftManager(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) {
    private val _draftState = MutableStateFlow(DraftState())
    val draftState: StateFlow<DraftState> = _draftState.asStateFlow()

    private val draftsDir: File get() = File(context.filesDir, "drafts")
    private val recentProjectsFile: File get() = File(context.filesDir, "recent_projects.json")

    /**
     * Initialize drafts folder.
     */
    fun initialize() {
        if (!draftsDir.exists()) {
            draftsDir.mkdirs()
        }
    }

    /**
     * Create new draft.
     */
    fun createDraft(name: String): File {
        val draftFolder = File(draftsDir, "${System.currentTimeMillis()}_$name")
        draftFolder.mkdirs()
        return draftFolder
    }

    /**
     * Get draft folder.
     */
    fun getDraftFolder(draftId: String): File = File(draftsDir, draftId)

    /**
     * List all drafts.
     */
    suspend fun listDrafts(): List<DraftInfo> = withContext(Dispatchers.IO) {
        val drafts = mutableListOf<DraftInfo>()

        draftsDir.listFiles()?.filter { it.isDirectory }?.forEach { dir ->
            val projectFile = File(dir, "project.json")
            if (projectFile.exists()) {
                val metaFile = File(dir, "metadata.json")
                val metadata = if (metaFile.exists()) {
                    try {
                        val json = org.json.JSONObject(metaFile.readText())
                        DraftMeta(
                            savedAt = json.optLong("savedAt"),
                            durationMs = json.optLong("duration")
                        )
                    } catch (e: Exception) { null }
                } else null

                drafts.add(DraftInfo(
                    id = dir.name,
                    name = dir.name.substringAfter("_"),
                    createdAt = dir.lastModified(),
                    modifiedAt = metadata?.savedAt ?: dir.lastModified(),
                    thumbnailPath = File(dir, "thumbnail.jpg").takeIf { it.exists() }?.absolutePath,
                    metadata = metadata
                ))
            }
        }

        drafts.sortByDescending { it.modifiedAt }
        _draftState.value = _draftState.value.copy(drafts = drafts)

        drafts
    }

    /**
     * Delete draft.
     */
    suspend fun deleteDraft(draftId: String): Boolean = withContext(Dispatchers.IO) {
        val draftFolder = File(draftsDir, draftId)
        draftFolder.deleteRecursively()
    }

    /**
     * Add to recent.
     */
    suspend fun addToRecent(draftId: String, name: String, path: String) = withContext(Dispatchers.IO) {
        val recent = getRecentProjects().toMutableList()
        
        recent.removeAll { it.id == draftId }
        recent.add(0, RecentProject(draftId, name, path, System.currentTimeMillis()))
        
        // Keep only last 20
        while (recent.size > 20) {
            recent.removeLast()
        }
        
        saveRecentProjects(recent)
    }

    /**
     * Get recent projects.
     */
    fun getRecentProjects(): List<RecentProject> {
        return try {
            if (recentProjectsFile.exists()) {
                val json = org.json.JSONArray(recentProjectsFile.readText())
                (0 until json.length()).map { i ->
                    val obj = json.getJSONObject(i)
                    RecentProject(
                        obj.getString("id"),
                        obj.getString("name"),
                        obj.getString("path"),
                        obj.getLong("openedAt")
                    )
                }
            } else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Save recent projects list.
     */
    private fun saveRecentProjects(projects: List<RecentProject>) {
        val json = org.json.JSONArray()
        for (project in projects) {
            json.put(org.json.JSONObject().apply {
                put("id", project.id)
                put("name", project.name)
                put("path", project.path)
                put("openedAt", project.openedAt)
            })
        }
        recentProjectsFile.writeText(json.toString())
    }

    /**
     * Clean old drafts.
     */
    suspend fun cleanupOldDrafts(maxAgeDays: Int = 30) = withContext(Dispatchers.IO) {
        val maxAge = maxAgeDays * 24 * 60 * 60 * 1000L
        val now = System.currentTimeMillis()

        listDrafts().forEach { draft ->
            if (now - draft.modifiedAt > maxAge) {
                deleteDraft(draft.id)
            }
        }
    }

    /**
     * Get draft by ID.
     */
    fun getDraftById(id: String): DraftInfo? {
        return _draftState.value.drafts.find { it.id == id }
    }
}

/**
 * Draft state.
 */
data class DraftState(
    val drafts: List<DraftInfo> = emptyList(),
    val currentDraftId: String? = null
)

/**
 * Draft info.
 */
data class DraftInfo(
    val id: String,
    val name: String,
    val createdAt: Long,
    val modifiedAt: Long,
    val thumbnailPath: String?,
    val metadata: DraftMeta?
)

/**
 * Draft metadata.
 */
data class DraftMeta(
    val savedAt: Long,
    val durationMs: Long
)

/**
 * Recent project.
 */
data class RecentProject(
    val id: String,
    val name: String,
    val path: String,
    val openedAt: Long
)