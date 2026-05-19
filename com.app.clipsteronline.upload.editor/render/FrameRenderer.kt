package com.app.clipsteronline.upload.editor.render

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.Matrix
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Renders video frames with transformations.
 * Handles scaling, cropping, rotation, and blending.
 */
class FrameRenderer(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {
    private var program: Int = 0
    private var positionHandle: Int = 0
    private var texCoordHandle: Int = 0
    private var mvpMatrixHandle: Int = 0
    private var textureHandle: Int = 0
    private var alphaHandle: Int = 0

    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)
    private val tempMatrix = FloatArray(16)

    private var textureId: Int = 0

    companion object {
        private const val VERTEX_SHADER = """
            uniform mat4 uMVPMatrix;
            attribute vec4 aPosition;
            attribute vec2 aTexCoord;
            varying vec2 vTexCoord;
            void main() {
                gl_Position = uMVPMatrix * aPosition;
                vTexCoord = aTexCoord;
            }
        """

        private const val FRAGMENT_SHADER = """
            #extension GL_OES_EGL_image_external : require
            precision mediump float;
            varying vec2 vTexCoord;
            uniform samplerExternalOES sTexture;
            uniform float uAlpha;
            void main() {
                vec4 color = texture2D(sTexture, vTexCoord);
                gl_FragColor = vec4(color.rgb, color.a * uAlpha);
            }
        """
    }

    /**
     * Initialize shader program.
     */
    fun initialize(): Boolean {
        program = createProgram(VERTEX_SHADER, FRAGMENT_SHADER)
        if (program == 0) return false

        positionHandle = GLES20.glGetAttribLocation(program, "aPosition")
        texCoordHandle = GLES20.glGetAttribLocation(program, "aTexCoord")
        mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix")
        textureHandle = GLES20.glGetUniformLocation(program, "sTexture")
        alphaHandle = GLES20.glGetUniformLocation(program, "uAlpha")

        return true
    }

    /**
     * Render frame with transformation.
     */
    fun renderFrame(
        textureId: Int,
        alpha: Float = 1f,
        scaleX: Float = 1f,
        scaleY: Float = 1f,
        rotation: Float = 0f,
        transX: Float = 0f,
        transY: Float = 0f,
        cropLeft: Float = 0f,
        cropTop: Float = 0f,
        cropRight: Float = 1f,
        cropBottom: Float = 1f
    ) {
        this.textureId = textureId

        // Set up matrices
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.scaleM(modelMatrix, 0, scaleX, scaleY, 1f)
        Matrix.rotateM(modelMatrix, 0, rotation, 0f, 0f, 1f)
        Matrix.translateM(modelMatrix, 0, transX, transY, 0f)

        // Compute MVP
        Matrix.multiplyMM(tempMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, tempMatrix, 0)

        // Bind texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES10.GL_TEXTURE_2D, textureId)

        // Set uniforms
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)
        GLES20.glUniform1i(textureHandle, 0)
        GLES20.glUniform1f(alphaHandle, alpha)

        // Enable blending if needed
        if (alpha < 1f) {
            GLES20.glEnable(GLES20.GL_BLEND)
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        }

        // Draw quad
        drawQuad(cropLeft, cropTop, cropRight, cropBottom)
    }

    /**
     * Render frame using texture coordinates.
     */
    private fun drawQuad(
        cropLeft: Float = 0f,
        cropTop: Float = 0f,
        cropRight: Float = 1f,
        cropBottom: Float = 1f
    ) {
        val vertices = floatArrayOf(
            -1f, -1f, 0f, cropLeft, cropBottom,
             1f, -1f, 0f, cropRight, cropBottom,
            -1f,  1f, 0f, cropLeft, cropTop,
             1f,  1f, 0f, cropRight, cropTop
        )

        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 20, vertices, 0)
        GLES20.glEnableVertexAttribArray(positionHandle)

        GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 20, vertices, 12)
        GLES20.glEnableVertexAttribArray(texCoordHandle)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(texCoordHandle)
    }

    /**
     * Set projection.
     */
    fun setProjection(width: Int, height: Int, isPortrait: Boolean = false) {
        val left = 0f
        val right = width.toFloat()
        val bottom = height.toFloat()
        val top = 0f

        Matrix.orthoM(projectionMatrix, 0, left, right, bottom, top, -1f, 1f)
    }

    /**
     * Set view.
     */
    fun setView(viewportWidth: Int, viewportHeight: Int) {
        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f)
    }

    /**
     * Create shader program.
     */
    private fun createProgram(vertexSource: String, fragmentSource: String): Int {
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource)

        val program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        GLES20.glLinkProgram(program)

        return program
    }

    /**
     * Load shader.
     */
    private fun loadShader(type: Int, source: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, source)
        GLES20.glCompileShader(shader)

        return shader
    }

    private val GLES10 = 0
}