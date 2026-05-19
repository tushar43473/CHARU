package com.app.clipsteronline.upload.editor.ui.bottomsheet

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import android.widget.LinearLayout

/**
 * Bottom filter menu.
 * LUTs, brightness, contrast, saturation, sharpen.
 */
class FilterMenu(context: Context) : LinearLayout(context) {

    private var filterCallback: ((FilterAction) -> Unit)? = null
    private var selectedFilter: String? = null
    private var brightness: Float = 0f
    private var contrast: Float = 1f
    private var saturation: Float = 1f
    private var sharpen: Float = 0f

    private val filters = listOf(
        FilterInfo("Original", "original"),
        FilterInfo("Warm", "warm"),
        FilterInfo("Cool", "cool"),
        FilterInfo("Fade", "fade"),
        FilterInfo("Soft", "soft"),
        FilterInfo("Vivid", "vivid"),
        FilterInfo("Dramatic", "dramatic"),
        FilterInfo("Mono", "mono")
    )

    /**
     * Set filter callback.
     */
    fun setOnFilterSelectedListener(callback: (FilterAction) -> Unit) {
        filterCallback = callback
    }

    /**
     * Set selected filter.
     */
    fun setSelectedFilter(filter: String) {
        selectedFilter = filter
        invalidate()
    }

    /**
     * Filter action data.
     */
    data class FilterAction(
        val type: String,
        val value: Float = 0f
    )

    /**
     * Filter info data.
     */
    data class FilterInfo(val name: String, val id: String)
}

/**
 * Filter adjustment sliders.
 */
class FilterAdjustmentsView(context: Context) : View(context) {

    private val sliderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var sliders = mutableListOf<SliderData>()

    data class SliderData(
        val label: String,
        var value: Float,
        var min: Float = 0f,
        var max: Float = 1f
    )

    fun addSlider(label: String, defaultValue: Float, min: Float = 0f, max: Float = 1f) {
        sliders.add(SliderData(label, defaultValue, min, max))
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val h = height.toFloat()
        val sliderH = h / sliders.size

        for ((index, slider) in sliders.withIndex()) {
            val y = index * sliderH + sliderH / 2

            labelPaint.color = 0xFFFFFFFF.toInt()
            labelPaint.textSize = 28f
            labelPaint.textAlign = Paint.Align.LEFT
            canvas.drawText(slider.label, 16f, y + 8f, labelPaint)

            // Simple slider bar
            sliderPaint.color = 0xFF333333.toInt()
            canvas.drawRect(100f, y - 4f, width - 16f, y + 4f, sliderPaint)

            sliderPaint.color = 0xFFFF6B35.toInt()
            val progress = (slider.value - slider.min) / (slider.max - slider.min)
            canvas.drawRect(100f, y - 4f, 100f + (width - 116f) * progress, y + 4f, sliderPaint)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desired = (sliders.size * 48)
        setMeasuredDimension(width, desired)
    }
}