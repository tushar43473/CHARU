package com.app.clipsteronline.upload.editor.player

import android.graphics.Matrix
import kotlin.math.max
import kotlin.math.min

enum class PreviewScaleMode { FIT_CENTER, CENTER_CROP, FILL, CUSTOM }

data class PreviewTransform(
    val matrix: Matrix,
    val appliedScaleX: Float,
    val appliedScaleY: Float,
)

class PreviewScaling {
    fun buildMatrix(
        viewWidth: Int,
        viewHeight: Int,
        contentWidth: Int,
        contentHeight: Int,
        mode: PreviewScaleMode,
        customScale: Float = 1f,
        panX: Float = 0f,
        panY: Float = 0f,
    ): PreviewTransform {
        val matrix = Matrix()
        if (viewWidth <= 0 || viewHeight <= 0 || contentWidth <= 0 || contentHeight <= 0) {
            return PreviewTransform(matrix, 1f, 1f)
        }

        val sx = viewWidth.toFloat() / contentWidth.toFloat()
        val sy = viewHeight.toFloat() / contentHeight.toFloat()
        val baseScaleX: Float
        val baseScaleY: Float
        when (mode) {
            PreviewScaleMode.FIT_CENTER -> {
                val s = min(sx, sy)
                baseScaleX = s
                baseScaleY = s
            }
            PreviewScaleMode.CENTER_CROP -> {
                val s = max(sx, sy)
                baseScaleX = s
                baseScaleY = s
            }
            PreviewScaleMode.FILL -> {
                baseScaleX = sx
                baseScaleY = sy
            }
            PreviewScaleMode.CUSTOM -> {
                val c = customScale.coerceIn(0.2f, 8f)
                baseScaleX = c
                baseScaleY = c
            }
        }

        val scaledW = contentWidth * baseScaleX
        val scaledH = contentHeight * baseScaleY
        val dx = ((viewWidth - scaledW) / 2f) + panX
        val dy = ((viewHeight - scaledH) / 2f) + panY

        matrix.postScale(baseScaleX, baseScaleY)
        matrix.postTranslate(dx, dy)
        return PreviewTransform(matrix, baseScaleX, baseScaleY)
    }
}
