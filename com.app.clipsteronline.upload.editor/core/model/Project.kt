package com.app.clipsteronline.upload.editor.core.model

data class Project(
    val id: String,
    val name: String,
    val tracks: List<TimelineTrack>,
    val resolution: Resolution,
    val fps: Int,
    val metadata: Metadata,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long,
) {
    init {
        require(id.isNotBlank())
        require(name.isNotBlank())
        require(fps in 1..120)
        require(createdAtEpochMs > 0)
        require(updatedAtEpochMs >= createdAtEpochMs)
        require(tracks.map { it.order }.distinct().size == tracks.size) { "track order must be unique" }
    }

    val durationMs: Long get() = tracks.maxOfOrNull { it.clips.maxOfOrNull { c -> c.endMs } ?: 0L } ?: 0L

    data class Resolution(val width: Int, val height: Int) {
        init { require(width > 0 && height > 0) }
        val aspectRatio: Float get() = width.toFloat() / height.toFloat()
    }

    data class Metadata(
        val projectVersion: Int,
        val aspectRatioLabel: String,
        val author: String,
        val notes: String? = null,
        val tags: Set<String> = emptySet(),
    ) {
        init {
            require(projectVersion >= 1)
            require(aspectRatioLabel.isNotBlank())
            require(author.isNotBlank())
        }
    }
}
