package com.app.clipsteronline.upload.editor.render

import android.graphics.Matrix
import android.view.Surface
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RenderSurface {
    private var targetSurface: Surface? = null
    private var transform: Matrix = Matrix()

    private val _state = MutableStateFlow(RenderSurfaceState())
    val state: StateFlow<RenderSurfaceState> = _state.asStateFlow()

    fun attach(surface: Surface?) {
        targetSurface = surface
        _state.value = _state.value.copy(attached = surface != null)
    }

    fun detach() {
        targetSurface = null
        _state.value = _state.value.copy(attached = false)
    }

    fun isAttached(): Boolean = targetSurface != null

    fun setTransform(matrix: Matrix) {
        transform = Matrix(matrix)
        _state.value = _state.value.copy(hasTransform = true)
    }

    fun resetTransform() {
        transform = Matrix()
        _state.value = _state.value.copy(hasTransform = false)
    }

    fun getTransform(): Matrix = Matrix(transform)
    fun getSurface(): Surface? = targetSurface
}

data class RenderSurfaceState(
    val attached: Boolean = false,
    val hasTransform: Boolean = false,
)
