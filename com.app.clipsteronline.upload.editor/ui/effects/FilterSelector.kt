package com.app.clipsteronline.upload.editor.ui.effects

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Filter selector with LUT previews.
 * Cinematic filters, categories, intensity.
 */
class FilterSelector(context: Context) : RecyclerView(context) {

    private var adapter: FilterAdapter? = null
    private var callback: ((FilterItem) -> Unit)? = null
    private var selectedFilter: String = "original"

    private val categories = listOf("All", "Warm", "Cool", "Mood", "B&W")
    
    private val filters = listOf(
        FilterItem("Original", "original", "All"),
        FilterItem("Warm", "warm", "Warm"),
        FilterItem("Cool", "cool", "Cool"),
        FilterItem("Fade", "fade", "Mood"),
        FilterItem("Soft", "soft", "Mood"),
        FilterItem("Vivid", "vivid", "Mood"),
        FilterItem("Dramatic", "dramatic", "Mood"),
        FilterItem("Mono", "mono", "B&W"),
        FilterItem("Sepia", "sepia", "B&W"),
        FilterItem("Noir", "noir", "B&W")
    )

    init {
        layoutManager = GridLayoutManager(context, 5)
        adapter = FilterAdapter(filters)
        this.adapter = adapter
    }

    /**
     * Set filter callback.
     */
    fun setOnFilterSelectedListener(callback: (FilterItem) -> Unit) {
        this.callback = callback
    }

    /**
     * Set selected filter.
     */
    fun setSelectedFilter(filterId: String) {
        selectedFilter = filterId
        adapter?.notifyDataSetChanged()
    }

    /**
     * Filter item data.
     */
    data class FilterItem(val name: String, val id: String, val category: String)
}

/**
 * Filter adapter.
 */
class FilterAdapter(private val filters: List<FilterSelector.FilterItem>) :
    RecyclerView.Adapter<FilterAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = FilterItemView(parent.context)
        view.layoutParams = ViewGroup.LayoutParams(80, 100)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(filters[position])
    }

    override fun getItemCount() = filters.size

    inner class ViewHolder(val view: FilterItemView) : RecyclerView.ViewHolder(view) {
        fun bind(item: FilterSelector.FilterItem) {
            view.setFilter(item)
        }
    }
}

/**
 * Filter item view.
 */
class FilterItemView(context: Context) : View(context) {

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val thumbPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var filter: FilterSelector.FilterItem? = null
    private var isSelected = false

    private val accentColor = 0xFFFF6B35.toInt()

    fun setFilter(filter: FilterSelector.FilterItem) {
        this.filter = filter
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        val filterItem = filter ?: return

        // Background
        bgPaint.color = 0xFF333333.toInt()
        bgPaint.style = Paint.Style.FILL
        canvas.drawRoundRect(0f, 0f, w, h, 8f, 8f, bgPaint)

        // Thumbnail gradient
        thumbPaint.shader = android.graphics.LinearGradient(
            0f, 0f, w, h,
            getFilterColors(filterItem.id),
            null,
            android.graphics.Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(4f, 4f, w - 4f, h - 24f, 6f, 6f, thumbPaint)

        // Selection border
        if (isSelected) {
            bgPaint.shader = null
            bgPaint.style = Paint.Style.STROKE
            bgPaint.strokeWidth = 3f
            bgPaint.color = accentColor
            canvas.drawRoundRect(0f, 0f, w, h, 8f, 8f, bgPaint)
        }

        // Label
        labelPaint.color = 0xFFFFFFFF.toInt()
        labelPaint.textSize = 20f
        labelPaint.textAlign = Paint.Align.CENTER
        canvas.drawText(filterItem.name.take(6), w / 2, h - 6f, labelPaint)
    }

    private fun getFilterColors(filterId: String): IntArray {
        return when (filterId) {
            "warm" -> intArrayOf(0xFFFF8800.toInt(), 0xFFFF4400.toInt())
            "cool" -> intArrayOf(0xFF0088FF.toInt(), 0xFF0044FF.toInt())
            "fade" -> intArrayOf(0xFFAAAA99.toInt(), 0xFF665544.toInt())
            "soft" -> intArrayOf(0xFFDDCCBB.toInt(), 0xFFAABBDD.toInt())
            "mono" -> intArrayOf(0xFF444444.toInt(), 0xFF888888.toInt())
            else -> intArrayOf(0xFF666666.toInt(), 0xFF333333.toInt())
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(80, 100)
    }
}