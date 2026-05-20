package com.app.clipsteronline.upload.editor.project

import com.app.clipsteronline.upload.editor.core.model.Project
import com.app.clipsteronline.upload.editor.core.model.TimelineTrack
import com.app.clipsteronline.upload.editor.database.EditorDatabase

class ProjectLoader(
    private val database: EditorDatabase,
    private val serializer: ProjectSerializer = ProjectSerializer(),
) {
    fun configure() = Unit

    fun load(projectId: String, tracks: List<TimelineTrack> = emptyList()): LoadResult {
        val entity = database.projectDao.get(projectId) ?: return LoadResult(null, "not-found")
        val project = serializer.deserialize(entity.serializedProject, tracks)
        return if (project != null) LoadResult(project, null) else LoadResult(null, "deserialize-failed")
    }

    data class LoadResult(
        val project: Project?,
        val error: String?,
    )
}
