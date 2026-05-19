package com.app.clipsteronline.upload.editor.ui.toolbar

import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.os.Build
import android.view.View
import android.view.WindowInsets
import android.widget.FrameLayout
import android.widget.ImageButton

/**
 * Main editor top toolbar.
 * Back, title, export, undo/redo.
 */
class EditorToolbar(context: Context) : FrameLayout(context) {

    private val toolbarPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val buttonPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var projectTitle = "Untitled"
    private var backCallback: (() -> Unit)? = null
    private var exportCallback: (() -> Unit)? = null
    private var undoCallback: (() -> Unit)? = null
    private var redoCallback: (() -> Unit)? = null

    private var canUndo = false
    private var canRedo = false

    private val accentColor = 0xFFFF6B35.toInt()
    private val transparentBlack = 0xCC000000.toInt()

    /**
     * Set back listener.
     */
    fun setOnBackListener(callback: () -> Unit) {
        backCallback = callback
    }

    /**
     * Set export listener.
     */
    fun setOnExportListener(callback: () -> Unit) {
        exportCallback = callback
    }

    /**
     * Set undo listener.
     */
    fun setOnUndoListener(callback: () -> Unit) {
        undoCallback = callback
    }

    /**
     * Set redo listener.
     */
    fun setOnRedoListener(callback: () -> Unit) {
        redoCallback = callback
    }

    /**
     * Set project title.
     */
    fun setProjectTitle(title: String) {
        projectTitle = title
        invalidate()
    }

    /**
     * Update undo/redo state.
     */
    fun setUndoRedoState(canUndo: Boolean, canRedo: Boolean) {
        this.canUndo = canUndo
        this.canRedo = canRedo
        invalidate()
    }

    /**
     * Apply insets.
     */
    fun applyTopInsets() {
        if (context is Activity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                context.windowInsetsController?.let { controller ->
                    controller.hide(WindowInsets.Type.statusBars())
                }
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()

        // Translucent background
        toolbarPaint.color = transparentBlack
        toolbarPaint.style = Paint.Style.FILL
        canvas.drawRect(0f, 0f, w, h, toolbarPaint)

        // Back button (left)
        drawBackButton(canvas, 48f, h / 2)

        // Title (center)
        titlePaint.color = 0xFFFFFFFF.toInt()
        titlePaint.textSize = 36f
        titlePaint.textAlign = Paint.Align.CENTER
        canvas.drawText(projectTitle, w / 2, h / 2 + 12f, titlePaint)

        // Undo button
        drawUndoButton(canvas, w - 164f, h / 2)

        // Redo button
        drawRedoButton(canvas, w - 108f, h / 2)

        // Export button (far right)
        drawExportButton(canvas, w - 52f, h / 2)
    }

    private fun drawBackButton(canvas: Canvas, cx: Float, cy: Float) {
        buttonPaint.color = if (true) 0xFFFFFFFF.toInt() else 0x66FFFFFF.toInt()
        buttonPaint.style = Paint.Style.STROKE
        buttonPaint.strokeWidth = 3f

        val path = Path()
        path.moveTo(cx + 8f, cy)
        path.lineTo(cx - 4f, cy - 8f)
        path.lineTo(cx - 4f, cy - 3f)
        path.lineTo(cx - 8f, cy - 8f)
        path.lineTo(cx - 8f, cy + 8f)
        path.lineTo(cx - 4f, cy + 3f)
        path.lineTo(cx - 4f, cy + 8f)
        path.close()

        canvas.drawPath(path, buttonPaint)
    }

    private fun drawUndoButton(canvas: Canvas, cx: Float, cy: Float) {
        buttonPaint.color = if (canUndo) accentColor else 0x66FFFFFF.toInt()
        buttonPaint.style = Paint.Style.STROKE
        buttonPaint.strokeWidth = 3f

        // Curved arrow
        val path = Path()
        path.moveTo(cx - 8f, cy - 8f)
        path.quadTo(cx, cy - 8f, cx, cy)
        path.quadTo(cx, cy + 8f, cx + 8f, cy + 8f)
        path.moveTo(cx - 4f, cy - 8f)
        path.lineTo(cx - 4f, cy + 8f)
        path.moveTo(cx - 4f, cy - 8f)
        path.lineTo(cx - 8f, cy - 8f)
        path.moveTo(cx - 4f, cy + 8f)
        path.lineTo(cx - 8f, cy + 8f)

        canvas.drawPath(path, buttonPaint)
    }

    private fun drawRedoButton(canvas: Canvas, cx: Float, cy: Float) {
        buttonPaint.color = if (canRedo) accentColor else 0x66FFFFFF.toInt()
        buttonPaint.style = Paint.Style.STROKE
        buttonPaint.strokeWidth = 3f

        val path = Path()
        path.moveTo(cx + 8f, cy - 8f)
        path.quadTo(cx, cy - 8f, cx, cy)
        path.quadTo(cx, cy + 8f, cx - 8f, cy + 8f)
        path.moveTo(cx + 4f, cy - 8f)
        path.lineTo(cx + 4f, cy + 8f)
        path.moveTo(cx + 4f, cy - 8f)
        path.lineTo(cx + 8f, cy - 8f)
        path.moveTo(cx + 4f, cy + 8f)
        path.lineTo(cx + 8f, cy + 8f)

        canvas.drawPath(path, buttonPaint)
    }

    private fun drawExportButton(canvas: Canvas, cx: Float, cy: Float) {
        buttonPaint.color = accentColor
        buttonPaint.style = Paint.Style.FILL

        // Export icon
        val path = Path()
        path.moveTo(cx - 12f, cy - 10f)
        path.lineTo(cx, cy - 10f)
        path.lineTo(cx, cy)
        path.moveTo(cx + 16f, cy - 10f)
        path.lineTo(cx + 16f, cy + 10f)

        canvas.drawRect(cx - 10f, cy - 6f, cx + 8f, cy + 6f, buttonPaint)
        path.moveTo(cx - 10f, cy - 10f)
        path.lineTo(cx - 4f, cy - 10f)
        path.lineTo(cx - 4f, cy - 4f)
        path.lineTo(cx - 10f, cy - 4f)
        path.close()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = resolveSize(desiredWidth, widthMeasureSpec)
        val height = resolveSize(52, heightMeasureSpec)
        setMeasuredDimension(width, height)
    }

    private val desiredWidth = 400
}

/**
 * Toolbar button state wrapper.
 */
class ToolbarButton(context: Context) : View(context) {

    private var isEnabled = true

    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
        alpha = if (enabled) 1f else 0.4f
    }
}