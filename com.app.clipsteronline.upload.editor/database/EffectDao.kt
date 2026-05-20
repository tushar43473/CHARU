package com.app.clipsteronline.upload.editor.database

class EffectDao {
    private val effectsByProject = mutableMapOf<String, MutableList<EffectEntity>>()

    fun initialize() = Unit

    @Synchronized
    fun setForProject(projectId: String, effects: List<EffectEntity>) {
        effectsByProject[projectId] = effects.toMutableList()
    }

    @Synchronized
    fun getForProject(projectId: String): List<EffectEntity> = effectsByProject[projectId]?.toList() ?: emptyList()

    data class EffectEntity(
        val id: String,
        val projectId: String,
        val name: String,
        val startMs: Long,
        val endMs: Long,
    )
}
