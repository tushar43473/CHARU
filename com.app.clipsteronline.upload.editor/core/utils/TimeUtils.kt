package com.app.clipsteronline.upload.editor.core.utils

import kotlin.math.roundToLong

object TimeUtils {
    fun msToFrames(milliseconds: Long, fps: Int): Long {
        require(milliseconds >= 0)
        require(fps > 0)
        return ((milliseconds / 1000.0) * fps).roundToLong()
    }

    fun framesToMs(frames: Long, fps: Int): Long {
        require(frames >= 0)
        require(fps > 0)
        return ((frames * 1000.0) / fps).roundToLong()
    }

    fun formatDuration(milliseconds: Long): String {
        val safeMs = milliseconds.coerceAtLeast(0)
        val totalSeconds = safeMs / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) "%d:%02d:%02d".format(hours, minutes, seconds) else "%02d:%02d".format(minutes, seconds)
    }

    fun formatTimeline(milliseconds: Long): String {
        val safeMs = milliseconds.coerceAtLeast(0)
        val minutes = safeMs / 60_000
        val seconds = (safeMs % 60_000) / 1000
        val millis = safeMs % 1000
        return "%02d:%02d.%03d".format(minutes, seconds, millis)
    }

    fun toSmpteTimecode(milliseconds: Long, fps: Int): String {
        require(fps > 0)
        val safeMs = milliseconds.coerceAtLeast(0)
        val hours = safeMs / 3_600_000
        val minutes = (safeMs % 3_600_000) / 60_000
        val seconds = (safeMs % 60_000) / 1000
        val frame = msToFrames(safeMs % 1000, fps)
        return "%02d:%02d:%02d:%02d".format(hours, minutes, seconds, frame)
    }
}
