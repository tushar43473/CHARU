package com.app.clipsteronline.upload.editor.ui.bottomsheet

import android.animation.ValueAnimator
import android.animation.ObjectAnimator
import android.animation.FloatProperty
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout

/**
 * Bottom menu container.
 * Horizontal switching, animated transitions, sheet behavior.
 */
class BottomMenuContainer(context: Context) : FrameLayout(context) {

    private val menus = mutableMapOf<String, View>()
    private var currentMenuKey: String = ""
    private var isExpanded = true
    private var collapsedHeight = 48f
    private var expandedHeight = 240f

    private val sheetPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val handlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val gestureDetector: GestureDetector

    private var menuCallback: ((String) -> Unit)? = null

    init {
        setWillNotDraw(false)

        gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (kotlin.math.abs(velocityY) > kotlin.math.abs(velocityX)) {
                    if (velocityY < -ViewConfiguration.get(context).scaledMinimumFlingVelocity) {
                        expand()
                    } else if (velocityY > ViewConfiguration.get(context).scaledMinimumFlingVelocity) {
                        collapse()
                    }
                }
                return true
            }
        })
    }

    /**
     * Addmenu.
     */
    fun addMenu(key: String, view: View) {
        menus[key] = view
        addView(view)
        showMenu(key)
    }

    /**
     * Show menu.
     */
    fun showMenu(key: String) {
        if (!menus.containsKey(key)) return

        // Hide current
        menus.values.forEach { it.visibility = View.GONE }

        // Show new
        currentMenuKey = key
        menus[key]?.let {
            it.visibility = View.VISIBLE
            it.layoutParams = layoutParams
        }

        invalidate()
    }

    /**
     * Expand.
     */
    fun expand() {
        animateHeight(expandedHeight)
        isExpanded = true
        menuCallback?.invoke(currentMenuKey)
    }

    /**
     * Collapse.
     */
    fun collapse() {
        animateHeight(collapsedHeight)
        isExpanded = false
    }

    /**
     * Toggle.
     */
    fun toggle() {
        if (isExpanded) collapse() else expand()
    }

    /**
     * Set menu callback.
     */
    fun setOnMenuChangeListener(callback: (String) -> Unit) {
        menuCallback = callback
    }

    /**
     * Animate height.
     */
    private fun animateHeight(target: Float) {
        ValueAnimator.ofFloat(height.toFloat(), target.toFloat()).apply {
            duration = 250
            interpolator = DecelerateInterpolator()
            addUpdateListener { animation ->
                val h = animation.animatedValue as Float
                layoutParams.height = h.toInt()
                setLayoutParams(layoutParams)
            }
            start()
        }
    }

    /**
     * Switch menu vertically.
     */
    fun switchMenu(toKey: String) {
        showMenu(toKey)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()

        // Sheet background
        sheetPaint.color = 0xFF1A1A1A.toInt()
        sheetPaint.style = Paint.Style.FILL
        Path().apply {
            addRoundRect(0f, 0f, w, h, 24f, 24f, Path.Direction.CW)
            canvas.drawPath(this, sheetPaint)
        }

        // Handle
        handlePaint.color = 0xFF555555.toInt()
        handlePaint.style = Paint.Style.FILL
        canvas.drawRoundRect(w / 2 - 24f, 12f, w / 2 + 24f, 20f, 4f, 4f, handlePaint)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        return true
    }
}

/**
 * Menu tab selector.
 */
class MenuTabSelector(context: Context) : View(context) {

    private val tabPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val indicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var tabs = listOf<String>()
    private var selectedIndex = 0

    private var tabClickCallback: ((Int) -> Unit)? = null

    fun setTabs(tabList: List<String>) {
        tabs = tabList
        invalidate()
    }

    fun setSelectedIndex(index: Int) {
        selectedIndex = index.coerceIn(0, tabs.size - 1)
        invalidate()
    }

    fun setOnTabClickListener(callback: (Int) -> Unit) {
        tabClickCallback = callback
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (tabs.isEmpty()) return

        val w = width.toFloat()
        val h = height.toFloat()
        val tabWidth = w / tabs.size

        // Tab labels
        tabPaint.textSize = 28f
        tabPaint.textAlign = Paint.Align.CENTER

        for ((index, tab) in tabs.withIndex()) {
            val x = index * tabWidth + tabWidth / 2

            tabPaint.color = if (index == selectedIndex) 0xFFFF6B35.toInt() else 0xFFAAAAAA.toInt()
            canvas.drawText(tab, x, h / 2 + 8f, tabPaint)
        }

        // Indicator
        indicatorPaint.color = 0xFFFF6B35.toInt()
        indicatorPaint.style = Paint.Style.FILL

        val indicatorX = selectedIndex * tabWidth + 16
        canvas.drawRoundRect(indicatorX, h - 4f, indicatorX + tabWidth - 32, h, 4f, 4f, indicatorPaint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(width, resolveSize(48, heightMeasureSpec))
    }
}