package com.app.clipsteronline.upload.editor.ui.toolbar

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.view.View

/**
 * Selection toolbar.
 * Delete, duplicate, split, group, lock actions.
 */
class SelectionToolbar(context: Context) : View(context) {

    private val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var selectionCallback: ((SelectionAction) -> Unit)? = null
    private var selectedCount = 0

    private val accentColor = 0xFFFF6B35.toInt()

    private val actions = listOf(
        SelectionAction("Delete", "delete"),
        SelectionAction("Duplicate", "duplicate"),
        SelectionAction("Split", "split"),
        SelectionAction("Group", "group"),
        SelectionAction("Lock", "lock"),
        SelectionAction("Copy", "copy")
    )

    /**
     * Set selection callback.
     */
    fun setOnSelectionActionListener(callback: (SelectionAction) -> Unit) {
        selectionCallback = callback
    }

    /**
     * Set selected count.
     */
    fun setSelectedCount(count: Int) {
        selectedCount = count.coerceAtLeast(0)
        invalidate()
    }

    /**
     * Trigger action.
     */
    fun triggerAction(action: String) {
        selectionCallback?.invoke(SelectionAction(action, action))
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()

        val actionSpacing = 48f
        val startX = (w - actions.size * actionSpacing) / 2

        for ((index, action) in actions.withIndex()) {
            val cx = startX + index * actionSpacing
            drawActionIcon(canvas, cx, h / 2, action.label)
        }
    }

    private fun drawActionIcon(canvas: Canvas, cx: Float, cy: Float, label: String) {
        val enabled = selectedCount > 0 || label == "Copy"

        iconPaint.color = when (label) {
            "Delete" -> 0xFFFF4444.toInt()
            "Lock" -> if (selectedCount > 0) accentColor else 0xFF666666.toInt()
            else -> if (enabled) 0xFFFFFFFF.toInt() else 0xFF666666.toInt()
        }

        iconPaint.style = Paint.Style.STROKE
        iconPaint.strokeWidth = 2f

        when (label) {
            "Delete" -> drawDeleteIcon(canvas, cx, cy)
            "Duplicate" -> drawDuplicateIcon(canvas, cx, cy)
            "Split" -> drawSplitIcon(canvas, cx, cy)
            "Group" -> drawGroupIcon(canvas, cx, cy)
            "Lock" -> drawLockIcon(canvas, cx, cy)
            "Copy" -> drawCopyIcon(canvas, cx, cy)
        }
    }

    private fun drawDeleteIcon(canvas: Canvas, cx: Float, cy: Float) {
        val path = Path()
        path.moveTo(cx - 8f, cy - 8f)
        path.lineTo(cx + 8f, cy - 8f)
        path.lineTo(cx + 8f, cy + 6f)
        path.lineTo(cx - 8f, cy + 6f)
        path.close()
        path.moveTo(cx, cy - 10f)
        path.lineTo(cx, cy - 4f)
        canvas.drawPath(path, iconPaint)
    }

    private fun drawDuplicateIcon(canvas: Canvas, cx: Float, cy: Float) {
        val offset = 8f
        iconPaint.style = Paint.Style.FILL
        canvas.drawRect(cx - 6f, cy - 6f, cx + 2f, cy + 6f, iconPaint)
        canvas.drawRect(cx + 2f, cy - offset + 6f, cx + 10f, cy + offset - 6f, iconPaint)
    }

    private fun drawSplitIcon(canvas: Canvas, cx: Float, cy: Float) {
        iconPaint.style = Paint.Style.STROKE
        canvas.drawLine(cx - 6f, cy, cx - 2f, cy, iconPaint)
        canvas.drawLine(cx + 2f, cy, cx + 6f, cy, iconPaint)
        canvas.drawLine(cx, cy - 8f, cx, cy + 8f, iconPaint)
    }

    private fun drawGroupIcon(canvas: Canvas, cx: Float, cy: Float) {
        canvas.drawRect(cx - 8f, cy - 8f, cx + 4f, cy + 8f, iconPaint)
        canvas.drawRect(cx - 4f, cy - 4f, cx + 8f, cy + 4f, iconPaint)
    }

    private fun drawLockIcon(canvas: Canvas, cx: Float, cy: Float) {
        canvas.drawRect(cx - 6f, cy - 2f, cx + 6f, cy + 6f, iconPaint)
        canvas.drawLine(cx - 4f, cy - 2f, cx - 4f, cy + 2f, iconPaint)
    }

    private fun drawCopyIcon(canvas: Canvas, cx: Float, cy: Float) {
        iconPaint.style = Paint.Style.STROKE
        canvas.drawLine(cx - 6f, cy - 6f, cx - 2f, cy - 6f, iconPaint)
        canvas.drawLine(cx - 6f, cy - 6f, cx - 6f, cy + 2f, iconPaint)
        canvas.drawLine(cx - 6f, cy + 2f, cx - 2f, cy + 2f, iconPaint)
        canvas.drawLine(cx + 2f, cy - 6f, cx + 6f, cy - 6f, iconPaint)
        canvas.drawLine(cx + 6f, cy - 6f, cx + 6f, cy + 6f, iconPaint)
        canvas.drawLine(cx + 6f, cy + 6f, cx + 2f, cy + 6f, iconPaint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(width, resolveSize(300, widthMeasureSpec))
    }

    /**
     * Selection action data.
     */
    data class SelectionAction(val label: String, val type: String)
}