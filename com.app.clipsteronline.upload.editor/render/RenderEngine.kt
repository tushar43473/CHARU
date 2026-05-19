package com.app.clipsteronline.upload.editor.render

import android.content.Context
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.EGLSurface
import android.opengl.GLES20
import android.view.Surface
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Central rendering engine.
 * Coordinates render pipeline and lifecycle.
 */
class RenderEngine(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {
    private val _engineState = MutableStateFlow(EngineState())
    val engineState: StateFlow<EngineState> = _engineState.asStateFlow()

    private var eglDisplay: EGLDisplay = EGL14.EGL_NO_DISPLAY
    private var eglContext: EGLContext = EGL14.EGL_NO_CONTEXT
    private var eglSurface: EGLSurface = EGL14.EGL_NO_SURFACE
    private var eglConfig: EGLConfig? = null

    private var isInitialized = false

    /**
     * Initialize render engine.
     */
    fun initialize(surface: Surface?, width: Int, height: Int): Boolean {
        if (isInitialized) return true

        eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        if (eglDisplay == EGL14.EGL_NO_DISPLAY) {
            return false
        }

        val version = IntArray(2)
        EGL14.eglInitialize(eglDisplay, version, 0, version, 1)

        val configAttribs = intArrayOf(
            EGL14.EGL_RED_SIZE, 8,
            EGL14.EGL_GREEN_SIZE, 8,
            EGL14.EGL_BLUE_SIZE, 8,
            EGL14.EGL_ALPHA_SIZE, 8,
            EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
            EGL14.EGL_SURFACE_TYPE, EGL14.EGL_WINDOW_BIT,
            EGL14.EGL_NONE
        )

        val configs = arrayOfNulls<EGLConfig>(1)
        val numConfigs = IntArray(1)
        EGL14.eglChooseConfig(eglDisplay, configAttribs, 0, configs, 0, 1, numConfigs, 0)

        eglConfig = configs[0]

        val contextAttribs = intArrayOf(
            EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
            EGL14.EGL_NONE
        )

        eglContext = EGL14.eglCreateContext(eglDisplay, eglConfig, EGL14.EGL_NO_CONTEXT, contextAttribs, 0)

        if (surface != null) {
            val surfaceAttribs = intArrayOf(EGL14.EGL_NONE)
            eglSurface = EGL14.eglCreateWindowSurface(eglDisplay, eglConfig, surface, surfaceAttribs, 0)
        }

        isInitialized = true

        _engineState.value = _engineState.value.copy(
            isReady = true,
            width = width,
            height = height
        )

        return true
    }

    /**
     * Swap buffers.
     */
    fun swapBuffers(): Boolean {
        if (!isInitialized) return false
        return EGL14.eglSwapBuffers(eglDisplay, eglSurface)
    }

    /**
     * Make current.
     */
    fun makeCurrent(): Boolean {
        if (!isInitialized) return false
        return EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)
    }

    /**
     * Present frame.
     */
    fun present(): Boolean {
        makeCurrent()
        drawFrame()
        return swapBuffers()
    }

    /**
     * Draw frame placeholder.
     */
    private fun drawFrame() {
        GLES20.glClearColor(0f, 0f, 0f, 1f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
    }

    /**
     * Set viewport.
     */
    fun setViewport(width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        _engineState.value = _engineState.value.copy(
            width = width,
            height = height
        )
    }

    /**
     * Release engine.
     */
    fun release() {
        if (!isInitialized) return

        EGL14.eglDestroySurface(eglDisplay, eglSurface)
        EGL14.eglDestroyContext(eglDisplay, eglContext)
        EGL14.eglTerminate(eglDisplay)

        eglDisplay = EGL14.EGL_NO_DISPLAY
        eglContext = EGL14.EGL_NO_CONTEXT
        eglSurface = EGL14.EGL_NO_SURFACE
        isInitialized = false

        _engineState.value = EngineState()
    }
}

/**
 * Engine state.
 */
data class EngineState(
    val isReady: Boolean = false,
    val isRendering: Boolean = false,
    val width: Int = 0,
    val height: Int = 0,
    val fps: Float = 0f
)