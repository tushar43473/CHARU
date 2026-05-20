package com.app.clipsteronline.upload.editor.core.model

data class ImageClip(
    override val clipId: String,
    override val startMs: Long,
    override val endMs: Long,
    override val layer: Int,
    val sourceUri: String,
    val durationMs: Long,
    val rotationDegrees: Float = 0f,
    val scale: Float = 1f,
    val transform: Transform = Transform(),
    val kenBurns: KenBurns? = null,
) : TimelineTrack.TimelineClip {
    init {
        require(clipId.isNotBlank())
        require(sourceUri.isNotBlank())
        require(durationMs > 0)
        require(startMs >= 0 && endMs >= startMs)
        require(durationMs == (endMs - startMs))
        require(scale > 0f)
    }

    data class Transform(val x: Float = 0f, val y: Float = 0f, val anchorX: Float = 0.5f, val anchorY: Float = 0.5f)

    data class KenBurns(
        val startScale: Float,
        val endScale: Float,
        val startX: Float,
        val startY: Float,
        val endX: Float,
        val endY: Float,
        val easing: Easing = Easing.EASE_IN_OUT,
    ) {
        init { require(startScale > 0f && endScale > 0f) }
        enum class Easing { LINEAR, EASE_IN, EASE_OUT, EASE_IN_OUT }
    }
}
