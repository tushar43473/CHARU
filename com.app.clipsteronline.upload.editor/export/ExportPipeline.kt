package com.app.clipsteronline.upload.editor.export

import com.app.clipsteronline.upload.editor.render.RenderComposition

class ExportPipeline(
    private val commandBuilder: FFmpegCommandBuilder = FFmpegCommandBuilder(),
    private val muxer: VideoMuxer = VideoMuxer(),
) {
    fun configure() = Unit

    fun run(
        session: ExportSession,
        composition: RenderComposition,
        inputVideoPath: String,
        inputAudioPath: String?,
    ): PipelineResult {
        val frameCount = composition.exportableFrameCount()
        if (frameCount == 0) return PipelineResult(false, "no-visible-layers", null, null)

        val command = commandBuilder.build(session, inputVideoPath, inputAudioPath)
        val mux = muxer.mux(inputVideoPath, inputAudioPath, session.outputPath)
        return PipelineResult(mux.success, mux.error, command, mux)
    }

    data class PipelineResult(
        val success: Boolean,
        val error: String?,
        val ffmpegCommand: String?,
        val muxResult: VideoMuxer.MuxResult?,
    )
}
