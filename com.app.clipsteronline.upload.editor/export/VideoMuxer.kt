package com.app.clipsteronline.upload.editor.export

class VideoMuxer {
    fun configure() = Unit

    fun mux(videoPath: String, audioPath: String?, outputPath: String): MuxResult {
        if (videoPath.isBlank() || outputPath.isBlank()) return MuxResult(false, "invalid-path", 0L)
        val trackCount = if (audioPath.isNullOrBlank()) 1 else 2
        val estimatedSize = (videoPath.length + (audioPath?.length ?: 0) + outputPath.length).toLong() * 1024L
        return MuxResult(true, null, estimatedSize, trackCount)
    }

    data class MuxResult(
        val success: Boolean,
        val error: String?,
        val outputBytes: Long,
        val trackCount: Int = 0,
    )
}
