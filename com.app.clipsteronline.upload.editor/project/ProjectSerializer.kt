package com.app.clipsteronline.upload.editor.project

import com.app.clipsteronline.upload.editor.core.model.Project

class ProjectSerializer {
    fun configure() = Unit

    fun serialize(project: Project): String {
        return listOf(
            esc(project.id),
            esc(project.name),
            project.resolution.width.toString(),
            project.resolution.height.toString(),
            project.fps.toString(),
            project.createdAtEpochMs.toString(),
            project.updatedAtEpochMs.toString(),
            esc(project.metadata.author),
            esc(project.metadata.aspectRatioLabel),
            project.metadata.projectVersion.toString(),
            esc(project.metadata.notes ?: ""),
            esc(project.metadata.tags.joinToString("|")),
        ).joinToString(";")
    }

    fun deserialize(serialized: String, tracks: List<com.app.clipsteronline.upload.editor.core.model.TimelineTrack>): Project? {
        val parts = serialized.split(';')
        if (parts.size < 12) return null
        return runCatching {
            Project(
                id = unesc(parts[0]),
                name = unesc(parts[1]),
                tracks = tracks,
                resolution = Project.Resolution(parts[2].toInt(), parts[3].toInt()),
                fps = parts[4].toInt(),
                metadata = Project.Metadata(
                    projectVersion = parts[9].toInt(),
                    aspectRatioLabel = unesc(parts[8]),
                    author = unesc(parts[7]),
                    notes = unesc(parts[10]).ifBlank { null },
                    tags = unesc(parts[11]).split('|').filter { it.isNotBlank() }.toSet(),
                ),
                createdAtEpochMs = parts[5].toLong(),
                updatedAtEpochMs = parts[6].toLong(),
            )
        }.getOrNull()
    }

    private fun esc(v: String) = v.replace("%", "%25").replace(";", "%3B")
    private fun unesc(v: String) = v.replace("%3B", ";").replace("%25", "%")
}
