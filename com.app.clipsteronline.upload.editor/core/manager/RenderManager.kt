package com.app.clipsteronline.upload.editor.core.manager

import android.content.Context
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.EGLExt
import android.opengl.EGLSurface
import android.opengl.GLES20
import android.view.Surface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Manager for coordinating render pipeline and frame rendering.
 * Handles preview, frame extraction, and render queue.
 */
class RenderManager(
    private val context: Context
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var eglDisplay: EGLDisplay = EGL14.EGL_NO_DISPLAY
    private var eglContext: EGLContext = EGL14.EGL_NO_CONTEXT
    private var eglSurface: EGLSurface = EGL14.EGL_NO_SURFACE
    private var eglConfig: EGLConfig? = null

    private val _renderState = MutableStateFlow(RenderState.IDLE)
    val renderState: StateFlow<RenderState> = _renderState.asStateFlow()

    private val renderQueue = ConcurrentLinkedQueue<RenderTask>()
    private var isRendering = false
    private var isInitialized = false

    companion object {
        private const val EGL_VERSION = 2

        private val CONFIG_ATTRIBUTES = intArrayOf(
            EGL14.EGL_RED_SIZE, 8,
            EGL14.EGL_GREEN_SIZE, 8,
            EGL14.EGL_BLUE_SIZE, 8,
            EGL14.EGL_ALPHA_SIZE, 8,
            EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
            EGLExt.EGL_RECORDABLE_ANDROID, 1,
            EGL14.EGL_NONE
        )

        private val CONTEXT_ATTRIBUTES = intArrayOf(
            EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
            EGL14.EGL_NONE
        )
    }

    /**
     * Initialize OpenGL renderer.
     */
    fun initialize(): Boolean {
        if (isInitialized) return true

        // Get EGL display
        eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        if (eglDisplay == EGL14.EGL_NO_DISPLAY) {
            _renderState.value = RenderState.ERROR("Failed to get EGL display")
            return false
        }

        // Initialize EGL
        val version = IntArray(2)
        if (!EGL14.eglInitialize(eglDisplay, version, 0, version, 1)) {
            _renderState.value = RenderState.ERROR("Failed to initialize EGL")
            return false
        }

        // Choose config
        val configs = arrayOfNulls<EGLConfig>(1)
        val numConfigs = IntArray(1)
        if (!EGL14.eglChooseConfig(eglDisplay, CONFIG_ATTRIBUTES, 0, configs, 0, 1, numConfigs, 0)) {
            _renderState.value = RenderState.ERROR("Failed to choose EGL config")
            return false
        }
        eglConfig = configs[0]

        // Create context
        eglContext = EGL14.eglCreateContext(eglDisplay, eglConfig, EGL14.EGL_NO_CONTEXT, CONTEXT_ATTRIBUTES, 0)
        if (eglContext == EGL14.EGL_NO_CONTEXT) {
            _renderState.value = RenderState.ERROR("Failed to create EGL context")
            return false
        }

        // Create dummy surface
        val surfaceAttribs = intArrayOf(EGL14.EGL_WIDTH, 1, EGL14.EGL_HEIGHT, 1, EGL14.EGL_NONE)
        eglSurface = EGL14.eglCreateWindowSurface(eglDisplay, eglConfig, android.view.Surface(android.os.Parcel.obtain()), surfaceAttribs, 0)
        if (eglSurface == EGL14.EGL_NO_SURFACE) {
            _renderState.value = RenderState.ERROR("Failed to create EGL surface")
            return false
        }

        isInitialized = true
        _renderState.value = RenderState.READY
        return true
    }

    /**
     * Initialize with surface.
     */
    fun initializeWithSurface(surface: Surface, width: Int, height: Int): Boolean {
        if (!isInitialized) {
            if (!initialize()) return false
        }

        // Create surface
        val surfaceAttribs = intArrayOf(EGL14.EGL_WIDTH, width, EGL14.EGL_HEIGHT, height, EGL14.EGL_NONE)
        eglSurface = EGL14.eglCreateWindowSurface(eglDisplay, eglConfig, surface, surfaceAttribs, 0)

        if (eglSurface == EGL14.EGL_NO_SURFACE) {
            _renderState.value = RenderState.ERROR("Failed to create window surface")
            return false
        }

        _renderState.value = RenderState.READY
        return true
    }

    /**
     * Make current.
     */
    fun makeCurrent(): Boolean {
        if (!isInitialized) return false
        return EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)
    }

    /**
     * Swap buffers.
     */
    fun swapBuffers(): Boolean {
        if (!isInitialized || eglSurface == EGL14.EGL_NO_SURFACE) return false
        return EGL14.eglSwapBuffers(eglDisplay, eglSurface)
    }

    /**
     * Render frame at time.
     */
    fun renderFrame(timeMs: Long): Boolean {
        if (!isInitialized) {
            _renderState.value = RenderState.ERROR("Renderer not initialized")
            return false
        }

        makeCurrent()

        // Clear
        GLES20.glClearColor(0f, 0f, 0f, 1f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        // Render here

        swapBuffers()
        return true
    }

    /**
     * Render preview.
     */
    fun renderPreview(timeMs: Long, onComplete: (() -> Unit)? = null): Boolean {
        _renderState.value = RenderState.RENDERING

        val result = renderFrame(timeMs)

        scope.launch {
            _renderState.value = RenderState.READY
            onComplete?.invoke()
        }

        return result
    }

    /**
     * Add render task to queue.
     */
    fun queueRender(task: RenderTask) {
        renderQueue.offer(task)
        processQueue()
    }

    /**
     * Clear render queue.
     */
    fun clearQueue() {
        renderQueue.clear()
    }

    /**
     * Get queue size.
     */
    fun getQueueSize(): Int = renderQueue.size

    /**
     * Process render queue.
     */
    private fun processQueue() {
        if (isRendering) return
        isRendering = true

        scope.launch {
            while (renderQueue.isNotEmpty()) {
                val task = renderQueue.poll() ?: break
                renderFrame(task.timeMs)
            }
            isRendering = false
        }
    }

    /**
     * Set viewport.
     */
    fun setViewport(width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }

    /**
     * Release renderer.
     */
    fun release() {
        if (eglSurface != EGL14.EGL_NO_SURFACE) {
            EGL14.eglDestroySurface(eglDisplay, eglSurface)
        }
        if (eglContext != EGL14.EGL_NO_CONTEXT) {
            EGL14.eglDestroyContext(eglDisplay, eglContext)
        }
        if (eglDisplay != EGL14.EGL_NO_DISPLAY) {
            EGL14.eglTerminate(eglDisplay)
        }

        eglSurface = EGL14.EGL_NO_SURFACE
        eglContext = EGL14.EGL_NO_CONTEXT
        eglDisplay = EGL14.EGL_NO_DISPLAY

        isInitialized = false
        _renderState.value = RenderState.IDLE
    }

    /**
     * Get GL version.
     */
    fun getGLVersion(): String {
        makeCurrent()
        return GLES20.glGetString(GLES20.GL_VERSION) ?: "Unknown"
    }

    /**
     * Check GL error.
     */
    fun checkGLError(): Int {
        return GLES20.glGetError()
    }
}

/**
 * Render state.
 */
sealed class RenderState {
    data object IDLE : RenderState()
    data object READY : RenderState()
    data object RENDERING : RenderState()
    data class ERROR(val message: String) : RenderState()
}

/**
 * Render task.
 */
data class RenderTask(
    val timeMs: Long,
    val trackId: String? = null,
    val priority: Int = 0,
    val onComplete: (() -> Unit)? = null
)