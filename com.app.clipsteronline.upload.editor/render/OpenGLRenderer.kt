package com.app.clipsteronline.upload.editor.render

import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.EGLSurface
import android.opengl.GLES20
import android.view.Surface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * OpenGL ES renderer.
 * Handles EGL context, textures, and framebuffers.
 */
class OpenGLRenderer(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {
    private val _glState = MutableStateFlow(GLState())
    val glState: StateFlow<GLState> = _glState.asStateFlow()

    private var eglDisplay: EGLDisplay = EGL14.EGL_NO_DISPLAY
    private var eglContext: EGLContext = EGL14.EGL_NO_CONTEXT
    private var eglSurface: EGLSurface = EGL14.EGL_NO_SURFACE
    private var eglConfig: EGLConfig? = null

    private var surface: Surface? = null
    private var width = 0
    private var height = 0

    private val texturePool = mutableListOf<Int>()
    private val framebufferPool = mutableListOf<Int>()

    private val MAX_TEXTURES = 32

    /**
     * Initialize OpenGL.
     */
    fun initialize(surface: Surface, width: Int, height: Int): Boolean {
        this.surface = surface
        this.width = width
        this.height = height

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

        val surfaceAttribs = intArrayOf(EGL14.EGL_NONE)
        eglSurface = EGL14.eglCreateWindowSurface(eglDisplay, eglConfig, surface, surfaceAttribs, 0)

        makeCurrent()
        GLES20.glViewport(0, 0, width, height)

        _glState.value = _glState.value.copy(
            isReady = true,
            width = width,
            height = height
        )

        return true
    }

    /**
     * Make context current.
     */
    fun makeCurrent(): Boolean {
        return EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)
    }

    /**
     * Swap buffers.
     */
    fun swapBuffers(): Boolean {
        return EGL14.eglSwapBuffers(eglDisplay, eglSurface)
    }

    /**
     * Allocate texture.
     */
    fun allocateTexture(): Int {
        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)

        val textureId = textures[0]
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)

        texturePool.add(textureId)
        return textureId
    }

    /**
     * Allocate framebuffer.
     */
    fun allocateFramebuffer(): Int {
        val framebuffers = IntArray(1)
        GLES20.glGenFramebuffers(1, framebuffers, 0)
        val fb = framebuffers[0]
        framebufferPool.add(fb)
        return fb
    }

    /**
     * Bind texture image.
     */
    fun bindTextureImage(textureId: Int, unit: Int = 0) {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + unit)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
    }

    /**
     * Bind framebuffer.
     */
    fun bindFramebuffer(framebufferId: Int) {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, framebufferId)
    }

    /**
     * Get texture from pool.
     */
    fun getFreeTexture(): Int? {
        return texturePool.firstOrNull()
    }

    /**
     * Release texture.
     */
    fun releaseTexture(textureId: Int) {
        if (texturePool.contains(textureId)) {
            val textures = intArrayOf(textureId)
            GLES20.glDeleteTextures(1, textures, 0)
            texturePool.remove(textureId)
        }
    }

    /**
     * Release framebuffer.
     */
    fun releaseFramebuffer(fb: Int) {
        if (framebufferPool.contains(fb)) {
            val framebuffers = intArrayOf(fb)
            GLES20.glDeleteFramebuffers(1, framebuffers, 0)
            framebufferPool.remove(fb)
        }
    }

    /**
     * Release all resources.
     */
    fun release() {
        makeCurrent()

        texturePool.forEach { releaseTexture(it) }
        framebufferPool.forEach { releaseFramebuffer(it) }

        EGL14.eglDestroySurface(eglDisplay, eglSurface)
        EGL14.eglDestroyContext(eglDisplay, eglContext)
        EGL14.eglTerminate(eglDisplay)

        _glState.value = GLState()
    }
}

/**
 * OpenGL state.
 */
data class GLState(
    val isReady: Boolean = false,
    val width: Int = 0,
    val height: Int = 0,
    val textureCount: Int = 0
)