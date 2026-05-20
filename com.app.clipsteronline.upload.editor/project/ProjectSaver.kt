package com.app.clipsteronline.upload.editor.project

import com.app.clipsteronline.upload.editor.core.model.Project
import com.app.clipsteronline.upload.editor.database.EditorDatabase
import com.app.clipsteronline.upload.editor.database.ProjectDao

class ProjectSaver(
    private val database: EditorDatabase,
    private val serializer: ProjectSerializer = ProjectSerializer(),
) {
    fun configure() = Unit

    fun save(project: Project): SaveResult {
        val serialized = serializer.serialize(project)
        val now = System.currentTimeMillis()
        database.projectDao.upsert(
            ProjectDao.ProjectEntity(
                id = project.id,
                name = project.name,
                serializedProject = serialized,
                createdAtMs = project.createdAtEpochMs,
                updatedAtMs = now,
                lastOpenedAtMs = now,
            ),
        )
        return SaveResult(true, null, now)
    }

    data class SaveResult(val success: Boolean, val error: String?, val savedAtMs: Long)
}
