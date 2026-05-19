package com.app.clipsteronline.upload.editor.render

import android.opengl.GLES20
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * OpenGL ES renderer.
 * Simplified GL management for video rendering.
 */
class GLRenderer(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {
    private val _glState = MutableStateFlow(GLRenderState())
    val glState: StateFlow<GLRenderState> = _glState.asStateFlow()

    private var program = 0
    private var isReady = false

    // Shader handles
    private var positionHandle = 0
    private var texCoordHandle = 0
    private var mvpMatrixHandle = 0
    private var textureHandle = 0
    private var alphaHandle = 0

    private val vertexShader = """
        uniform mat4 uMVPMatrix;
        attribute vec4 aPosition;
        attribute vec2 aTexCoord;
        varying vec2 vTexCoord;
        void main() {
            gl_Position = uMVPMatrix * aPosition;
            vTexCoord = aTexCoord;
        }
    """

    private val fragmentShader = """
        precision mediump float;
        varying vec2 vTexCoord;
        uniform sampler2D sTexture;
        uniform float uAlpha;
        void main() {
            vec4 color = texture2D(sTexture, vTexCoord);
            gl_FragColor = vec4(color.rgb, color.a * uAlpha);
        }
    """

    /**
     * Initialize GL context.
     */
    fun initialize(): Boolean {
        if (isReady) return true

        program = createProgram(vertexShader, fragmentShader)
        if (program == 0) return false

        // Get attribute/uniform locations
        positionHandle = GLES20.glGetAttribLocation(program, "aPosition")
        texCoordHandle = GLES20.glGetAttribLocation(program, "aTexCoord")
        mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix")
        textureHandle = GLES20.glGetUniformLocation(program, "sTexture")
        alphaHandle = GLES20.glGetUniformLocation(program, "uAlpha")

        isReady = true

        _glState.value = _glState.value.copy(
            isReady = true
        )

        return true
    }

    /**
     * Use program.
     */
    fun useProgram() {
        if (isReady) {
            GLES20.glUseProgram(program)
        }
    }

    /**
     * Draw textured quad.
     */
    fun drawQuad(
        textureId: Int,
        mvpMatrix: FloatArray,
        alpha: Float = 1f,
        texCoords: FloatArray = defaultTexCoords
    ) {
        if (!isReady) return

        useProgram()

        // Bind texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glUniform1i(textureHandle, 0)

        // Set MVP matrix
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)

        // Set alpha
        GLES20.glUniform1f(alphaHandle, alpha)

        // Set vertices
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 20, quadVertices, 0)
        GLES20.glEnableVertexAttribArray(positionHandle)

        GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 8, texCoords, 0)
        GLES20.glEnableVertexAttribArray(texCoordHandle)

        // Draw
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        // Cleanup
        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(texCoordHandle)
    }

    /**
     * Clear framebuffer.
     */
    fun clear(r: Float = 0f, g: Float = 0f, b: Float = 0f, a: Float = 1f) {
        GLES20.glClearColor(r, g, b, a)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
    }

    /**
     * Enable blending.
     */
    fun enableBlending(src: Int = GLES20.GL_SRC_ALPHA, dst: Int = GLES20.GL_ONE_MINUS_SRC_ALPHA) {
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(src, dst)
    }

    /**
     * Disable blending.
     */
    fun disableBlending() {
        GLES20.glDisable(GLES20.GL_BLEND)
    }

    /**
     * Create shader program.
     */
    private fun createProgram(vs: String, fs: String): Int {
        val vertexShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER)
        GLES20.glShaderSource(vertexShader, vs)
        GLES20.glCompileShader(vertexShader)

        val fragmentShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER)
        GLES20.glShaderSource(fragmentShader, fs)
        GLES20.glCompileShader(fragmentShader)

        val program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        GLES20.glLinkProgram(program)

        return program
    }

    /**
     * Release resources.
     */
    fun release() {
        if (program != 0) {
            GLES20.glDeleteProgram(program)
            program = 0
        }
        isReady = false
        _glState.value = GLRenderState()
    }

    companion object {
        private val quadVertices = floatArrayOf(
            -1f, -1f, 0f, 0f, 1f,
             1f, -1f, 0f, 1f, 1f,
            -1f,  1f, 0f, 0f, 0f,
             1f,  1f, 0f, 1f, 0f
        )

        private val defaultTexCoords = floatArrayOf(
            0f, 1f,
            1f, 1f,
            0f, 0f,
            1f, 0f
        )
    }
}

/**
 * GL render state.
 */
data class GLRenderState(
    val isReady: Boolean = false
)