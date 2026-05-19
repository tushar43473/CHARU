package com.app.clipsteronline.upload.editor.ui.effects

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * LUT preview adapter.
 * Render bitmap previews, cached thumbnails.
 */
class LUTPreviewAdapter : RecyclerView.Adapter<LUTPreviewAdapter.ViewHolder>() {

    private val lutFiles = mutableListOf<LUTPreview>()
    private var selectedIndex = 0

    /**
     * Add LUT preview.
     */
    fun addLUT(lut: LUTPreview) {
        val index = lutFiles.size
        lutFiles.add(lut)
        notifyItemInserted(index)
    }

    /**
     * Set selected index.
     */
    fun setSelected(index: Int) {
        val old = selectedIndex
        selectedIndex = index.coerceIn(0, lutFiles.size - 1)
        notifyItemChanged(old)
        notifyItemChanged(selectedIndex)
    }

    /**
     * Get LUT at position.
     */
    fun getLUT(position: Int): LUTPreview? = lutFiles.getOrNull(position)

    /**
     * Clear previews.
     */
    fun clear() {
        val size = lutFiles.size
        lutFiles.clear()
        notifyItemRangeRemoved(0, size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LUTPreviewItemView(parent.context)
        view.layoutParams = ViewGroup.LayoutParams(90, 110)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(lutFiles[position], position == selectedIndex)
    }

    override fun getItemCount() = lutFiles.size

    internal class ViewHolder(val view: LUTPreviewItemView) : RecyclerView.ViewHolder(view)
}

/**
 * LUT preview data.
 */
data class LUTPreview(
    val id: String,
    val name: String,
    val thumbnail: Bitmap? = null
)

/**
 * LUT preview item view.
 */
class LUTPreviewItemView(context: Context) : View(context) {

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val thumbPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var lutPreview: LUTPreview? = null
    private var selected = false
    private var isLoading = true

    private val accentColor = 0xFFFF6B35.toInt()
    private val cache = mutableMapOf<String, Bitmap>()

    fun setLUT(lut: LUTPreview) {
        lutPreview = lut
        isLoading = lut.thumbnail == null
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()

        // Background
        bgPaint.color = 0xFF2A2A2A.toInt()
        bgPaint.style = Paint.Style.FILL
        canvas.drawRoundRect(0f, 0f, w, h, 8f, 8f, bgPaint)

        // Thumbnail
        if (isLoading) {
            // Loading placeholder
            bgPaint.color = 0xFF444444.toInt()
            canvas.drawRoundRect(4f, 4f, w - 4f, h - 28f, 6f, 6f, bgPaint)

            // Loading indicator
            bgPaint.color = 0xFF555555.toInt()
            canvas.drawCircle(w / 2, h / 2 - 8, 8f, bgPaint)
        } else {
            lutPreview?.thumbnail?.let { bm ->
                canvas.drawBitmap(bm, null, RectF(4f, 4f, w - 4f, h - 28f), thumbPaint)
            }
        }

        // Selection border
        if (selected) {
            borderPaint.color = accentColor
            borderPaint.style = Paint.Style.STROKE
            borderPaint.strokeWidth = 3f
            canvas.drawRoundRect(0f, 0f, w, h, 8f, 8f, borderPaint)
        } else {
            borderPaint.color = 0xFF555555.toInt()
            borderPaint.style = Paint.Style.STROKE
            borderPaint.strokeWidth = 1f
            canvas.drawRoundRect(0f, 0f, w, h, 8f, 8f, borderPaint)
        }

        // Label
        labelPaint.color = 0xFFFFFFFF.toInt()
        labelPaint.textSize = 20f
        labelPaint.textAlign = Paint.Align.CENTER
        canvas.drawText(lutPreview?.name ?: "", w / 2, h - 6f, labelPaint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(90, 110)
    }
}

/**
 * Preview cache manager.
 */
class PreviewCacheManager(private val maxSize: Int = 20) {

    private val cache = android.util.LruCache<String, Bitmap>(maxSize)

    /**
     * Get cached preview.
     */
    fun get(key: String): Bitmap? = cache.get(key)

    /**
     * Put preview in cache.
     */
    fun put(key: String, bitmap: Bitmap) {
        cache.put(key, bitmap)
    }

    /**
     * Clear cache.
     */
    fun clear() {
        cache.evictAll()
    }

    /**
     * Remove preview.
     */
    fun remove(key: String) {
        cache.remove(key)
    }
}