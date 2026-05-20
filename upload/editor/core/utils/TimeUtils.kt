package upload.editor.core.utils

import kotlin.math.abs
import kotlin.math.roundToLong

object TimeUtils {
    fun formatTimeline(ms: Long): String {
        val total = ms.coerceAtLeast(0)
        val hours = total / 3_600_000
        val minutes = (total % 3_600_000) / 60_000
        val seconds = (total % 60_000) / 1_000
        val millis = total % 1_000
        return if (hours > 0) "%02d:%02d:%02d.%03d".format(hours, minutes, seconds, millis)
        else "%02d:%02d.%03d".format(minutes, seconds, millis)
    }

    fun framesToMs(frame: Long, fps: Int): Long {
        require(frame >= 0)
        require(fps > 0)
        return ((frame * 1000.0) / fps).roundToLong()
    }

    fun msToFrames(ms: Long, fps: Int): Long {
        require(ms >= 0)
        require(fps > 0)
        return ((ms / 1000.0) * fps).roundToLong()
    }

    fun formatDuration(ms: Long): String {
        val totalSec = ms.coerceAtLeast(0) / 1000
        val h = totalSec / 3600
        val m = (totalSec % 3600) / 60
        val s = totalSec % 60
        return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
    }

    fun snapToInterval(valueMs: Long, intervalMs: Long, thresholdMs: Long): Long {
        require(intervalMs > 0)
        require(thresholdMs >= 0)
        val remainder = valueMs % intervalMs
        val down = valueMs - remainder
        val up = down + intervalMs
        return when {
            abs(valueMs - down) <= thresholdMs -> down
            abs(up - valueMs) <= thresholdMs -> up
            else -> valueMs
        }
    }

    fun formatTimecode(ms: Long, fps: Int): String {
        require(fps > 0)
        val clamped = ms.coerceAtLeast(0)
        val hours = clamped / 3_600_000
        val minutes = (clamped % 3_600_000) / 60_000
        val seconds = (clamped % 60_000) / 1_000
        val frame = msToFrames(clamped % 1_000, fps)
        return "%02d:%02d:%02d:%02d".format(hours, minutes, seconds, frame)
    }
}
