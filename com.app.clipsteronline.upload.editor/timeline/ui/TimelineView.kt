package com.app.clipsteronline.upload.editor.timeline.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.OverScroller
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.app.clipsteronline.upload.editor.timeline.engine.TimelineEngine
import com.app.clipsteronline.upload.editor.timeline.engine.TimelineScrollEngine
import com.app.clipsteronline.upload.editor.timeline.engine.TimelineZoomEngine
import com.app.clipsteronline.upload.editor.timeline.engine.TimelineSnapEngine
import com.app.clipsteronline.upload.editor.timeline.engine.TimelineState

/**
 * Main timeline container view.
 * Horizontal scrolling with zoom support.
 */
class TimelineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var engine: TimelineEngine? = null
    private var scrollEngine: TimelineScrollEngine? = null
    private var zoomEngine: TimelineZoomEngine? = null
    private var snapEngine: TimelineSnapEngine? = null

    private val scroller = OverScroller(context)
    private var zoomGestureDetector: ScaleGestureDetector
    private var gestureDetector: GestureDetector

    private var timelineWidth = 0f
    private var trackHeight = 80f
    private var rulerHeight = 40f

    private var activePointerId = -1
    private var lastTouchX = 0f

    // Paints
    private val backgroundPaint = Paint()
    private val trackPaint = Paint()

    // Sub-views
    private var rulerView: TimelineRulerView? = null
    private var playheadView: PlayheadView? = null

    init {
        setBackgroundColor(Color.parseColor("#0D0D0D"))
        setupPaints()
        setupGestureDetectors()
        isClickable = true
        isFocusable = true
    }

    /**
     * Initialize timeline.
     */
    fun initialize(
        engine: TimelineEngine,
        scrollEngine: TimelineScrollEngine,
        zoomEngine: TimelineZoomEngine,
        snapEngine: TimelineSnapEngine
    ) {
        this.engine = engine
        this.scrollEngine = scrollEngine
        this.zoomEngine = zoomEngine
        this.snapEngine = snapEngine
        observeState()
    }

    /**
     * Observe timeline state.
     */
    private fun observeState() {
        scope.launch {
            engine?.state?.collectLatest {
                invalidateTimeline()
            }
        }

        scope.launch {
            scrollEngine?.scroll?.collect {
                invalidateTimeline()
            }
        }

        scope.launch {
            zoomEngine?.zoom?.collect {
                updateTimelineWidth()
                invalidateTimeline()
            }
        }
    }

    /**
     * Set ruler view.
     */
    fun setRulerView(ruler: TimelineRulerView) {
        this.rulerView = ruler
    }

    /**
     * Set playhead view.
     */
    fun setPlayheadView(playhead: PlayheadView) {
        this.playheadView = playhead
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val trackCount = 5
        val height = (rulerHeight + trackCount * trackHeight).toInt()
        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateTimelineWidth()
        scrollEngine?.setScrollBounds(0f, timelineWidth - w)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val state = engine?.state?.value ?: return
        val zoom = zoomEngine?.getZoom() ?: 1f
        val scrollX = scrollEngine?.getScroll() ?: 0f

        // Draw ruler
        rulerView?.draw(canvas, zoom, scrollX)

        // Draw tracks area
        drawTracks(canvas, state, zoom, scrollX)

        // Draw playhead
        playheadView?.let { playhead ->
            val playheadX = getXForTime(state.playheadPosition, zoom) - scrollX
            playhead.draw(canvas, playheadX, height.toFloat())
        }
    }

    /**
     * Draw tracks.
     */
    private fun drawTracks(canvas: Canvas, state: TimelineState, zoom: Float, scrollX: Float) {
        val tracks = engine?.getTracks() ?: return
        var y = rulerHeight

        tracks.forEachIndexed { index, _ ->
            val trackColor = if (index % 2 == 0) Color.parseColor("#141414") else Color.parseColor("#1A1A1A")
            trackPaint.color = trackColor
            canvas.drawRect(0f, y, width.toFloat(), y + trackHeight, trackPaint)
            y += trackHeight
        }
    }

    /**
     * Get X for time.
     */
    private fun getXForTime(timeMs: Long, zoom: Float): Float {
        return timeMs * zoom * 100 / 1000f
    }

    /**
     * Invalidate timeline.
     */
    private fun invalidateTimeline() {
        invalidate()
    }

    /**
     * Update timeline width.
     */
    private fun updateTimelineWidth() {
        val duration = engine?.getDuration() ?: 0L
        val zoom = zoomEngine?.getZoom() ?: 1f
        timelineWidth = getXForTime(duration, zoom)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.actionMasked

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                activePointerId = event.getPointerId(0)
                lastTouchX = event.x
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val pointerIndex = event.findPointerIndex(activePointerId)
                if (pointerIndex >= 0) {
                    val x = event.getX(pointerIndex)
                    val dx = x - lastTouchX
                    scrollEngine?.onScroll(dx)
                    lastTouchX = x
                }
                return true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                activePointerId = -1
                return true
            }
        }

        return gestureDetector.onTouchEvent(event) || zoomGestureDetector.onTouchEvent(event)
    }

    /**
     * Setup paints.
     */
    private fun setupPaints() {
        backgroundPaint.color = Color.parseColor("#0D0D0D")
        backgroundPaint.style = Paint.Style.FILL
        trackPaint.color = Color.parseColor("#141414")
        trackPaint.style = Paint.Style.FILL
    }

    /**
     * Setup gesture detectors.
     */
    private fun setupGestureDetectors() {
        gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
                scrollEngine?.onScroll(distanceX)
                return true
            }

            override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                scrollEngine?.fling(velocityX)
                return true
            }
        })

        zoomGestureDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                zoomEngine?.onPinch(detector.scaleFactor, detector.focusX.toFloat(), width.toFloat(), scrollEngine?.getScroll() ?: 0f)
                return true
            }
        })
    }
}