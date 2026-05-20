package com.app.clipsteronline.upload.editor.export

data class ExportSession(
    val id: String,
    val outputPath: String,
    val preset: ExportPreset,
    val status: Status = Status.PENDING,
    val totalFrames: Int,
    val renderedFrames: Int = 0,
    val startedAtMs: Long = 0L,
    val updatedAtMs: Long = System.currentTimeMillis(),
    val error: String? = null,
) {
    init {
        require(id.isNotBlank())
        require(outputPath.isNotBlank())
        require(totalFrames >= 0)
        require(renderedFrames in 0..totalFrames.coerceAtLeast(0))
    }

    enum class Status { PENDING, RUNNING, MUXING, COMPLETED, FAILED, CANCELED }

    data class ExportPreset(
        val name: String,
        val width: Int,
        val height: Int,
        val videoBitrateKbps: Int,
        val audioBitrateKbps: Int,
        val fps: Int,
        val codec: Codec,
    ) {
        enum class Codec { H264, H265 }

        companion object {
            val P720 = ExportPreset("720p", 1280, 720, 4500, 192, 30, Codec.H264)
            val P1080 = ExportPreset("1080p", 1920, 1080, 8000, 256, 30, Codec.H264)
            val P4K = ExportPreset("4k", 3840, 2160, 18000, 320, 30, Codec.H265)
        }
    }
}
