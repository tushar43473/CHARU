package com.app.clipsteronline.upload.editor.text

class SubtitleParser {
    fun configure() = Unit

    fun parseSrt(content: String): List<SubtitleCue> {
        if (content.isBlank()) return emptyList()
        return content.trim().split("\n\n")
            .mapNotNull { parseBlock(it) }
    }

    private fun parseBlock(block: String): SubtitleCue? {
        val lines = block.lines().map { it.trim() }.filter { it.isNotBlank() }
        if (lines.size < 2) return null
        val timingLine = lines[1].takeIf { it.contains("-->") } ?: return null
        val (startRaw, endRaw) = timingLine.split("-->").map { it.trim() }.let { it[0] to it[1] }
        val startMs = parseTime(startRaw) ?: return null
        val endMs = parseTime(endRaw) ?: return null
        if (endMs <= startMs) return null
        val text = lines.drop(2).joinToString("\n").ifBlank { return null }
        return SubtitleCue(startMs, endMs, text)
    }

    private fun parseTime(raw: String): Long? {
        val norm = raw.replace(',', ':').split(':')
        if (norm.size != 4) return null
        val h = norm[0].toLongOrNull() ?: return null
        val m = norm[1].toLongOrNull() ?: return null
        val s = norm[2].toLongOrNull() ?: return null
        val ms = norm[3].toLongOrNull() ?: return null
        return (((h * 60 + m) * 60 + s) * 1000) + ms
    }

    data class SubtitleCue(val startMs: Long, val endMs: Long, val text: String)
}
