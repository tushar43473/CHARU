package com.app.clipsteronline.upload.editor.core.manager

import com.app.clipsteronline.upload.editor.core.model.Project
import java.util.concurrent.ConcurrentHashMap

class ProjectManager {
    private val projects = ConcurrentHashMap<String, Project>()

    fun upsert(project: Project) {
        require(project.id.isNotBlank())
        projects[project.id] = project
    }

    fun get(projectId: String): Project? = projects[projectId]

    fun all(): List<Project> = projects.values.sortedByDescending { it.updatedAtEpochMs }

    fun delete(projectId: String): Boolean = projects.remove(projectId) != null
}
