package com.app.clipsteronline.upload.editor.core.model

data class TextClip(
    override val clipId: String,
    override val startMs: Long,
    override val endMs: Long,
    override val layer: Int,
    val text: String,
    val style: TextStyle,
    val animation: TextAnimation? = null,
    val positionX: Float = 0.5f,
    val positionY: Float = 0.8f,
) : TimelineTrack.TimelineClip {
    init {
        require(clipId.isNotBlank())
        require(text.isNotBlank())
        require(startMs >= 0 && endMs >= startMs)
        require(positionX in 0f..1f && positionY in 0f..1f)
    }

    data class TextStyle(
        val fontFamily: String,
        val fontSizeSp: Float,
        val colorArgb: Long,
        val strokeColorArgb: Long? = null,
        val strokeWidthPx: Float = 0f,
        val shadowColorArgb: Long? = null,
        val shadowRadiusPx: Float = 0f,
    ) {
        init {
            require(fontFamily.isNotBlank())
            require(fontSizeSp > 0f)
            require(strokeWidthPx >= 0f)
            require(shadowRadiusPx >= 0f)
        }
    }

    data class TextAnimation(
        val inPreset: Preset? = null,
        val outPreset: Preset? = null,
        val inDurationMs: Long = 0L,
        val outDurationMs: Long = 0L,
    ) {
        init {
            require(inDurationMs >= 0 && outDurationMs >= 0)
        }

        enum class Preset { FADE, SLIDE_UP, SLIDE_DOWN, SCALE, TYPEWRITER }
    }
}
