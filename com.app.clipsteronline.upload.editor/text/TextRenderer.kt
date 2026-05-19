package com.app.clipsteronline.upload.editor.text

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.util.UUID

/**
 * Renders text on canvas with style.
 * Handles fonts, colors, shadows, and animations.
 */
class TextRenderer(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        isFakeBoldText = false
    }

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var defaultFont = "sans-serif"
    private var defaultTextSize = 48f

    /**
     * Render text layer.
     */
    fun renderText(layer: TextLayer, canvas: Canvas, width: Int, height: Int) {
        if (!layer.isVisible) return

        val posX = (layer.x * width).toInt()
        val posY = (layer.y * height).toInt()

        val textSize = defaultTextSize * layer.scale
        textPaint.textSize = textSize
        textPaint.alpha = (layer.alpha * 255).toInt()

        // Apply style
        applyStyle(layer.style, textPaint)
        
        // Draw shadow
        if (layer.style.hasShadow()) {
            drawShadow(layer, canvas, posX, posY)
        }

        // Draw background
        if (layer.style.hasBackground()) {
            drawBackground(layer, canvas, posX, posY)
        }

        // Draw text
        val lines = layer.text.split("\n")
        var currentY = posY - (textSize / 3)

        for (line in lines) {
            canvas.drawText(line, posX.toFloat(), currentY, textPaint)
            currentY += textSize * 1.2f
        }
    }

    /**
     * Apply text style.
     */
    fun applyStyle(style: TextStyle, paint: Paint) {
        when (style) {
            TextStyle.DEFAULT -> {
                paint.typeface = Typeface.create(defaultFont, Typeface.NORMAL)
                paint.textSize = defaultTextSize
                paint.color = 0xFFFFFFFF.toInt()
            }
            TextStyle.TITLE -> {
                paint.typeface = Typeface.create(defaultFont, Typeface.BOLD)
                paint.textSize = defaultTextSize * 1.5f
                paint.color = 0xFFFFFFFF.toInt()
            }
            TextStyle.SUBTITLE -> {
                paint.typeface = Typeface.create(defaultFont, Typeface.ITALIC)
                paint.textSize = defaultTextSize * 1.2f
                paint.color = 0xFFFFFFFF.toInt()
            }
            TextStyle.CAPTION -> {
                paint.typeface = Typeface.create(defaultFont, Typeface.NORMAL)
                paint.textSize = defaultTextSize * 0.8f
                paint.color = 0xFFFFFFFF.toInt()
            }
            TextStyle.WATERMARK -> {
                paint.typeface = Typeface.create(defaultFont, Typeface.NORMAL)
                paint.textSize = defaultTextSize * 0.6f
                paint.color = 0x80FFFFFF.toInt()
            }
            TextStyle.MEME -> {
                paint.typeface = Typeface.create("arial", Typeface.BOLD)
                paint.textSize = defaultTextSize * 2f
                paint.color = 0xFF000000.toInt()
                paint.strokeWidth = 3f
                paint.style = Paint.Style.STROKE
            }
            else -> {}
        }
    }

    /**
     * Draw shadow.
     */
    private fun drawShadow(layer: TextLayer, canvas: Canvas, x: Int, y: Int) {
        shadowPaint.apply {
            textSize = textPaint.textSize
            color = 0x80000000.toInt()
            typeface = textPaint.typeface
            textAlign = textPaint.textAlign
        }

        canvas.drawText(layer.text, x + 3f, y + 3f, shadowPaint)
    }

    /**
     * Draw background.
     */
    private fun drawBackground(layer: TextLayer, canvas: Canvas, x: Int, y: Int) {
        val bounds = getTextBounds(layer, x, y)
        backgroundPaint.color = 0xCC000000.toInt()

        canvas.drawRect(bounds.first.toFloat(), bounds.second.toFloat(), 
                     bounds.third.toFloat(), bounds.fourth.toFloat(), backgroundPaint)
    }

    /**
     * Get text bounds.
     */
    private fun getTextBounds(layer: TextLayer, x: Int, y: Int): Tuple4<Int> {
        val width = (layer.text.length * textPaint.textSize * 0.6f).toInt()
        val height = textPaint.textSize.toInt()

        return Tuple4(x - width / 2, y - height, x + width / 2, y + height / 2)
    }

    /**
     * Set default font.
     */
    fun setDefaultFont(font: String) {
        defaultFont = font
    }

    /**
     * Set default text size.
     */
    fun setDefaultTextSize(size: Float) {
        defaultTextSize = size
    }

    /**
     * Set text color.
     */
    fun setTextColor(color: Int) {
        textPaint.color = color
    }
}

/**
 * Four-element tuple.
 */
data class Tuple4<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

/**
 * Extension for TextStyle.
 */
private fun TextStyle.hasShadow(): Boolean = when (this) {
    TextStyle.TITLE, TextStyle.SUBTITLE, TextStyle.CAPTION -> true
    else -> false
}

private fun TextStyle.hasBackground(): Boolean = when (this) {
    TextStyle.CAPTION, TextStyle.MEME -> true
    else -> false
}