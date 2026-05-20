package com.app.clipsteronline.upload.editor.text

import com.app.clipsteronline.upload.editor.core.model.TextClip

class CaptionGenerator(
    private val subtitleParser: SubtitleParser = SubtitleParser(),
) {
    fun configure() = Unit

    fun fromSrt(srt: String, baseStyle: TextClip.TextStyle, layer: Int = 100): List<TextClip> {
        return subtitleParser.parseSrt(srt).mapIndexed { index, cue ->
            TextClip(
                clipId = "subtitle_$index",
                startMs = cue.startMs,
                endMs = cue.endMs,
                layer = layer,
                text = cue.text,
                style = baseStyle,
                animation = TextClip.TextAnimation(
                    inPreset = TextClip.TextAnimation.Preset.FADE,
                    outPreset = TextClip.TextAnimation.Preset.FADE,
                    inDurationMs = 120,
                    outDurationMs = 120,
                ),
            )
        }
    }

    fun karaokeWords(text: String, startMs: Long, endMs: Long): List<WordCue> {
        val words = text.split(Regex("\\s+")).filter { it.isNotBlank() }
        if (words.isEmpty()) return emptyList()
        val span = (endMs - startMs).coerceAtLeast(1L)
        val slice = span / words.size
        return words.mapIndexed { index, word ->
            val wStart = startMs + slice * index
            val wEnd = if (index == words.lastIndex) endMs else wStart + slice
            WordCue(word, wStart, wEnd)
        }
    }

    data class WordCue(val word: String, val startMs: Long, val endMs: Long)
}
