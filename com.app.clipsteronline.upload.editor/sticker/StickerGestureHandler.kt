package com.app.clipsteronline.upload.editor.sticker

import com.app.clipsteronline.upload.editor.core.model.StickerClip
import kotlin.math.atan2
import kotlin.math.sqrt

class StickerGestureHandler {
    fun configure() = Unit

    fun drag(clip: StickerClip, deltaX: Float, deltaY: Float): StickerClip {
        val nextX = (clip.transform.x + deltaX).coerceIn(0f, 1f)
        val nextY = (clip.transform.y + deltaY).coerceIn(0f, 1f)
        return clip.copy(transform = clip.transform.copy(x = nextX, y = nextY))
    }

    fun pinchAndRotate(clip: StickerClip, startA: TouchPoint, startB: TouchPoint, endA: TouchPoint, endB: TouchPoint): StickerClip {
        val startDistance = distance(startA, startB).coerceAtLeast(1e-4f)
        val endDistance = distance(endA, endB).coerceAtLeast(1e-4f)
        val scaleFactor = (endDistance / startDistance).coerceIn(0.4f, 3f)

        val startAngle = angle(startA, startB)
        val endAngle = angle(endA, endB)
        val deltaRotation = endAngle - startAngle

        val nextScale = (clip.scale * scaleFactor).coerceIn(0.1f, 8f)
        val nextRotation = clip.rotationDegrees + deltaRotation
        return clip.copy(scale = nextScale, rotationDegrees = nextRotation)
    }

    fun flipHorizontal(clip: StickerClip): StickerClip = clip.copy(isFlippedHorizontally = !clip.isFlippedHorizontally)
    fun flipVertical(clip: StickerClip): StickerClip = clip.copy(isFlippedVertically = !clip.isFlippedVertically)

    private fun distance(a: TouchPoint, b: TouchPoint): Float = sqrt((b.x - a.x) * (b.x - a.x) + (b.y - a.y) * (b.y - a.y))
    private fun angle(a: TouchPoint, b: TouchPoint): Float = Math.toDegrees(atan2((b.y - a.y).toDouble(), (b.x - a.x).toDouble())).toFloat()

    data class TouchPoint(val x: Float, val y: Float)
}
