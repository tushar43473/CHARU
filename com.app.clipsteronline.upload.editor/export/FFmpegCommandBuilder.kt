package com.app.clipsteronline.upload.editor.export

import com.app.clipsteronline.upload.editor.core.model.ExportConfig

class FFmpegCommandBuilder {
    fun build(inputPath: String, outputPath: String, config: ExportConfig): List<String> {
        require(inputPath.isNotBlank())
        require(outputPath.isNotBlank())
        require(config.width > 0 && config.height > 0)
        require(config.fps in 1..240)
        require(config.bitrate > 100_000)

        return listOf(
            "ffmpeg", "-y",
            "-i", inputPath,
            "-r", config.fps.toString(),
            "-s", "${config.width}x${config.height}",
            "-b:v", config.bitrate.toString(),
            "-c:v", "libx264",
            "-preset", "medium",
            "-pix_fmt", "yuv420p",
            outputPath,
        )
    }
}
