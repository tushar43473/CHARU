package com.app.clipsteronline.upload.editor.export

class ExportProgressTracker {
    fun configure() = Unit

    fun progress(session: ExportSession): ProgressSnapshot {
        val progress = if (session.totalFrames <= 0) 0f else (session.renderedFrames.toFloat() / session.totalFrames).coerceIn(0f, 1f)
        val elapsed = (System.currentTimeMillis() - session.startedAtMs).coerceAtLeast(0L)
        val remainingMs = if (progress <= 0f) Long.MAX_VALUE else ((elapsed / progress) - elapsed).toLong().coerceAtLeast(0L)
        return ProgressSnapshot(progress, elapsed, remainingMs, session.status)
    }

    data class ProgressSnapshot(
        val progress: Float,
        val elapsedMs: Long,
        val remainingMs: Long,
        val status: ExportSession.Status,
    )
}
