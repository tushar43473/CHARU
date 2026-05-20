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
) {
    private val scroller = OverScroller(context, interpolator)

    private val _state = MutableStateFlow(TimelineScrollState())
    val state: StateFlow<TimelineScrollState> = _state.asStateFlow()

    @Synchronized
    fun setBounds(maxHorizontalPx: Double, maxVerticalPx: Double) {
        _state.value = _state.value.copy(
            maxHorizontalPx = max(0.0, maxHorizontalPx),
            maxVerticalPx = max(0.0, maxVerticalPx),
            horizontalPx = _state.value.horizontalPx.coerceIn(0.0, maxHorizontalPx),
            verticalPx = _state.value.verticalPx.coerceIn(0.0, maxVerticalPx),
        )
    }

    @Synchronized
    fun scrollBy(dx: Double, dy: Double): TimelineScrollState {
        val current = _state.value
        val next = current.copy(
            horizontalPx = (current.horizontalPx + dx).coerceIn(0.0, current.maxHorizontalPx),
            verticalPx = (current.verticalPx + dy).coerceIn(0.0, current.maxVerticalPx),
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
        scroller.fling(
            current.horizontalPx.toInt(),
            current.verticalPx.toInt(),
            velocityX.toInt(),
            velocityY.toInt(),
            0,
            current.maxHorizontalPx.toInt(),
            0,
            current.maxVerticalPx.toInt(),
        )
        _state.value = current.copy(
            velocityX = velocityX,
            velocityY = velocityY,
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
    fun computeNextFrame(): TimelineScrollState {
        if (!scroller.computeScrollOffset()) {
            if (_state.value.isFlinging) {
                _state.value = _state.value.copy(isFlinging = false, velocityX = 0.0, velocityY = 0.0)
            }
            return _state.value
        }

        val current = _state.value
        val nextHorizontal = scroller.currX.toDouble().coerceIn(0.0, current.maxHorizontalPx)
        val nextVertical = scroller.currY.toDouble().coerceIn(0.0, current.maxVerticalPx)

        val next = current.copy(
            horizontalPx = nextHorizontal,
            verticalPx = nextVertical,
            velocityX = scroller.currVelocity.toDouble() * velocitySign(current.horizontalPx, nextHorizontal),
            velocityY = scroller.currVelocity.toDouble() * velocitySign(current.verticalPx, nextVertical),
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
        val next = current.copy(
            horizontalPx = horizontalPx.coerceIn(0.0, current.maxHorizontalPx),
            verticalPx = verticalPx.coerceIn(0.0, current.maxVerticalPx),
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
    val isFlinging: Boolean = false,
)
