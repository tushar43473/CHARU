package com.app.clipsteronline.upload.editor.ui.effects

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Effect picker with horizontal scrolling.
 * Realtime previews, category switching.
 */
class EffectPicker(context: Context) : RecyclerView(context) {

    private var adapter: EffectAdapter? = null
    private var effectCallback: ((EffectItem) -> Unit)? = null
    private var selectedEffect: String = "none"

    private val effects = listOf(
        EffectItem("None", "none"),
        EffectItem("Blur", "blur"),
        EffectItem("Glitch", "glitch"),
        EffectItem("VHS", "vhs"),
        EffectItem("Retro", "retro"),
        EffectItem("Sparkle", "sparkle"),
        EffectItem("Neon", "neon"),
        EffectItem("Pixelate", "pixelate"),
        EffectItem("Shake", "shake"),
        EffectItem("Zoom", "zoom")
    )

    init {
        layoutManager = LinearLayoutManager(context, HORIZONTAL, false)
        adapter = EffectAdapter(effects)
        this.adapter = adapter

        setPadding(32, 0, 32, 0)
        clipToPadding = false
    }

    /**
     * Set effect callback.
     */
    fun setOnEffectSelectedListener(callback: (EffectItem) -> Unit) {
        effectCallback = callback
    }

    /**
     * Set selected effect.
     */
    fun setSelectedEffect(effectId: String) {
        selectedEffect = effectId
        adapter?.notifyDataSetChanged()
    }

    /**
     * Effect item data.
     */
    data class EffectItem(val name: String, val id: String)
}

/**
 * Effect adapter.
 */
class EffectAdapter(private val effects: List<EffectPicker.EffectItem>) : 
    RecyclerView.Adapter<EffectAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = EffectItemView(parent.context)
        view.layoutParams = ViewGroup.LayoutParams(100, ViewGroup.LayoutParams.MATCH_PARENT)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(effects[position])
    }

    override fun getItemCount() = effects.size

    inner class ViewHolder(val view: EffectItemView) : RecyclerView.ViewHolder(view) {
        fun bind(item: EffectPicker.EffectItem) {
            view.setEffect(item)
        }
    }
}

/**
 * Effect item view.
 */
class EffectItemView(context: Context) : View(context) {

    private val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var effect: EffectPicker.EffectItem? = null
    private var isSelected = false

    private val accentColor = 0xFFFF6B35.toInt()

    fun setEffect(effect: EffectPicker.EffectItem) {
        this.effect = effect
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        val effectItem = effect ?: return

        // Card background
        cardPaint.style = Paint.Style.FILL

        if (effectItem.id == "none") {
            cardPaint.color = 0xFF2A2A2A.toInt()
        } else {
            cardPaint.color = 0xFF333333.toInt()
        }

        canvas.drawRoundRect(4f, 4f, w - 4f, h - 4f, 12f, 12f, cardPaint)

        // Thumbnail placeholder
        cardPaint.color = 0xFF444444.toInt()
        canvas.drawRoundRect(12f, 12f, w - 12f, h - 32f, 8f, 8f, cardPaint)

        // Selection border
        if (isSelected) {
            cardPaint.style = Paint.Style.STROKE
            cardPaint.strokeWidth = 3f
            cardPaint.color = accentColor
            canvas.drawRoundRect(4f, 4f, w - 4f, h - 4f, 12f, 12f, cardPaint)
        }

        // Label
        labelPaint.color = 0xFFFFFFFF.toInt()
        labelPaint.textSize = 24f
        labelPaint.textAlign = Paint.Align.CENTER
        canvas.drawText(effectItem.name, w / 2, h - 8f, labelPaint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(100, resolveSize(80, heightMeasureSpec))
    }
}