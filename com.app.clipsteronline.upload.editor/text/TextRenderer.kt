package com.app.clipsteronline.upload.editor.text

import com.app.clipsteronline.upload.editor.core.model.TextClip

class TextRenderer(
    private val fontManager: FontManager = FontManager(),
    private val animationEngine: TextAnimationEngine = TextAnimationEngine(),
) {
    fun configure() = Unit

    fun render(clip: TextClip, timeMs: Long, viewportWidth: Int, viewportHeight: Int): RenderedText {
        val font = fontManager.resolve(clip.style.fontFamily)
        val anim = animationEngine.evaluate(clip, timeMs)
        val resolvedText = if (clip.animation?.inPreset == TextClip.TextAnimation.Preset.TYPEWRITER) {
            val chars = (clip.text.length * anim.progress).toInt().coerceIn(1, clip.text.length)
            clip.text.take(chars)
        } else clip.text

        val lines = wrap(resolvedText, maxCharsPerLine = 28)
        val x = (clip.positionX * viewportWidth)
        val y = ((clip.positionY + anim.offsetY) * viewportHeight)
        return RenderedText(lines, x, y, anim.alpha, anim.scale, font.family, clip.style)
    }

    private fun wrap(text: String, maxCharsPerLine: Int): List<String> {
        if (text.length <= maxCharsPerLine) return listOf(text)
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var current = ""
        words.forEach { word ->
            val candidate = if (current.isBlank()) word else "$current $word"
            if (candidate.length <= maxCharsPerLine) current = candidate
            else {
                if (current.isNotBlank()) lines += current
                current = word
            }
        }
        if (current.isNotBlank()) lines += current
        return lines
    }

    data class RenderedText(
        val lines: List<String>,
        val xPx: Float,
        val yPx: Float,
        val alpha: Float,
        val scale: Float,
        val fontFamily: String,
        val style: TextClip.TextStyle,
    )
}
