package com.app.clipsteronline.upload.editor.timeline.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

/**
 * View for rendering clip trim handles.
 */
class ClipHandleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val handlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val indicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var handleType: HandleType = HandleType.NONE
    private var isActive = false
    private var isHighlighted = false

    private var handleListener: HandleListener? = null

    init {
        setupPaints()
    }

    /**
     * Set handle type.
     */
    fun setHandleType(type: HandleType) {
        this.handleType = type
        invalidate()
    }

    /**
     * Set active state.
     */
    fun setActive(active: Boolean) {
        this.isActive = active
        invalidate()
    }

    /**
     * Set handle listener.
     */
    fun setHandleListener(listener: HandleListener?) {
        this.handleListener = listener
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (handleType == HandleType.NONE) return

        val isLeft = handleType == HandleType.LEFT
        val handleWidth = width.toFloat()
        val handleHeight = height.toFloat()

        // Draw handle background
        handlePaint.color = if (isActive) Color.parseColor("#FF6B35") else Color.parseColor("#666666")
        handlePaint.style = Paint.Style.FILL

        if (isLeft) {
            canvas.drawRect(0f, 0f, handleWidth, handleHeight, handlePaint)
        } else {
            canvas.drawRect(0f, 0f, handleWidth, handleHeight, handlePaint)
        }

        // Draw grip lines
        indicatorPaint.color = Color.WHITE
        indicatorPaint.strokeWidth = 2f

        val lineCount = 2
        val spacing = handleWidth / (lineCount + 1)
        for (i in 1..lineCount) {
            val x = spacing * i
            canvas.drawLine(x, handleHeight * 0.3f, x, handleHeight * 0.7f, indicatorPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                handleListener?.onHandleTouchStart(handleType)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                handleListener?.onHandleTouchMove(handleType, event.x)
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                handleListener?.onHandleTouchEnd(handleType)
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun setupPaints() {
        handlePaint.style = Paint.Style.FILL
        indicatorPaint.style = Paint.Style.STROKE
    }
}

/**
 * Handle types.
 */
enum class HandleType {
    NONE,
    LEFT,
    RIGHT
}

/**
 * Handle listener.
 */
interface HandleListener {
    fun onHandleTouchStart(type: HandleType)
    fun onHandleTouchMove(type: HandleType, x: Float)
    fun onHandleTouchEnd(type: HandleType)
}