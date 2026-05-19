package com.app.clipsteronline.upload.editor.player

import android.content.Context
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.util.AttributeSet
import android.view.Surface
import android.view.SurfaceView
import android.view.TextureView
import android.view.View

/**
 * Preview surface for video playback.
 * Supports both SurfaceView and TextureView.
 */
class PreviewSurface @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var surfaceView: SurfaceView? = null
    private var textureView: TextureView? = null
    private var currentSurface: Surface? = null

    private var surfaceCreatedListener: OnSurfaceCreatedListener? = null
    private var surfaceDestroyedListener: OnSurfaceDestroyedListener? = null

    private val scaleMatrix = Matrix()

    /**
     * Surface type.
     */
    enum class SurfaceType {
        SURFACE_VIEW,
        TEXTURE_VIEW
    }

    var surfaceType: SurfaceType = SurfaceType.TEXTURE_VIEW
        set(value) {
            field = value
            recreateView()
        }

    var resizeMode: ResizeMode = ResizeMode.FIT
        set(value) {
            field = value
            requestLayout()
        }

    init {
        setBackgroundColor(Color.BLACK)
    }

    /**
     * Get surface view.
     */
    fun getSurfaceView(): SurfaceView? = surfaceView

    /**
     * Get texture view.
     */
    fun getTextureView(): TextureView? = textureView

    /**
     * Get surface.
     */
    fun getSurface(): Surface? = when (surfaceType) {
        SurfaceType.SURFACE_VIEW -> surfaceView?.holder?.surface
        SurfaceType.TEXTURE_VIEW -> textureView?.surfaceTexture?.let { Surface(it) }
    }

    /**
     * Get surface texture.
     */
    fun getSurfaceTexture(): SurfaceTexture? = textureView?.surfaceTexture

    /**
     * Set surface texture listener.
     */
    fun setSurfaceTextureListener(
        listener: TextureView.SurfaceTextureListener
    ) {
        textureView?.setSurfaceTextureListener(listener)
    }

    /**
     * Set surface created listener.
     */
    fun setOnSurfaceCreatedListener(listener: OnSurfaceCreatedListener?) {
        this.surfaceCreatedListener = listener
    }

    /**
     * Set surface destroyed listener.
     */
    fun setOnSurfaceDestroyedListener(listener: OnSurfaceDestroyedListener?) {
        this.surfaceDestroyedListener = listener
    }

    /**
     * Set transform matrix.
     */
    fun setTransformMatrix(matrix: Matrix) {
        textureView?.setTransformMatrix(matrix)
    }

    /**
     * Get transform matrix.
     */
    fun getTransform(): Matrix {
        val matrix = Matrix()
        textureView?.getTransform(matrix)
        return matrix
    }

    /**
     * Set video scale.
     */
    fun setVideoScale(scaleX: Float, scaleY: Float) {
        textureView?.setScaleX(scaleX)
        textureView?.setScaleY(scaleY)
    }

    /**
     * Apply scaling to fit.
     */
    fun applyFitScaling(videoWidth: Int, videoHeight: Int) {
        val viewWidth = width
        val viewHeight = height

        if (viewWidth == 0 || viewHeight == 0 || videoWidth == 0 || videoHeight == 0) return

        val videoAspect = videoWidth.toFloat() / videoHeight
        val viewAspect = viewWidth.toFloat() / viewHeight

        val scale: Float
        val translateX: Float
        val translateY: Float

        when (resizeMode) {
            ResizeMode.FIT -> {
                if (videoAspect > viewAspect) {
                    scale = viewWidth.toFloat() / videoWidth
                    val scaledHeight = videoHeight * scale
                    translateX = 0f
                    translateY = (viewHeight - scaledHeight) / 2
                } else {
                    scale = viewHeight.toFloat() / videoHeight
                    val scaledWidth = videoWidth * scale
                    translateX = (viewWidth - scaledWidth) / 2
                    translateY = 0f
                }
            }
            ResizeMode.FILL -> {
                if (videoAspect > viewAspect) {
                    scale = viewHeight.toFloat() / videoHeight
                    val scaledWidth = videoWidth * scale
                    translateX = (viewWidth - scaledWidth) / 2
                    translateY = 0f
                } else {
                    scale = viewWidth.toFloat() / videoWidth
                    val scaledHeight = videoHeight * scale
                    translateX = 0f
                    translateY = (viewHeight - scaledHeight) / 2
                }
            }
            ResizeMode.CROP -> {
                scale = maxOf(
                    viewWidth.toFloat() / videoWidth,
                    viewHeight.toFloat() / videoHeight
                )
                translateX = (viewWidth - videoWidth * scale) / 2
                translateY = (viewHeight - videoHeight * scale) / 2
            }
            ResizeMode.STRETCH -> {
                scale = 1f
                translateX = 0f
                translateY = 0f
            }
        }

        this.scaleMatrix.reset()
        this.scaleMatrix.setScale(scale, scale)
        this.scaleMatrix.postTranslate(translateX, translateY)
        textureView?.setTransformMatrix(this.scaleMatrix)
    }

    /**
     * Update aspect ratio.
     */
    fun setAspectRatio(videoWidth: Int, videoHeight: Int) {
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    private fun recreateView() {
        when (surfaceType) {
            SurfaceType.SURFACE_VIEW -> {
                textureView?.visibility = View.GONE
                if (surfaceView == null) {
                    surfaceView = SurfaceView(context).apply {
                        layoutParams = layoutParams
                    }
                }
                surfaceView?.visibility = View.VISIBLE
            }
            SurfaceType.TEXTURE_VIEW -> {
                surfaceView?.visibility = View.GONE
                if (textureView == null) {
                    textureView = TextureView(context).apply {
                        layoutParams = layoutParams
                    }
                }
                textureView?.visibility = View.VISIBLE
            }
        }
    }

    override fun onDetachedFromWindow() {
        surfaceDestroyedListener?.onSurfaceDestroyed()
        super.onDetachedFromWindow()
    }
}

/**
 * Resize mode for preview.
 */
enum class ResizeMode {
    FIT,
    FILL,
    CROP,
    STRETCH
}

/**
 * Surface created listener.
 */
interface OnSurfaceCreatedListener {
    fun onSurfaceCreated()
}

/**
 * Surface destroyed listener.
 */
interface OnSurfaceDestroyedListener {
    fun onSurfaceDestroyed()
}