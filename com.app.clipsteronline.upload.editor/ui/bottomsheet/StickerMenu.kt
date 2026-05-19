package com.app.clipsteronline.upload.editor.ui.bottomsheet

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import android.widget.LinearLayout

/**
 * Bottom sticker menu.
 * Categories, animated stickers, recent stickers.
 */
class StickerMenu(context: Context) : LinearLayout(context) {

    private var stickerCallback: ((StickerAction) -> Unit)? = null

    private val categories = listOf(
        StickerCategory("Trending", "trending"),
        StickerCategory("Emoji", "emoji"),
        StickerCategory("Texts", "texts"),
        StickerCategory("Happy", "happy"),
        StickerCategory("Deco", "deco"),
        StickerCategory("Recent", "recent")
    )

    /**
     * Set sticker callback.
     */
    fun setOnStickerSelectedListener(callback: (StickerAction) -> Unit) {
        stickerCallback = callback
    }

    /**
     * Sticker action data.
     */
    data class StickerAction(
        val type: String,
        val stkId: String = ""
    )

    /**
     * Sticker category data.
     */
    data class StickerCategory(val name: String, val id: String)
}

/**
 * Sticker preview thumbnail.
 */
class StickerThumbnail(context: Context) : View(context) {

    private val thumbnailPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var stickerId: String = ""
    private var label: String = ""

    private val accentColor = 0xFFFF6B35.toInt()
    private val bgColor = 0xFF333333.toInt()

    fun setSticker(id: String, name: String) {
        stickerId = id
        label = name
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()

        // Thumbnail background
        thumbnailPaint.color = bgColor
        thumbnailPaint.style = Paint.Style.FILL
        canvas.drawRoundRect(0f, 0f, w, h, 8f, 8f, thumbnailPaint)

        // Border
        thumbnailPaint.style = Paint.Style.STROKE
        thumbnailPaint.strokeWidth = 2f
        thumbnailPaint.color = 0xFF555555.toInt()
        canvas.drawRoundRect(0f, 0f, w, h, 8f, 8f, thumbnailPaint)

        // Sticker icon placeholder
        thumbnailPaint.color = accentColor
        thumbnailPaint.style = Paint.Style.STROKE
        thumbnailPaint.strokeWidth = 3f
        canvas.drawCircle(w / 2, h / 2 - 12, 16f, thumbnailPaint)

        // Label
        if (label.isNotEmpty()) {
            labelPaint.color = 0xFFCCCCCC.toInt()
            labelPaint.textSize = 24f
            labelPaint.textAlign = Paint.Align.CENTER
            canvas.drawText(label.take(8), w / 2, h - 8f, labelPaint)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = resolveSize(80, widthMeasureSpec)
        val height = resolveSize(80, heightMeasureSpec)
        setMeasuredDimension(width, height)
    }

    private val desiredWidth = 80
    private val desiredHeight = 80
}