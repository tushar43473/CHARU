package com.app.clipsteronline.upload.editor.project

import com.app.clipsteronline.upload.editor.core.model.Project

class DraftManager {
    private val drafts = mutableMapOf<String, MutableList<Draft>>()

    fun configure() = Unit

    fun createDraft(project: Project, payload: String): Draft {
        val draft = Draft(
            id = "${project.id}_${System.currentTimeMillis()}",
            projectId = project.id,
            version = (drafts[project.id]?.maxOfOrNull { it.version } ?: 0) + 1,
            payload = payload,
        )
        drafts.getOrPut(project.id) { mutableListOf() }.add(draft)
        return draft
    }

    fun latest(projectId: String): Draft? = drafts[projectId]?.maxByOrNull { it.version }
    fun all(projectId: String): List<Draft> = drafts[projectId]?.sortedByDescending { it.version } ?: emptyList()

    data class Draft(
        val id: String,
        val projectId: String,
        val version: Int,
        val payload: String,
        val createdAtMs: Long = System.currentTimeMillis(),
    )
}
