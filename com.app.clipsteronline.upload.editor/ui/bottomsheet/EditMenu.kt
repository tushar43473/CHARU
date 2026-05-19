package com.app.clipsteronline.upload.editor.ui.bottomsheet

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.view.View
import android.widget.LinearLayout

/**
 * Bottom edit menu for trim/split/rotate/crop.
 * Horizontal scrollable edit actions.
 */
class EditMenu(context: Context) : LinearLayout(context) {

    private var editCallback: ((EditAction) -> Unit)? = null

    private val actions = listOf(
        EditAction("Trim", "trim"),
        EditAction("Split", "split"),
        EditAction("Copy", "copy"),
        EditAction("Rotate", "rotate"),
        EditAction("Crop", "crop"),
        EditAction("Delete", "delete"),
        EditAction("Duplicate", "duplicate"),
        EditAction("Reverse", "reverse")
    )

    /**
     * Set edit callback.
     */
    fun setOnEditActionListener(callback: (EditAction) -> Unit) {
        editCallback = callback
    }

    /**
     * Notify selection changed.
     */
    fun onSelectionChanged(hasSelection: Boolean) {
        // Enable/disable actions based on selection
    }

    /**
     * Edit action data.
     */
    data class EditAction(val name: String, val type: String)
}

/**
 * Edit menu item view.
 */
class EditMenuItemView(context: Context) : View(context) {

    private val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var label = "Action"
    private var isSelected = false

    private val accentColor = 0xFFFF6B35.toInt()

    fun setLabel(text: String) {
        label = text
        invalidate()
    }

    fun setSelected(selected: Boolean) {
        isSelected = selected
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()

        // Background
        if (isSelected) {
            iconPaint.color = accentColor
            iconPaint.alpha = 30
            canvas.drawRoundRect(0f, 0f, w, h, 12f, 12f, iconPaint)
        }

        // Icon placeholder
        iconPaint.color = if (isSelected) accentColor else 0xFFFFFFFF.toInt()
        iconPaint.style = Paint.Style.STROKE
        iconPaint.strokeWidth = 2f

        canvas.drawCircle(w / 2, h / 2 - 8, 12f, iconPaint)

        // Label
        labelPaint.color = if (isSelected) accentColor else 0xFFAAAAAA.toInt()
        labelPaint.textSize = 24f
        labelPaint.textAlign = Paint.Align.CENTER
        canvas.drawText(label, w / 2, h / 2 + 16, labelPaint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = resolveSize(80, widthMeasureSpec)
        val height = resolveSize(80, heightMeasureSpec)
        setMeasuredDimension(width, height)
    }

    private val desiredWidth = 80
    private val desiredHeight = 80
}