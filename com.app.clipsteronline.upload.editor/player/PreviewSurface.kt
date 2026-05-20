package com.app.clipsteronline.upload.editor.player

import android.graphics.Matrix
import android.view.Surface
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreviewSurface(
    private val scaling: PreviewScaling = PreviewScaling(),
) {
    private var surface: Surface? = null
    private var videoWidth: Int = 0
    private var videoHeight: Int = 0

    private val _state = MutableStateFlow(PreviewSurfaceState())
    val state: StateFlow<PreviewSurfaceState> = _state.asStateFlow()

    fun bind(surface: Surface?) {
        this.surface = surface
        _state.value = _state.value.copy(isBound = surface != null)
    }

    fun unbind() {
        surface = null
        _state.value = _state.value.copy(isBound = false)
    }

    fun updateVideoSize(width: Int, height: Int) {
        videoWidth = width.coerceAtLeast(0)
        videoHeight = height.coerceAtLeast(0)
        _state.value = _state.value.copy(videoWidth = videoWidth, videoHeight = videoHeight)
    }

    fun updateTransform(
        viewWidth: Int,
        viewHeight: Int,
        mode: PreviewScaleMode,
        customScale: Float = 1f,
        panX: Float = 0f,
        panY: Float = 0f,
    ): Matrix {
        val transform = scaling.buildMatrix(viewWidth, viewHeight, videoWidth, videoHeight, mode, customScale, panX, panY)
        _state.value = _state.value.copy(scaleX = transform.appliedScaleX, scaleY = transform.appliedScaleY)
        return transform.matrix
    }

    fun aspectRatioOrNull(): Float? {
        if (videoWidth <= 0 || videoHeight <= 0) return null
        return videoWidth.toFloat() / videoHeight.toFloat()
    }

    fun getSurface(): Surface? = surface
}

data class PreviewSurfaceState(
    val isBound: Boolean = false,
    val videoWidth: Int = 0,
    val videoHeight: Int = 0,
    val scaleX: Float = 1f,
    val scaleY: Float = 1f,
)
