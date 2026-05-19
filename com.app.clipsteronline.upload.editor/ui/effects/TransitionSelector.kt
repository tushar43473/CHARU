package com.app.clipsteronline.upload.editor.ui.effects

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Transition selector.
 * Animated previews, duration controls.
 */
class TransitionSelector(context: Context) : RecyclerView(context) {

    private var adapter: TransitionAdapter? = null
    private var callback: ((TransitionItem) -> Unit)? = null
    private var selectedTransition: String = "none"
    private var transitionDuration: Long = 500L

    private val transitions = listOf(
        TransitionItem("None", "none", 0),
        TransitionItem("Cross", "cross", 500),
        TransitionItem("Fade", "fade", 500),
        TransitionItem("Slide", "slide", 400),
        TransitionItem("Push", "push", 400),
        TransitionItem("Wipe", "wipe", 500),
        TransitionItem("Zoom", "zoom", 300),
        TransitionItem("Blur", "blur", 500)
    )

    init {
        layoutManager = LinearLayoutManager(context, HORIZONTAL, false)
        adapter = TransitionAdapter(transitions)
        this.adapter = adapter

        setPadding(24, 0, 24, 0)
        clipToPadding = false
    }

    /**
     * Set callback.
     */
    fun setOnTransitionSelectedListener(callback: (TransitionItem) -> Unit) {
        this.callback = callback
    }

    /**
     * Set selected transition.
     */
    fun setSelectedTransition(id: String) {
        selectedTransition = id
        adapter?.notifyDataSetChanged()
    }

    /**
     * Set transition duration.
     */
    fun setDuration(ms: Long) {
        transitionDuration = ms.coerceIn(100, 2000)
    }

    /**
     * Transition item data.
     */
    data class TransitionItem(
        val name: String,
        val id: String,
        val defaultDuration: Long
    )
}

/**
 * Transition adapter.
 */
class TransitionAdapter(private val transitions: List<TransitionSelector.TransitionItem>) :
    RecyclerView.Adapter<TransitionAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = TransitionItemView(parent.context)
        view.layoutParams = ViewGroup.LayoutParams(80, 80)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(transitions[position])
    }

    override fun getItemCount() = transitions.size

    inner class ViewHolder(val view: TransitionItemView) : RecyclerView.ViewHolder(view)
}

/**
 * Transition item view.
 */
class TransitionItemView(context: Context) : View(context) {

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var transition: TransitionSelector.TransitionItem? = null
    private var selected = false

    private val accentColor = 0xFFFF6B35.toInt()

    fun setTransition(transition: TransitionSelector.TransitionItem) {
        this.transition = transition
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        val trans = transition ?: return

        // Background
        bgPaint.color = if (trans.id == "none") 0xFF2A2A2A.toInt() else 0xFF333333.toInt()
        bgPaint.style = Paint.Style.FILL
        canvas.drawRoundRect(0f, 0f, w, h - 20f, 8f, 8f, bgPaint)

        // Transition icon (animated preview)
        if (trans.id != "none") {
            drawTransitionIcon(canvas, trans.id, w / 2, h / 2 - 10f)
        }

        // Selection border
        bgPaint.style = Paint.Style.STROKE
        bgPaint.strokeWidth = if (selected) 3f else 1f
        bgPaint.color = if (selected) accentColor else 0xFF444444.toInt()
        canvas.drawRoundRect(0f, 0f, w, h - 20f, 8f, 8f, bgPaint)

        // Label
        labelPaint.color = 0xFFFFFFFF.toInt()
        labelPaint.textSize = 18f
        labelPaint.textAlign = Paint.Align.CENTER
        canvas.drawText(trans.name, w / 2, h - 4f, labelPaint)
    }

    private fun drawTransitionIcon(canvas: Canvas, id: String, cx: Float, cy: Float) {
        iconPaint.style = Paint.Style.STROKE
        iconPaint.strokeWidth = 2f
        iconPaint.color = 0xFFFF6B35.toInt()

        when (id) {
            "cross" -> {
                // Overlapping squares
                canvas.drawRect(cx - 10f, cy - 10f, cx - 2f, cy + 2f, iconPaint)
                canvas.drawRect(cx + 2f, cy - 2f, cx + 10f, cy + 10f, iconPaint)
            }
            "fade" -> {
                // Fading circles
                iconPaint.alpha = 150
                canvas.drawCircle(cx, cy, 10f, iconPaint)
                iconPaint.alpha = 80
                canvas.drawCircle(cx, cy, 14f, iconPaint)
            }
            "slide" -> {
                // Sliding rectangles
                canvas.drawRect(cx - 12f, cy - 8f, cx - 2f, cy + 8f, iconPaint)
                canvas.drawRect(cx + 2f, cy - 8f, cx + 12f, cy + 8f, iconPaint)
            }
            "wipe" -> {
                // Reveal lines
                iconPaint.strokeWidth = 3f
                canvas.drawLine(cx - 10f, cy + 10f, cx - 10f, cy - 10f, iconPaint)
                canvas.drawLine(cx, cy + 10f, cx, cy - 10f, iconPaint)
                canvas.drawLine(cx + 10f, cy + 10f, cx + 10f, cy - 10f, iconPaint)
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(80, 80)
    }
}