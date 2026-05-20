package com.app.clipsteronline.upload.editor.text

import com.app.clipsteronline.upload.editor.core.model.TextClip

class TextEngine(
    private val textRenderer: TextRenderer = TextRenderer(),
    private val captionGenerator: CaptionGenerator = CaptionGenerator(),
) {
    private val clips = mutableListOf<TextClip>()

    fun configure() = Unit

    fun setClips(newClips: List<TextClip>) {
        clips.clear()
        clips.addAll(newClips)
    }

    fun addClip(clip: TextClip) {
        clips += clip
    }

    fun loadSubtitles(srt: String, style: TextClip.TextStyle, layer: Int = 100) {
        clips += captionGenerator.fromSrt(srt, style, layer)
    }

    fun renderAt(timeMs: Long, viewportWidth: Int, viewportHeight: Int): List<TextRenderer.RenderedText> {
        return clips.filter { timeMs in it.startMs..it.endMs }
            .sortedBy { it.layer }
            .map { textRenderer.render(it, timeMs, viewportWidth, viewportHeight) }
    }
}
