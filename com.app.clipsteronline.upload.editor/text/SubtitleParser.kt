package com.app.clipsteronline.upload.editor.text

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.io.BufferedReader
import java.io.StringReader

/**
 * Parses subtitle formats.
 * Supports SRT, VTT, ASS/SSA.
 */
class SubtitleParser(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {

    /**
     * Parse SRT content.
     */
    fun parseSRT(content: String): List<Caption> {
        val captions = mutableListOf<Caption>()
        val blocks = content.trim().split("\n\n")

        for (block in blocks) {
            val lines = block.split("\n")
            if (lines.size < 3) continue

            // Skip index
            val timeLine = lines[1]
            val textLines = lines.drop(2)

            val (startMs, endMs) = parseSRTTime(timeLine) ?: continue
            val text = textLines.joinToString("\n")

            captions.add(Caption(
                text = text,
                startMs = startMs,
                endMs = endMs
            ))
        }

        return captions
    }

    /**
     * Parse WebVTT content.
     */
    fun parseVTT(content: String): List<Caption> {
        // Strip WEBVTT header
        val cleanContent = content.replaceFirst("^WEBVTT\\s*\n?".toRegex(), "")
        return parseSRT(cleanContent)
    }

    /**
     * Parse ASS/SSA content.
     */
    fun parseASS(content: String): List<Caption> {
        val captions = mutableListOf<Caption>()
        val inEvents = false
        val lines = content.lines()

        for (line in lines) {
            if (line.startsWith("[Events]")) {
                inEvents = true
                continue
            }

            if (inEvents && line.startsWith("Dialogue:")) {
                val fields = line.substringAfter("Dialogue:").split(",")
                if (fields.size < 10) continue

                val start = fields[1].trim()
                val end = fields[2].trim()
                val text = fields.subList(9, fields.size).joinToString(",").trim()

                val startMs = parseASSTime(start)
                val endMs = parseASSTime(end)

                captions.add(Caption(
                    text = stripASSFormatting(text),
                    startMs = startMs,
                    endMs = endMs
                ))
            }
        }

        return captions
    }

    /**
     * Parse SRT timestamp.
     */
    private fun parseSRTTime(timeStr: String): Pair<Long, Long>? {
        try {
            val parts = timeStr.split(" --> ")
            if (parts.size != 2) return null

            return parseSRTMilliseconds(parts[0]) to parseSRTMilliseconds(parts[1])
        } catch (e: Exception) {
            return null
        }
    }

    /**
     * Parse SRT milliseconds.
     */
    private fun parseSRTMilliseconds(timeStr: String): Long {
        val parts = timeStr.trim().split(":")
        val hours = parts[0].toLong()
        val minutes = parts[1].toLong()
        val secondsParts = parts[2].split(",")
        val seconds = secondsParts[0].toLong()
        val millis = secondsParts[1].toLong()

        return hours * 3600000 + minutes * 60000 + seconds * 1000 + millis
    }

    /**
     * Parse ASS timestamp.
     */
    private fun parseASSTime(timeStr: String): Long {
        val parts = timeStr.trim().split(":")
        val hours = parts[0].toLong()
        val minutes = parts[1].toLong()
        val secondsParts = parts[2].split("\\.".toRegex())
        val seconds = secondsParts[0].toLong()
        val centis = secondsParts[1].take(2).toLong()

        return hours * 3600000 + minutes * 60000 + seconds * 1000 + centis * 10
    }

    /**
     * Strip ASS formatting tags.
     */
    private fun stripASSFormatting(text: String): String {
        return text
            .replace("\\{[^}]*}".toRegex(), "")
            .replace("\\N".toRegex(), "\n")
            .trim()
    }

    /**
     * Detect format from content.
     */
    fun detectFormat(content: String): SubtitleFormat {
        return when {
            content.startsWith("WEBVTT") -> SubtitleFormat.VTT
            content.contains("[Script Info]") -> SubtitleFormat.ASS
            content.contains("Dialogue:") -> SubtitleFormat.ASS
            else -> SubtitleFormat.SRT
        }
    }

    /**
     * Convert between formats.
     */
    fun convert(content: String, toFormat: SubtitleFormat): String {
        val captions = parseSRT(content)
        return when (toFormat) {
            SubtitleFormat.SRT -> toSRT(captions)
            SubtitleFormat.VTT -> toVTT(captions)
            SubtitleFormat.ASS -> toASS(captions)
        }
    }

    /**
     * Convert to SRT.
     */
    private fun toSRT(captions: List<Caption>): String {
        return captions.mapIndexed { index, caption ->
            "${index + 1}\n${formatSRT(caption.startMs)} --> ${formatSRT(caption.endMs)}\n${caption.text}"
        }.joinToString("\n\n")
    }

    /**
     * Convert to VTT.
     */
    private fun toVTT(captions: List<Caption>): String {
        return "WEBVTT\n\n" + toSRT(captions)
    }

    /**
     * Convert to ASS.
     */
    private fun toASS(captions: List<Caption>): String {
        val sb = StringBuilder()
        sb.appendLine("[Script Info]")
        sb.appendLine("ScriptType: v4.00+")
        sb.appendLine("[Events]")
        sb.appendLine("Format: Layer, Start, End, Style, Name, MarginL, MarginR, MarginV, Effect, Text")

        for (caption in captions) {
            val start = formatASS(caption.startMs)
            val end = formatASS(caption.endMs)
            sb.appendLine("Dialogue: 0,$start,$end,Default,,0,0,0,,${caption.text}")
        }

        return sb.toString()
    }

    /**
     * Format SRT time.
     */
    private fun formatSRT(ms: Long): String {
        val hours = ms / 3600000
        val minutes = (ms % 3600000) / 60000
        val seconds = (ms % 60000) / 1000
        val millis = ms % 1000
        return String.format("%02d:%02d:%02d,%03d", hours, minutes, seconds, millis)
    }

    /**
     * Format ASS time.
     */
    private fun formatASS(ms: Long): String {
        val hours = ms / 3600000
        val minutes = (ms % 3600000) / 60000
        val seconds = (ms % 60000) / 1000
        val centis = (ms % 1000) / 10
        return String.format("%d:%02d:%02d.%02d", hours, minutes, seconds, centis)
    }
}

/**
 * Subtitle format.
 */
enum class SubtitleFormat {
    SRT,
    VTT,
    ASS
}