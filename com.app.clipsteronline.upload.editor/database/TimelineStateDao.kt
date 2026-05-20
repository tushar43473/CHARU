package com.app.clipsteronline.upload.editor.database

class TimelineStateDao {
    private val statesByProject = mutableMapOf<String, TimelineStateEntity>()

    fun initialize() = Unit

    @Synchronized
    fun upsert(entity: TimelineStateEntity) {
        statesByProject[entity.projectId] = entity
    }

    @Synchronized
    fun get(projectId: String): TimelineStateEntity? = statesByProject[projectId]

    data class TimelineStateEntity(
        val projectId: String,
        val playheadMs: Long,
        val zoom: Float,
        val scrollPx: Float,
        val updatedAtMs: Long = System.currentTimeMillis(),
    )
}
