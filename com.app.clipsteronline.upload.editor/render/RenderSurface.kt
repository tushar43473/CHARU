package com.app.clipsteronline.upload.editor.render

import android.view.Surface
import android.view.SurfaceView
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.EGLSurface
import android.opengl.GLES20
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * EGL render surface manager.
 * Handles surface creation and rendering.
 */
class RenderSurface(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {
    private val _surfaceState = MutableStateFlow(SurfaceState())
    val surfaceState: StateFlow<SurfaceState> = _surfaceState.asStateFlow()

    private var eglDisplay: EGLDisplay = EGL14.EGL_NO_DISPLAY
    private var eglContext: EGLContext = EGL14.EGL_NO_CONTEXT
    private var eglSurface: EGLSurface = EGL14.EGL_NO_SURFACE
    private var eglConfig: EGLConfig? = null

    private var surface: Surface? = null
    private var isReady = false

    /**
     * Create from Surface.
     */
    fun create(surface: Surface, width: Int, height: Int): Boolean {
        this.surface = surface
        return initialize(width, height)
    }

    /**
     * Initialize EGL.
     */
    fun initialize(width: Int, height: Int): Boolean {
        // Get EGL display
        eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        if (eglDisplay == EGL14.EGL_NO_DISPLAY) {
            return false
        }

        // Initialize
        val version = IntArray(2)
        EGL14.eglInitialize(eglDisplay, version, 0, version, 1)

        // Choose config
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

        // Create context
        val contextAttribs = intArrayOf(
            EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
            EGL14.EGL_NONE
        )

        eglContext = EGL14.eglCreateContext(eglDisplay, eglConfig, EGL14.EGL_NO_CONTEXT, contextAttribs, 0)

        // Create window surface
        val surfaceAttribs = intArrayOf(EGL14.EGL_NONE)
        eglSurface = EGL14.eglCreateWindowSurface(eglDisplay, eglConfig, surface!!, surfaceAttribs, 0)

        if (eglSurface == EGL14.EGL_NO_SURFACE) {
            return false
        }

        // Make current
        makeCurrent()

        // Viewport
        GLES20.glViewport(0, 0, width, height)
        GLES20.glEnable(GLES20.GL_BLEND)

        isReady = true
        _surfaceState.value = _surfaceState.value.copy(isReady = true)

        return true
    }

    /**
     * Make current.
     */
    fun makeCurrent(): Boolean {
        return EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)
    }

    /**
     * Swap buffers.
     */
    fun swapBuffers(): Boolean {
        if (!isReady) return false
        return EGL14.eglSwapBuffers(eglDisplay, eglSurface)
    }

    /**
     * Query surface.
     */
    fun querySurface(attribute: Int): Int {
        return EGL14.eglQuerySurface(eglDisplay, eglSurface, attribute)
    }

    /**
     * Get width.
     */
    fun getWidth(): Int = querySurface(EGL14.EGL_WIDTH)

    /**
     * Get height.
     */
    fun getHeight(): Int = querySurface(EGL14.EGL_HEIGHT)

    /**
     * Is valid.
     */
    fun isValid(): Boolean = eglSurface != EGL14.EGL_NO_SURFACE

    /**
     * Release surface.
     */
    fun releaseSurface() {
        if (eglSurface != EGL14.EGL_NO_SURFACE) {
            EGL14.eglDestroySurface(eglDisplay, eglSurface)
            eglSurface = EGL14.EGL_NO_SURFACE
        }
    }

    /**
     * Release context.
     */
    fun releaseContext() {
        if (eglContext != EGL14.EGL_NO_CONTEXT) {
            EGL14.eglDestroyContext(eglDisplay, eglContext)
            eglContext = EGL14.EGL_NO_CONTEXT
        }
    }

    /**
     * Terminate.
     */
    fun terminate() {
        if (eglDisplay != EGL14.EGL_NO_DISPLAY) {
            EGL14.eglTerminate(eglDisplay)
            eglDisplay = EGL14.EGL_NO_DISPLAY
        }
    }

    /**
     * Release all resources.
     */
    fun release() {
        releaseSurface()
        releaseContext()
        terminate()

        surface = null
        isReady = false
        _surfaceState.value = SurfaceState()
    }
}

/**
 * Surface state.
 */
data class SurfaceState(
    val isReady: Boolean = false
)