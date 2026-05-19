package com.app.clipsteronline.upload.editor.ui.effects

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.view.View

/**
 * Effect timeline overlay.
 * Effect blocks, transitions, layered rendering.
 */
class EffectTimelineOverlay(context: Context) : View(context) {

    private val blockPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val transitionPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val handlePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val effects = mutableListOf<EffectBlock>()
    private var pixelsPerMs = 0.1f

    private val accentColor = 0xFFFF6B35.toInt()

    /**
     * Set pixels per millisecond.
     */
    fun setPixelsPerMs(px: Float) {
        pixelsPerMs = px.coerceIn(0.01f, 1f)
        invalidate()
    }

    /**
     * Add effect block.
     */
    fun addEffect(block: EffectBlock) {
        effects.add(block)
        invalidate()
    }

    /**
     * Remove effect.
     */
    fun removeEffect(effectId: String) {
        effects.removeAll { it.id == effectId }
        invalidate()
    }

    /**
     * Update effect.
     */
    fun updateEffect(block: EffectBlock) {
        val index = effects.indexOfFirst { it.id == block.id }
        if (index >= 0) {
            effects[index] = block
            invalidate()
        }
    }

    /**
     * Get effects in range.
     */
    fun getEffectsInRange(startMs: Long, endMs: Long): List<EffectBlock> {
        return effects.filter { 
            it.startMs < endMs && it.endMs > startMs 
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        for (block in effects) {
            drawEffectBlock(canvas, block)
        }
    }

    private fun drawEffectBlock(canvas: Canvas, block: EffectBlock) {
        val startX = block.startMs * pixelsPerMs
        val endX = block.endMs * pixelsPerMs
        val w = endX - startX

        // Block background
        if (block.type == BlockType.EFFECT) {
            blockPaint.color = 0xFF3D5AFE.toInt()
        } else {
            blockPaint.color = 0xFFFF6B35.toInt()
        }

        blockPaint.style = Paint.Style.FILL
        canvas.drawRoundRect(startX, 0f, endX, height.toFloat(), 4f, 4f, blockPaint)

        // Transition overlay
        if (block.type == BlockType.TRANSITION) {
            transitionPaint.color = 0x40FFFFFF.toInt()
            transitionPaint.style = Paint.Style.FILL
            canvas.drawRect(startX, 0f, startX + w * 0.3f, height.toFloat(), transitionPaint)
            canvas.drawRect(endX - w * 0.3f, 0f, endX, height.toFloat(), transitionPaint)
        }

        // Label
        labelPaint.color = 0xFFFFFFFF.toInt()
        labelPaint.textSize = 20f
        labelPaint.textAlign = Paint.Align.LEFT

        val labelX = startX + 8f
        val labelY = height / 2f + 6f

        // Truncate label if too long
        val maxLabel = if (w > 100) block.name else block.name.take((w / 10).toInt())
        canvas.drawText(maxLabel, labelX, labelY, labelPaint)

        // Duration
        val duration = ((block.endMs - block.startMs) / 1000).toInt()
        if (w > 50) {
            labelPaint.textSize = 16f
            labelPaint.color = 0xCCFFFFFF.toInt()
            canvas.drawText("${duration}s", labelX, labelY + 20f, labelPaint)
        }

        // Resize handles
        if (block.isSelected) {
            handlePaint.color = accentColor
            handlePaint.style = Paint.Style.FILL

            handlePaint.color = accentColor
            canvas.drawRect(startX, 0f, startX + 6f, height.toFloat(), handlePaint)
            canvas.drawRect(endX - 6f, 0f, endX, height.toFloat(), handlePaint)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(width, resolveSize(48, heightMeasureSpec))
    }

    /**
     * Effect block data.
     */
    data class EffectBlock(
        val id: String,
        val name: String,
        val type: BlockType,
        val startMs: Long,
        val endMs: Long,
        val isSelected: Boolean = false
    )

    /**
     * Block types.
     */
    enum class BlockType {
        EFFECT,
        TRANSITION,
        FILTER
    }
}

/**
 * Multi-track effect overlay.
 */
class MultiTrackEffectOverlay(context: Context) : View(context) {

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var tracks = listOf<EffectTimelineOverlay>()

    fun addTrack(effectOverlay: EffectTimelineOverlay) {
        tracks = tracks + effectOverlay
        invalidate()
    }

    fun removeTrack(id: String) {
        tracks = tracks.filterNot { it.id.hashCode().toString() == id }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val trackH = height.toFloat() / tracks.size.coerceAtLeast(1)

        for ((index, track) in tracks.withIndex()) {
            canvas.save()
            canvas.translate(0f, index * trackH)
            track.let { /* Render track */ }
            canvas.restore()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(width, resolveSize(tracks.size * 48, heightMeasureSpec))
    }
}