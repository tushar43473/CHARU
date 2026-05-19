package com.app.clipsteronline.upload.editor.player

import android.graphics.Matrix
import kotlin.math.max
import kotlin.math.min

/**
 * Preview scaling calculations.
 * Handles crop/fill/fit and matrix transformations.
 */
object PreviewScaling {

    /**
     * Calculate fit scaling.
     * Scales video to fit within view maintaining aspect ratio.
     */
    fun calculateFitScaling(
        viewWidth: Int,
        viewHeight: Int,
        videoWidth: Int,
        videoHeight: Int
    ): ScaleResult {
        if (viewWidth == 0 || viewHeight == 0 || videoWidth == 0 || videoHeight == 0) {
            return ScaleResult.IDENTITY
        }

        val videoAspect = videoWidth.toFloat() / videoHeight
        val viewAspect = viewWidth.toFloat() / viewHeight

        val scale: Float
        val translateX: Float
        val translateY: Float

        if (videoAspect > viewAspect) {
            // Video is wider - fit to width
            scale = viewWidth.toFloat() / videoWidth
            val scaledHeight = videoHeight * scale
            translateX = 0f
            translateY = (viewHeight - scaledHeight) / 2
        } else {
            // Video is taller - fit to height
            scale = viewHeight.toFloat() / videoHeight
            val scaledWidth = videoWidth * scale
            translateX = (viewWidth - scaledWidth) / 2
            translateY = 0f
        }

        return ScaleResult(
            scaleX = scale,
            scaleY = scale,
            translateX = translateX,
            translateY = translateY
        )
    }

    /**
     * Calculate fill scaling.
     * Scales video to fill view, may crop edges.
     */
    fun calculateFillScaling(
        viewWidth: Int,
        viewHeight: Int,
        videoWidth: Int,
        videoHeight: Int
    ): ScaleResult {
        if (viewWidth == 0 || viewHeight == 0 || videoWidth == 0 || videoHeight == 0) {
            return ScaleResult.IDENTITY
        }

        val videoAspect = videoWidth.toFloat() / videoHeight
        val viewAspect = viewWidth.toFloat() / viewHeight

        val scale: Float
        val translateX: Float
        val translateY: Float

        if (videoAspect > viewAspect) {
            // Video is wider - fill to height
            scale = viewHeight.toFloat() / videoHeight
            val scaledWidth = videoWidth * scale
            translateX = (viewWidth - scaledWidth) / 2
            translateY = 0f
        } else {
            // Video is taller - fill to width
            scale = viewWidth.toFloat() / videoWidth
            val scaledHeight = videoHeight * scale
            translateX = 0f
            translateY = (viewHeight - scaledHeight) / 2
        }

        return ScaleResult(
            scaleX = scale,
            scaleY = scale,
            translateX = translateX,
            translateY = translateY
        )
    }

    /**
     * Calculate crop scaling.
     * Center crops to fill view.
     */
    fun calculateCropScaling(
        viewWidth: Int,
        viewHeight: Int,
        videoWidth: Int,
        videoHeight: Int
    ): ScaleResult {
        if (viewWidth == 0 || viewHeight == 0 || videoWidth == 0 || videoHeight == 0) {
            return ScaleResult.IDENTITY
        }

        val scale = max(
            viewWidth.toFloat() / videoWidth,
            viewHeight.toFloat() / videoHeight
        )

        val scaledWidth = videoWidth * scale
        val scaledHeight = videoHeight * scale

        val translateX = (viewWidth - scaledWidth) / 2
        val translateY = (viewHeight - scaledHeight) / 2

        return ScaleResult(
            scaleX = scale,
            scaleY = scale,
            translateX = translateX,
            translateY = translateY
        )
    }

    /**
     * Calculate stretch scaling.
     * Stretches to fill view, ignores aspect ratio.
     */
    fun calculateStretchScaling(
        viewWidth: Int,
        viewHeight: Int,
        videoWidth: Int,
        videoHeight: Int
    ): ScaleResult {
        if (videoWidth == 0 || videoHeight == 0) {
            return ScaleResult.IDENTITY
        }

        return ScaleResult(
            scaleX = viewWidth.toFloat() / videoWidth,
            scaleY = viewHeight.toFloat() / videoHeight,
            translateX = 0f,
            translateY = 0f
        )
    }

    /**
     * Calculate matrix for scaling.
     */
    fun calculateMatrix(
        result: ScaleResult,
        videoWidth: Int,
        videoHeight: Int
    ): Matrix {
        val matrix = Matrix()

        matrix.postTranslate(result.translateX, result.translateY)
        matrix.postScale(result.scaleX, result.scaleY, result.translateX, result.translateY)

        return matrix
    }

    /**
     * Calculate matrix from parameters directly.
     */
    fun calculateMatrix(
        viewWidth: Int,
        viewHeight: Int,
        videoWidth: Int,
        videoHeight: Int,
        mode: PreviewScaleMode
    ): Matrix {
        val result = when (mode) {
            PreviewScaleMode.FIT -> calculateFitScaling(viewWidth, viewHeight, videoWidth, videoHeight)
            PreviewScaleMode.FILL -> calculateFillScaling(viewWidth, viewHeight, videoWidth, videoHeight)
            PreviewScaleMode.CROP -> calculateCropScaling(viewWidth, viewHeight, videoWidth, videoHeight)
            PreviewScaleMode.STRETCH -> calculateStretchScaling(viewWidth, viewHeight, videoWidth, videoHeight)
        }

        return calculateMatrix(result, videoWidth, videoHeight)
    }
}

/**
 * Scale mode.
 */
enum class PreviewScaleMode {
    FIT,
    FILL,
    CROP,
    STRETCH
}

/**
 * Scale result.
 */
data class ScaleResult(
    val scaleX: Float = 1f,
    val scaleY: Float = 1f,
    val translateX: Float = 0f,
    val translateY: Float = 0f
) {
    companion object {
        val IDENTITY = ScaleResult(1f, 1f, 0f, 0f)
    }

    fun isIdentity(): Boolean = this == IDENTITY
}