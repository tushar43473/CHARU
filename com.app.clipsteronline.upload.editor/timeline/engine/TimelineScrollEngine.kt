package com.app.clipsteronline.upload.editor.timeline.engine

import android.content.Context
import android.view.animation.Interpolator
import android.widget.OverScroller
import kotlin.math.abs
import kotlin.math.max
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TimelineScrollEngine(
    context: Context,
    interpolator: Interpolator? = null,
    private val physics: TimelinePhysics = TimelinePhysics(),
) {
    private val scroller = OverScroller(context, interpolator)

    private val _state = MutableStateFlow(TimelineScrollState())
    val state: StateFlow<TimelineScrollState> = _state.asStateFlow()

    @Synchronized
    fun setBounds(maxHorizontalPx: Double, maxVerticalPx: Double) {
        val current = _state.value
        _state.value = current.copy(
            maxHorizontalPx = max(0.0, maxHorizontalPx),
            maxVerticalPx = max(0.0, maxVerticalPx),
            horizontalPx = current.horizontalPx.coerceIn(0.0, maxHorizontalPx),
            verticalPx = current.verticalPx.coerceIn(0.0, maxVerticalPx),
        )
    }

    @Synchronized
    fun scrollBy(dx: Double, dy: Double, viewportWidthPx: Double = 0.0, viewportHeightPx: Double = 0.0): TimelineScrollState {
        val current = _state.value
        val dampedDx = physics.dampDelta(dx, viewportWidthPx)
        val dampedDy = physics.dampDelta(dy, viewportHeightPx)
        val horizontal = physics.resolveBounds(current.horizontalPx + dampedDx, 0.0, current.maxHorizontalPx)
        val vertical = physics.resolveBounds(current.verticalPx + dampedDy, 0.0, current.maxVerticalPx)

        val next = current.copy(
            horizontalPx = horizontal.clampedPositionPx,
            verticalPx = vertical.clampedPositionPx,
            overscrollXPx = horizontal.overscrollPx,
            overscrollYPx = vertical.overscrollPx,
            velocityX = 0.0,
            velocityY = 0.0,
            isFlinging = false,
        )
        _state.value = next
        return next
    }

    @Synchronized
    fun fling(velocityX: Double, velocityY: Double): TimelineScrollState {
        val current = _state.value
        val safeVelocityX = physics.sanitizeVelocity(velocityX)
        val safeVelocityY = physics.sanitizeVelocity(velocityY)

        scroller.fling(
            current.horizontalPx.toInt(),
            current.verticalPx.toInt(),
            safeVelocityX.toInt(),
            safeVelocityY.toInt(),
            0,
            current.maxHorizontalPx.toInt(),
            0,
            current.maxVerticalPx.toInt(),
        )
        _state.value = current.copy(
            velocityX = safeVelocityX,
            velocityY = safeVelocityY,
            overscrollXPx = 0.0,
            overscrollYPx = 0.0,
            isFlinging = true,
        )
        return _state.value
    }

    @Synchronized
    fun stop() {
        if (!scroller.isFinished) scroller.forceFinished(true)
        _state.value = _state.value.copy(isFlinging = false, velocityX = 0.0, velocityY = 0.0)
    }

    @Synchronized
    fun computeNextFrame(frameDeltaMs: Long = 16L): TimelineScrollState {
        val current = _state.value
        if (!scroller.computeScrollOffset()) {
            val springX = physics.springBackVelocity(current.overscrollXPx)
            val springY = physics.springBackVelocity(current.overscrollYPx)
            return if (abs(springX) < 0.1 && abs(springY) < 0.1) {
                _state.value = current.copy(isFlinging = false, velocityX = 0.0, velocityY = 0.0, overscrollXPx = 0.0, overscrollYPx = 0.0)
                _state.value
            } else {
                val recovered = scrollBy(springX * (frameDeltaMs / 1000.0), springY * (frameDeltaMs / 1000.0))
                _state.value = recovered.copy(isFlinging = true)
                _state.value
            }
        }

        val horizontal = physics.resolveBounds(scroller.currX.toDouble(), 0.0, current.maxHorizontalPx)
        val vertical = physics.resolveBounds(scroller.currY.toDouble(), 0.0, current.maxVerticalPx)
        val rawVelocity = scroller.currVelocity.toDouble()
        val signedX = rawVelocity * velocitySign(current.horizontalPx, horizontal.clampedPositionPx)
        val signedY = rawVelocity * velocitySign(current.verticalPx, vertical.clampedPositionPx)

        val next = current.copy(
            horizontalPx = horizontal.clampedPositionPx,
            verticalPx = vertical.clampedPositionPx,
            overscrollXPx = horizontal.overscrollPx,
            overscrollYPx = vertical.overscrollPx,
            velocityX = physics.applyFriction(signedX, frameDeltaMs),
            velocityY = physics.applyFriction(signedY, frameDeltaMs),
            isFlinging = !scroller.isFinished,
        )
        _state.value = next
        return next
    }

    fun centerOn(playheadTimeUs: Long, zoomEngine: TimelineZoomEngine, viewportWidthPx: Double): TimelineScrollState {
        val playheadPixel = zoomEngine.timeUsToPixel(playheadTimeUs)
        val desired = max(0.0, playheadPixel - viewportWidthPx / 2.0)
        return scrollTo(desired, _state.value.verticalPx)
    }

    fun scrollTo(horizontalPx: Double, verticalPx: Double): TimelineScrollState {
        val current = _state.value
        val horizontal = physics.resolveBounds(horizontalPx, 0.0, current.maxHorizontalPx)
        val vertical = physics.resolveBounds(verticalPx, 0.0, current.maxVerticalPx)
        val next = current.copy(
            horizontalPx = horizontal.clampedPositionPx,
            verticalPx = vertical.clampedPositionPx,
            overscrollXPx = horizontal.overscrollPx,
            overscrollYPx = vertical.overscrollPx,
            isFlinging = false,
            velocityX = 0.0,
            velocityY = 0.0,
        )
        _state.value = next
        return next
    }

    private fun velocitySign(previous: Double, current: Double): Double {
        val delta = current - previous
        if (abs(delta) < 0.0001) return 0.0
        return if (delta > 0.0) 1.0 else -1.0
    }
}

data class TimelineScrollState(
    val horizontalPx: Double = 0.0,
    val verticalPx: Double = 0.0,
    val velocityX: Double = 0.0,
    val velocityY: Double = 0.0,
    val maxHorizontalPx: Double = 0.0,
    val maxVerticalPx: Double = 0.0,
    val overscrollXPx: Double = 0.0,
    val overscrollYPx: Double = 0.0,
    val isFlinging: Boolean = false,
)
