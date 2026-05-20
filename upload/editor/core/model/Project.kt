package upload.editor.core.model

data class Project(
    val id: String,
    val name: String,
    val tracks: List<TimelineTrack>,
    val aspectRatio: AspectRatio,
    val fps: Int,
    val resolution: Resolution,
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

    val totalDurationMs: Long
        get() = tracks.maxOfOrNull { it.endMs } ?: 0L

    data class AspectRatio(val width: Int, val height: Int) {
        init {
            require(width > 0 && height > 0)
        }

        val ratio: Float get() = width.toFloat() / height.toFloat()
    }

    data class Resolution(val width: Int, val height: Int) {
        init {
            require(width > 0 && height > 0)
        }

        val pixels: Long get() = width.toLong() * height.toLong()
    }

    data class Metadata(
        val author: String,
        val appVersion: String,
        val deviceModel: String,
        val tags: Set<String> = emptySet(),
        val notes: String? = null,
    ) {
        init {
            require(author.isNotBlank())
            require(appVersion.isNotBlank())
            require(deviceModel.isNotBlank())
        }
    }
}
