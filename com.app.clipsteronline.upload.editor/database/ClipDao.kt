package com.app.clipsteronline.upload.editor.database

class ClipDao {
    private val clipsByProject = mutableMapOf<String, MutableList<ClipEntity>>()

    fun initialize() = Unit

    @Synchronized
    fun replaceForProject(projectId: String, clips: List<ClipEntity>) {
        clipsByProject[projectId] = clips.toMutableList()
    }

    @Synchronized
    fun getForProject(projectId: String): List<ClipEntity> = clipsByProject[projectId]?.toList() ?: emptyList()

    data class ClipEntity(
        val id: String,
        val projectId: String,
        val trackId: String,
        val type: String,
        val startMs: Long,
        val endMs: Long,
    )
}
