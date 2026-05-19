package com.app.clipsteronline.upload.editor.gestures

import android.view.MotionEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Central gesture coordinator.
 * Routes gestures to timeline/preview/clip handlers.
 */
class GestureEngine(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
) {
    private val _gestureState = MutableStateFlow(GestureEngineState())
    val gestureState: StateFlow<GestureEngineState> = _gestureState.asStateFlow()

    private var timelineHandler: TimelineGestureHandler? = null
    private var clipHandler: ClipGestureHandler? = null
    private var previewHandler: PreviewGestureHandler? = null
    private var multiTouchHandler: MultiTouchHandler? = null

    private var activeRegion: GestureRegion = GestureRegion.TIMELINE
    private var gestureListener: GestureListener? = null

    /**
     * Set handlers.
     */
    fun setHandlers(
        timeline: TimelineGestureHandler,
        clip: ClipGestureHandler,
        preview: PreviewGestureHandler
    ) {
        this.timelineHandler = timeline
        this.clipHandler = clip
        this.previewHandler = preview
        multiTouchHandler = MultiTouchHandler()
    }

    /**
     * Set gesture listener.
     */
    fun setListener(listener: GestureListener) {
        this.gestureListener = listener
    }

    /**
     * Handle touch event.
     */
    fun onTouchEvent(event: MotionEvent, canvasWidth: Int, canvasHeight: Int): Boolean {
        val action = event.actionMasked
        val pointerCount = event.pointerCount

        // Determine active region
        activeRegion = detectRegion(event, canvasWidth, canvasHeight, activeRegion)

        // Route to handler
        when (activeRegion) {
            GestureRegion.TIMELINE -> {
                timelineHandler?.onTouchEvent(event, canvasWidth)
            }
            GestureRegion.CLIP -> {
                clipHandler?.onTouchEvent(event, canvasWidth)
            }
            GestureRegion.PREVIEW -> {
                previewHandler?.onTouchEvent(event, canvasWidth, canvasHeight)
            }
        }

        // Update state
        _gestureState.value = _gestureState.value.copy(
            activeRegion = activeRegion,
            pointerCount = pointerCount,
            isActive = action != MotionEvent.ACTION_UP && action != MotionEvent.ACTION_CANCEL
        )

        return true
    }

    /**
     * Detect active region.
     */
    private fun detectRegion(
        event: MotionEvent,
        canvasWidth: Int,
        canvasHeight: Int,
        currentRegion: GestureRegion
    ): GestureRegion {
        val y = event.y
        val ratio = y / canvasHeight

        // Bottom 40% = timeline area
        return if (ratio > 0.6f) {
            GestureRegion.TIMELINE
        } else if (ratio > 0.3f) {
            GestureRegion.CLIP
        } else {
            GestureRegion.PREVIEW
        }
    }

    /**
     * Set snap guide engine.
     */
    fun setSnapGuideEngine(engine: SnapGuideEngine) {
        clipHandler?.setSnapGuide(engine)
    }

    /**
     * Release resources.
     */
    fun release() {
        timelineHandler = null
        clipHandler = null
        previewHandler = null
        multiTouchHandler = null
    }
}

/**
 * Gesture engine state.
 */
data class GestureEngineState(
    val activeRegion: GestureRegion = GestureRegion.TIMELINE,
    val pointerCount: Int = 0,
    val isActive: Boolean = false,
    val isDragging: Boolean = false
)

/**
 * Gesture regions.
 */
enum class GestureRegion {
    TIMELINE,
    CLIP,
    PREVIEW
}

/**
 * Gesture listener.
 */
interface GestureListener {
    fun onGestureStart(gesture: GestureType)
    fun onGestureUpdate(gesture: GestureType)
    fun onGestureEnd(gesture: GestureType)
}

/**
 * Gesture types.
 */
enum class GestureType {
    DRAG,
    PINCH,
    ROTATE,
    FLING,
    TAP,
    LONG_PRESS
}