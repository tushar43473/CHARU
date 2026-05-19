package com.app.clipsteronline.upload.editor.ui.bottomsheet

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import android.widget.LinearLayout

/**
 * Bottom effects menu.
 * Realtime effects, transitions, intensity.
 */
class EffectsMenu(context: Context) : LinearLayout(context) {

    private var effectsCallback: ((EffectAction) -> Unit)? = null
    private var selectedEffect: String? = null
    private var intensity: Float = 1f

    private val effects = listOf(
        EffectInfo("None", "none"),
        EffectInfo("Blur", "blur"),
        EffectInfo("Glitch", "glitch"),
        EffectInfo("VHS", "vhs"),
        EffectInfo("Retro", "retro"),
        EffectInfo("Sparkle", "sparkle"),
        EffectInfo("Neon", "neon"),
        EffectInfo("Pixelate", "pixelate")
    )

    /**
     * Set effects callback.
     */
    fun setOnEffectSelectedListener(callback: (EffectAction) -> Unit) {
        effectsCallback = callback
    }

    /**
     * Set selected effect.
     */
    fun setSelectedEffect(effect: String) {
        selectedEffect = effect
        invalidate()
    }

    /**
     * Set intensity.
     */
    fun setIntensity(value: Float) {
        intensity = value.coerceIn(0f, 1f)
        invalidate()
    }

    /**
     * Effect action data.
     */
    data class EffectAction(
        val type: String,
        val intensity: Float = 1f
    )

    /**
     * Effect info data.
     */
    data class EffectInfo(val name: String, val id: String)
}

/**
 * Effects intensity slider.
 */
class EffectsIntensitySlider(context: Context) : View(context) {

    private val sliderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var intensity = 1f
    private var intensityCallback: ((Float) -> Unit)? = null

    fun setIntensity(value: Float) {
        intensity = value.coerceIn(0f, 1f)
        invalidate()
    }

    fun setOnIntensityChangedListener(callback: (Float) -> Unit) {
        intensityCallback = callback
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        val trackHeight = 8f
        val trackY = h / 2 - trackHeight / 2

        // Track background
        trackPaint.color = 0xFF333333.toInt()
        trackPaint.style = Paint.Style.FILL
        canvas.drawRoundRect(0f, trackY, w, trackY + trackHeight, 4f, 4f, trackPaint)

        // Track fill
        sliderPaint.color = 0xFFFF6B35.toInt()
        canvas.drawRoundRect(0f, trackY, w * intensity, trackY + trackHeight, 4f, 4f, sliderPaint)

        // Thumb
        sliderPaint.color = 0xFFFFFFFF.toInt()
        canvas.drawCircle(w * intensity, h / 2, 12f, sliderPaint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = resolveSize(desiredWidth, widthMeasureSpec)
        val height = resolveSize(48, heightMeasureSpec)
        setMeasuredDimension(width, height)
    }

    private val desiredWidth = 300
}