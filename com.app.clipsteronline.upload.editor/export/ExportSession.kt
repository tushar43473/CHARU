package com.app.clipsteronline.upload.editor.export

import android.net.Uri

/**
 * Export session model.
 * Contains state, progress, metadata, and configuration.
 */
data class ExportSession(
    val id: String = java.util.UUID.randomUUID().toString(),
    val config: ExportConfig,
    val outputUri: Uri,
    val startTime: Long = 0L,
    val endTime: Long = 0L,
    val status: ExportStatus = ExportStatus.IDLE,
    val progress: Float = 0f,
    val currentFrame: Int = 0,
    val totalFrames: Int = 0,
    val elapsedTimeMs: Long = 0L,
    val estimatedRemainingMs: Long = 0L,
    val outputFileSize: Long = 0L,
    val errorMessage: String? = null
) {
    /**
     * Get duration in milliseconds.
     */
    fun getDuration(): Long = config.duration

    /**
     * Is active.
     */
    fun isActive(): Boolean = status == ExportStatus.EXPORTING || status == ExportStatus.PAUSED

    /**
     * Is completed.
     */
    fun isCompleted(): Boolean = status == ExportStatus.COMPLETED

    /**
     * Is failed.
     */
    fun isFailed(): Boolean = status == ExportStatus.FAILED

    /**
     * Is cancelled.
     */
    fun isCancelled(): Boolean = status == ExportStatus.CANCELLED

    /**
     * Get progress percentage.
     */
    fun getProgressPercent(): Int = (progress * 100).toInt()

    /**
     * Copy with progress.
     */
    fun withProgress(progress: Float): ExportSession = copy(progress = progress)

    /**
     * Copy with status.
     */
    fun withStatus(status: ExportStatus): ExportSession = copy(status = status)

    /**
     * Copy with current frame.
     */
    fun withCurrentFrame(frame: Int): ExportSession = copy(currentFrame = frame)

    /**
     * Copy with error.
     */
    fun withError(message: String): ExportSession = copy(
        status = ExportStatus.FAILED,
        errorMessage = message
    )

    /**
     * Copy with output size.
     */
    fun withOutputSize(size: Long): ExportSession = copy(outputFileSize = size)
}

/**
 * Export configuration.
 */
data class ExportConfig(
    val quality: ExportQuality = ExportQuality.MEDIUM,
    val format: ExportFormat = ExportFormat.MP4,
    val resolution: Pair<Int, Int> = 1920 to 1080,
    val frameRate: Int = 30,
    val duration: Long = 0L,
    val videoBitrate: String = "8M",
    val audioBitrate: String = "192k",
    val useHardwareAcceleration: Boolean = false,
    val includeAudio: Boolean = true,
    val includeVideo: Boolean = true,
    val includeSubtitles: Boolean = false,
    val metadata: Map<String, String> = emptyMap()
) {
    /**
     * Get width.
     */
    fun getWidth(): Int = resolution.first

    /**
     * Get height.
     */
    fun getHeight(): Int = resolution.second
}