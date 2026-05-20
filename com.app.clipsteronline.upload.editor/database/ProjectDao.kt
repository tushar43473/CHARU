package com.app.clipsteronline.upload.editor.database

class ProjectDao {
    private val store = linkedMapOf<String, ProjectEntity>()

    fun initialize() = Unit

    @Synchronized
    fun upsert(entity: ProjectEntity) {
        store[entity.id] = entity.copy(updatedAtMs = System.currentTimeMillis())
    }

    @Synchronized
    fun get(id: String): ProjectEntity? = store[id]

    @Synchronized
    fun list(): List<ProjectEntity> = store.values.sortedByDescending { it.updatedAtMs }

    @Synchronized
    fun delete(id: String) {
        store.remove(id)
    }

    data class ProjectEntity(
        val id: String,
        val name: String,
        val serializedProject: String,
        val createdAtMs: Long,
        val updatedAtMs: Long,
        val lastOpenedAtMs: Long,
    )
}
