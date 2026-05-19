package com.app.clipsteronline.upload.editor.core.utils

/**
 * Time and duration utilities for timeline operations.
 * Provides time conversions, formatting, and snapping calculations.
 */
object TimeUtils {

    private const val MILLIS_PER_SECOND = 1000L
    private const val MILLIS_PER_MINUTE = 60_000L
    private const val MILLIS_PER_HOUR = 3_600_000L

    private const val FRAME_RATE_DEFAULT = 30

    /**
     * Format milliseconds to timeline display format (00:00:00.000).
     */
    fun formatTimeline(timeMs: Long): String {
        val hours = timeMs / MILLIS_PER_HOUR
        val minutes = (timeMs % MILLIS_PER_HOUR) / MILLIS_PER_MINUTE
        val seconds = (timeMs % MILLIS_PER_MINUTE) / MILLIS_PER_SECOND
        val millis = timeMs % MILLIS_PER_SECOND

        return if (hours > 0) {
            String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, millis)
        } else {
            String.format("%02d:%02d.%03d", minutes, seconds, millis)
        }
    }

    /**
     * Format milliseconds to short format (00:00).
     */
    fun formatShort(timeMs: Long): String {
        val minutes = timeMs / MILLIS_PER_MINUTE
        val seconds = (timeMs % MILLIS_PER_MINUTE) / MILLIS_PER_SECOND

        return String.format("%02d:%02d", minutes, seconds)
    }

    /**
     * Format milliseconds to compact format (1:30).
     */
    fun formatCompact(timeMs: Long): String {
        val totalSeconds = timeMs / MILLIS_PER_SECOND
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60

        return if (minutes >= 60) {
            val hours = minutes / 60
            val remainingMinutes = minutes % 60
            String.format("%d:%02d:%02d", hours, remainingMinutes, seconds)
        } else {
            String.format("%d:%02d", minutes, seconds)
        }
    }

    /**
     * Convert frames to milliseconds.
     */
    fun framesToMs(frames: Int, frameRate: Int = FRAME_RATE_DEFAULT): Long {
        return (frames * MILLIS_PER_SECOND / frameRate).toLong()
    }

    /**
     * Convert milliseconds to frames.
     */
    fun msToFrames(timeMs: Long, frameRate: Int = FRAME_RATE_DEFAULT): Int {
        return (timeMs * frameRate / MILLIS_PER_SECOND).toInt()
    }

    /**
     * Round time to nearest frame.
     */
    fun roundToFrame(timeMs: Long, frameRate: Int = FRAME_RATE_DEFAULT): Long {
        val frameDuration = MILLIS_PER_SECOND / frameRate
        return (timeMs / frameDuration) * frameDuration
    }

    /**
     * Snap time to nearest grid point.
     */
    fun snapToGrid(timeMs: Long, gridSizeMs: Long, threshold: Long = 50L): Long {
        val remainder = timeMs % gridSizeMs
        return when {
            remainder < threshold -> timeMs - remainder
            remainder > gridSizeMs - threshold -> timeMs + (gridSizeMs - remainder)
            else -> timeMs
        }
    }

    /**
     * Snap time to nearest snap points.
     */
    fun snapToPoints(timeMs: Long, snapPoints: List<Long>, threshold: Long = 50L): Long {
        var closestPoint = timeMs
        var closestDistance = threshold

        for (point in snapPoints) {
            val distance = kotlin.math.abs(timeMs - point)
            if (distance < closestDistance) {
                closestDistance = distance
                closestPoint = point
            }
        }

        return closestPoint
    }

    /**
     * Calculate duration between two times.
     */
    fun duration(startMs: Long, endMs: Long): Long {
        return (endMs - startMs).coerceAtLeast(0L)
    }

    /**
     * Parse time string to milliseconds.
     */
    fun parseToMs(timeString: String): Long? {
        return try {
            val parts = timeString.split(":")
            when (parts.size) {
                1 -> parts[0].toLong()
                2 -> {
                    val minutes = parts[0].toLong()
                    val secondsWithMillis = parts[1]
                    val secondsPart = secondsWithMillis.substringBefore(".")
                    val millisPart = secondsWithMillis.substringAfter(".", "0")
                    minutes * MILLIS_PER_MINUTE +
                        secondsPart.toLong() * MILLIS_PER_SECOND +
                        millisPart.padEnd(3, '0').take(3).toLong()
                }
                3 -> {
                    val hours = parts[0].toLong()
                    val minutes = parts[1].toLong()
                    val secondsWithMillis = parts[2]
                    val secondsPart = secondsWithMillis.substringBefore(".")
                    val millisPart = secondsWithMillis.substringAfter(".", "0")
                    hours * MILLIS_PER_HOUR +
                        minutes * MILLIS_PER_MINUTE +
                        secondsPart.toLong() * MILLIS_PER_SECOND +
                        millisPart.padEnd(3, '0').take(3).toLong()
                }
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Format duration for display with appropriate precision.
     */
    fun formatDuration(timeMs: Long, precision: Int = 0): String {
        return when {
            timeMs < 1000 -> "${timeMs}ms"
            timeMs < MILLIS_PER_MINUTE -> {
                val seconds = timeMs / 1000.0
                String.format("%.${precision}fs", seconds)
            }
            timeMs < MILLIS_PER_HOUR -> formatShort(timeMs)
            else -> formatTimeline(timeMs)
        }
    }

    /**
     * Get frame rate from typical values.
     */
    fun getFrameRateValues(): List<Int> = listOf(23, 24, 25, 29, 30, 50, 59, 60)

    /**
     * Calculate keyframe interval based on frame rate and desired interval.
     */
    fun calculateKeyframeInterval(frameRate: Int, seconds: Float): Int {
        return (frameRate * seconds).toInt().coerceAtLeast(1)
    }
}