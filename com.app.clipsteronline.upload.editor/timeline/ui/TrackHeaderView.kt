package com.app.clipsteronline.upload.editor.timeline.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

/**
 * View for rendering track headers with controls.
 */
class TrackHeaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val buttonPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var trackName: String = ""
    private var trackIndex: Int = 0

    private var isMuted: Boolean = false
    private var isLocked: Boolean = false
    private var isSelected: Boolean = false

    private var headerListener: TrackHeaderListener? = null

    private val buttonSize = 32f
    private val buttonMargin = 8f

    init {
        setupPaints()
    }

    /**
     * Set track info.
     */
    fun setTrackInfo(index: Int, name: String) {
        this.trackIndex = index
        this.trackName = name
        invalidate()
    }

    /**
     * Set muted state.
     */
    fun setMuted(muted: Boolean) {
        this.isMuted = muted
        invalidate()
    }

    /**
     * Set locked state.
     */
    fun setLocked(locked: Boolean) {
        this.isLocked = locked
        invalidate()
    }

    /**
     * Set selected state.
     */
    fun setSelected(selected: Boolean) {
        this.isSelected = selected
        invalidate()
    }

    /**
     * Set header listener.
     */
    fun setHeaderListener(listener: TrackHeaderListener?) {
        this.headerListener = listener
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        drawBackground(canvas)
        drawTrackName(canvas)
        drawButtons(canvas)
    }

    /**
     * Draw background.
     */
    private fun drawBackground(canvas: Canvas) {
        val bgColor = if (isSelected) Color.parseColor("#2A2A2A") else Color.parseColor("#1A1A1A")
        backgroundPaint.color = bgColor
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)
    }

    /**
     * Draw track name.
     */
    private fun drawTrackName(canvas: Canvas) {
        textPaint.color = Color.WHITE
        textPaint.textSize = 28f
        canvas.drawText(trackName, 16f, height / 2 + 10, textPaint)
    }

    /**
     * Draw buttons.
     */
    private fun drawButtons(canvas: Canvas) {
        // Mute button
        drawButton(canvas, 0, isMuted, "M")
        // Lock button
        drawButton(canvas, 1, isLocked, "L")
    }

    /**
     * Draw button.
     */
    private fun drawButton(canvas: Canvas, index: Int, isActive: Boolean, label: String) {
        val x = width - buttonSize - buttonMargin - (index * (buttonSize + buttonMargin))
        val y = (height - buttonSize) / 2

        buttonPaint.color = if (isActive) Color.parseColor("#FF6B35") else Color.parseColor("#333333")
        buttonPaint.style = Paint.Style.FILL

        val rect = RectF(x, y, x + buttonSize, y + buttonSize)
        canvas.drawRoundRect(rect, 4f, 4f, buttonPaint)

        textPaint.color = Color.WHITE
        textPaint.textSize = 20f
        textPaint.textAlign = Paint.Align.CENTER
        canvas.drawText(label, x + buttonSize / 2, y + buttonSize / 2 + 8, textPaint)
        textPaint.textAlign = Paint.Align.LEFT
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val buttonIndex = getButtonAt(event.x)
                when (buttonIndex) {
                    0 -> headerListener?.onMuteClick(trackIndex)
                    1 -> headerListener?.onLockClick(trackIndex)
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    /**
     * Get button at position.
     */
    private fun getButtonAt(x: Float): Int {
        for (i in 0..1) {
            val buttonX = width - buttonSize - buttonMargin - (i * (buttonSize + buttonMargin))
            if (x >= buttonX && x <= buttonX + buttonSize) {
                return i
            }
        }
        return -1
    }

    private fun setupPaints() {
        backgroundPaint.style = Paint.Style.FILL
        textPaint.style = Paint.Style.FILL
        iconPaint.style = Paint.Style.FILL
        buttonPaint.style = Paint.Style.FILL
    }
}

/**
 * Track header listener.
 */
interface TrackHeaderListener {
    fun onMuteClick(trackIndex: Int)
    fun onLockClick(trackIndex: Int)
    fun onVisibilityClick(trackIndex: Int)
    fun onSelectClick(trackIndex: Int)
}