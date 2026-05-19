package com.app.clipsteronline.upload.editor.ui.bottomsheet

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView

/**
 * Bottom text menu.
 * Add text, font, color, animation.
 */
class TextMenu(context: Context) : LinearLayout(context) {

    private var textCallback: ((TextAction) -> Unit)? = null

    private val presets = listOf(
        TextPreset("Classic", "classic"),
        TextPreset("Modern", "modern"),
        TextPreset("Meme", "meme"),
        TextPreset("Title", "title"),
        TextPreset("Subtitle", "subtitle"),
        TextPreset("Caption", "caption")
    )

    /**
     * Set text callback.
     */
    fun setOnTextActionListener(callback: (TextAction) -> Unit) {
        textCallback = callback
    }

    /**
     * Text action data.
     */
    data class TextAction(
        val type: String,
        val value: String = ""
    )

    /**
     * Text preset data.
     */
    data class TextPreset(val name: String, val style: String)
}

/**
 * Text style picker.
 */
class TextStylePicker(context: Context) : View(context) {

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var selectedStyle: String = "classic"

    private val styles = listOf(
        "Classic", "Modern", "Meme", "Title", 
        "Subtitle", "Caption", "Watermark"
    )

    fun setSelectedStyle(style: String) {
        selectedStyle = style
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()

        val selectedIndex = styles.indexOf(selectedStyle).coerceAtLeast(0)
        val itemWidth = w / styles.size

        for ((index, style) in styles.withIndex()) {
            val x = index * itemWidth
            val isSelected = style == selectedStyle

            textPaint.color = if (isSelected) 0xFFFF6B35.toInt() else 0xFFFFFFFF.toInt()
            textPaint.textSize = 28f
            textPaint.textAlign = Paint.Align.CENTER
            textPaint.isFakeBoldText = isSelected

            canvas.drawText(style, x + itemWidth / 2, h / 2, textPaint)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = resolveSize(desiredWidth, widthMeasureSpec)
        val height = resolveSize(60, heightMeasureSpec)
        setMeasuredDimension(width, height)
    }

    private val desiredWidth = 300
}