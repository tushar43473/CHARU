package com.app.clipsteronline.upload.editor.export

import com.app.clipsteronline.upload.editor.render.RenderComposition

class ExportEngine(
    private val pipeline: ExportPipeline = ExportPipeline(),
    private val progressTracker: ExportProgressTracker = ExportProgressTracker(),
) {
    fun configure() = Unit

    fun start(
        session: ExportSession,
        composition: RenderComposition,
        inputVideoPath: String,
        inputAudioPath: String?,
    ): ExportResult {
        val runningSession = session.copy(status = ExportSession.Status.RUNNING, startedAtMs = System.currentTimeMillis(), updatedAtMs = System.currentTimeMillis())
        val pipelineResult = pipeline.run(runningSession, composition, inputVideoPath, inputAudioPath)
        val finalStatus = if (pipelineResult.success) ExportSession.Status.COMPLETED else ExportSession.Status.FAILED
        val finalSession = runningSession.copy(
            status = finalStatus,
            renderedFrames = runningSession.totalFrames,
            updatedAtMs = System.currentTimeMillis(),
            error = pipelineResult.error,
        )
        val progress = progressTracker.progress(finalSession)
        return ExportResult(finalSession, progress, pipelineResult)
    }

    data class ExportResult(
        val session: ExportSession,
        val progress: ExportProgressTracker.ProgressSnapshot,
        val pipeline: ExportPipeline.PipelineResult,
    )
}
