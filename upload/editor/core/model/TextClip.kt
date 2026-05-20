package upload.editor.core.model

data class TextClip(
    override val id: String,
    override val startTimeMs: Long,
    override val endTimeMs: Long,
    override val zIndex: Int = 0,
    val content: String,
    val style: TextStyle,
    val animation: TextAnimation? = null,
    val positionX: Float = 0.5f,
    val positionY: Float = 0.8f,
) : TimelineTrack.TimelineClip {
    init {
        require(id.isNotBlank())
        require(content.isNotBlank())
        require(startTimeMs >= 0 && endTimeMs >= startTimeMs)
        require(positionX in 0f..1f)
        require(positionY in 0f..1f)
    }

    data class TextStyle(
        val fontFamily: String,
        val fontSizeSp: Float,
        val colorArgb: Long,
        val strokeColorArgb: Long? = null,
        val strokeWidthPx: Float = 0f,
        val shadowColorArgb: Long? = null,
        val shadowRadiusPx: Float = 0f,
        val shadowDxPx: Float = 0f,
        val shadowDyPx: Float = 0f,
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
        val inDurationMs: Long = 0,
        val outDurationMs: Long = 0,
    ) {
        init {
            require(inDurationMs >= 0)
            require(outDurationMs >= 0)
        }

        enum class Preset { FADE, SLIDE_UP, SLIDE_DOWN, SCALE, TYPEWRITER }
    }
}
