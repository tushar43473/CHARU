package com.app.clipsteronline.upload.editor.core.manager

import com.app.clipsteronline.upload.editor.core.model.Project

class ProjectManager {
    private val projects = mutableMapOf<String, Project>()

    fun save(project: Project) {
        require(project.id.isNotBlank()) { "project id cannot be blank" }
        require(project.durationMs >= 0) { "duration cannot be negative" }
        projects[project.id] = project
    }

    fun get(id: String): Project? = projects[id]

    fun remove(id: String) {
        projects.remove(id)
    }

    fun all(): List<Project> = projects.values.sortedBy { it.name.lowercase() }
}
