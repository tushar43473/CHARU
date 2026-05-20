package com.app.clipsteronline.upload.editor.timeline.engine

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToLong

/**
 * Keeps timeline scale math deterministic and frame-accurate.
 */
class TimelineZoomEngine(
    initialPixelsPerSecond: Double = DEFAULT_PIXELS_PER_SECOND,
    val minPixelsPerSecond: Double = MIN_PIXELS_PER_SECOND,
    val maxPixelsPerSecond: Double = MAX_PIXELS_PER_SECOND,
    private val frameRate: Double = DEFAULT_FRAME_RATE,
) {

    @Volatile
    private var _pixelsPerSecond: Double = initialPixelsPerSecond.coerceIn(minPixelsPerSecond, maxPixelsPerSecond)

    @Volatile
    private var _density: Double = _pixelsPerSecond / DEFAULT_PIXELS_PER_SECOND

    val pixelsPerSecond: Double get() = _pixelsPerSecond
    val density: Double get() = _density

    fun setPixelsPerSecond(value: Double): ZoomChange {
        val clamped = value.coerceIn(minPixelsPerSecond, maxPixelsPerSecond)
        val changed = abs(clamped - _pixelsPerSecond) >= EPSILON
        _pixelsPerSecond = clamped
        _density = _pixelsPerSecond / DEFAULT_PIXELS_PER_SECOND
        return ZoomChange(changed = changed, pixelsPerSecond = _pixelsPerSecond, density = _density)
    }

    fun applyPinch(
        scaleFactor: Double,
        pivotPx: Double,
        currentHorizontalOffsetPx: Double,
    ): ZoomTransform {
        val safeScale = if (scaleFactor.isFinite() && scaleFactor > 0.0) scaleFactor else 1.0
        val previous = _pixelsPerSecond
        val target = (previous * safeScale).coerceIn(minPixelsPerSecond, maxPixelsPerSecond)
        setPixelsPerSecond(target)

        val pivotTimeUs = pixelToTimeUs(pivotPx + currentHorizontalOffsetPx, previous)
        val pivotAfterPx = timeUsToPixel(pivotTimeUs)
        val newOffsetPx = max(0.0, pivotAfterPx - pivotPx)

        return ZoomTransform(
            pixelsPerSecond = _pixelsPerSecond,
            density = _density,
            pivotTimeUs = pivotTimeUs,
            adjustedHorizontalOffsetPx = newOffsetPx,
        )
    }

    fun pixelToTimeUs(pixel: Double, pxPerSecondOverride: Double = _pixelsPerSecond): Long {
        val safePx = max(0.0, pixel)
        return ((safePx / pxPerSecondOverride) * MICROS_PER_SECOND)
            .coerceAtLeast(0.0)
            .roundToLong()
    }

    fun timeUsToPixel(timeUs: Long, pxPerSecondOverride: Double = _pixelsPerSecond): Double {
        val safeUs = max(0L, timeUs)
        return (safeUs.toDouble() / MICROS_PER_SECOND) * pxPerSecondOverride
    }

    fun quantizeToFrame(timeUs: Long): Long {
        val frameDurationUs = MICROS_PER_SECOND / max(frameRate, 1.0)
        val frameIndex = (timeUs / frameDurationUs).roundToLong()
        return (frameIndex * frameDurationUs).roundToLong().coerceAtLeast(0L)
    }

    fun zoomToSelection(startUs: Long, endUs: Long, viewportWidthPx: Double, paddingRatio: Double = 0.12): ZoomChange {
        val durationUs = max(1L, endUs - startUs)
        val availablePx = viewportWidthPx * (1.0 - paddingRatio.coerceIn(0.0, 0.4))
        val requiredPps = (availablePx / durationUs.toDouble()) * MICROS_PER_SECOND
        return setPixelsPerSecond(requiredPps)
    }

    companion object {
        const val MICROS_PER_SECOND = 1_000_000.0
        const val DEFAULT_FRAME_RATE = 30.0
        const val DEFAULT_PIXELS_PER_SECOND = 160.0
        const val MIN_PIXELS_PER_SECOND = 24.0
        const val MAX_PIXELS_PER_SECOND = 3600.0
        private const val EPSILON = 0.000001
    }
}

data class ZoomChange(
    val changed: Boolean,
    val pixelsPerSecond: Double,
    val density: Double,
)

data class ZoomTransform(
    val pixelsPerSecond: Double,
    val density: Double,
    val pivotTimeUs: Long,
    val adjustedHorizontalOffsetPx: Double,
)
