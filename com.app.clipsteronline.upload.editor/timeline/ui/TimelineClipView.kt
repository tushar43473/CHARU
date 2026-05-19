package com.app.clipsteronline.upload.editor.timeline.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

/**
 * View for rendering clip blocks in timeline.
 */
class TimelineClipView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val clipPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val thumbnailPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val handlePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var clipId: String = ""
    private var clipColor: Int = Color.parseColor("#3D5AFE")
    private var isSelected: Boolean = false
    private var thumbnail: Bitmap? = null

    private var showThumbnail: Boolean = true
    private var showDuration: Boolean = true

    private var clipHandleListener: ClipHandleListener? = null

    private val cornerRadius = 8f

    init {
        setupPaints()
    }

    /**
     * Set clip properties.
     */
    fun setClip(id: String, color: Int) {
        this.clipId = id
        this.clipColor = color
        invalidate()
    }

    /**
     * Set selection.
     */
    fun setSelected(selected: Boolean) {
        this.isSelected = selected
        invalidate()
    }

    /**
     * Set thumbnail.
     */
    fun setThumbnail(bitmap: Bitmap?) {
        this.thumbnail = bitmap
        invalidate()
    }

    /**
     * Set clip handle listener.
     */
    fun setClipHandleListener(listener: ClipHandleListener?) {
        this.clipHandleListener = listener
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        drawClipBackground(canvas)
        drawThumbnail(canvas)
        drawSelectionBorder(canvas)
        drawHandles(canvas)
    }

    /**
     * Draw clip background.
     */
    private fun drawClipBackground(canvas: Canvas) {
        clipPaint.color = clipColor

        val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, clipPaint)
    }

    /**
     * Draw thumbnail.
     */
    private fun drawThumbnail(canvas: Canvas) {
        thumbnail?.let { bmp ->
            if (showThumbnail && bmp.width > 0) {
                val srcRect = android.graphics.Rect(0, 0, bmp.width, bmp.height)
                val dstRect = RectF(4f, 4f, height - 4f, height - 4f)
                canvas.drawBitmap(bmp, srcRect, dstRect, thumbnailPaint)
            }
        }
    }

    /**
     * Draw selection border.
     */
    private fun drawSelectionBorder(canvas: Canvas) {
        if (isSelected) {
            borderPaint.color = Color.parseColor("#FF6B35")
            borderPaint.style = Paint.Style.STROKE
            borderPaint.strokeWidth = 3f

            val rect = RectF(2f, 2f, width - 2f, height - 2f)
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, borderPaint)
        }
    }

    /**
     * Draw handles.
     */
    private fun drawHandles(canvas: Canvas) {
        if (isSelected) {
            handlePaint.color = Color.parseColor("#FF6B35")
            handlePaint.style = Paint.Style.FILL

            // Left handle
            canvas.drawRect(0f, height / 2 - 20f, 12f, height / 2 + 20f, handlePaint)
            // Right handle
            canvas.drawRect(width - 12f, height / 2 - 20f, width.toFloat(), height / 2 + 20f, handlePaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val handle = getHandleAt(event.x)
                if (handle != null) {
                    clipHandleListener?.onHandleDragStart(clipId, handle)
                    return true
                }
            }
            MotionEvent.ACTION_MOVE -> {
                clipHandleListener?.onHandleDrag(clipId, event.x)
                return true
            }
            MotionEvent.ACTION_UP -> {
                clipHandleListener?.onHandleDragEnd(clipId)
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    /**
     * Get handle at position.
     */
    private fun getHandleAt(x: Float): ClipHandle? {
        return when {
            x < 20f -> ClipHandle.LEFT
            x > width - 20f -> ClipHandle.RIGHT
            else -> null
        }
    }

    private fun setupPaints() {
        clipPaint.style = Paint.Style.FILL
        thumbnailPaint.isFilterBitmap = true
        borderPaint.style = Paint.Style.STROKE
        textPaint.color = Color.WHITE
        textPaint.textSize = 24f
        handlePaint.style = Paint.Style.FILL
    }
}

/**
 * Clip handle positions.
 */
enum class ClipHandle {
    LEFT,
    RIGHT
}

/**
 * Clip handle listener.
 */
interface ClipHandleListener {
    fun onHandleDragStart(clipId: String, handle: ClipHandle)
    fun onHandleDrag(clipId: String, x: Float)
    fun onHandleDragEnd(clipId: String)
}