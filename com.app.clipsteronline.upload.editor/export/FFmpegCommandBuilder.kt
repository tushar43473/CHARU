package com.app.clipsteronline.upload.editor.export

class FFmpegCommandBuilder {
    fun configure() = Unit

    fun build(session: ExportSession, inputVideoPath: String, inputAudioPath: String?): String {
        val codec = when (session.preset.codec) {
            ExportSession.ExportPreset.Codec.H264 -> "libx264"
            ExportSession.ExportPreset.Codec.H265 -> "libx265"
        }
        val audioInput = inputAudioPath?.let { " -i \"$it\" " } ?: ""
        val mapAudio = if (inputAudioPath != null) "-map 0:v:0 -map 1:a:0" else "-map 0:v:0"
        return buildString {
            append("ffmpeg -y -i \"")
            append(inputVideoPath)
            append("\"")
            append(audioInput)
            append(" ")
            append(mapAudio)
            append(" -c:v ")
            append(codec)
            append(" -b:v ${session.preset.videoBitrateKbps}k")
            append(" -r ${session.preset.fps}")
            append(" -vf scale=${session.preset.width}:${session.preset.height}")
            if (inputAudioPath != null) append(" -c:a aac -b:a ${session.preset.audioBitrateKbps}k")
            append(" \"")
            append(session.outputPath)
            append("\"")
        }
    }
}
