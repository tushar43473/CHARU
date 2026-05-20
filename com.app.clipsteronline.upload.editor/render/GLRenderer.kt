package com.app.clipsteronline.upload.editor.render

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class GLRenderer(
    private val shaderRenderer: ShaderRenderer = ShaderRenderer(),
) {
    private var programId: Int = 0
    private var posAttr = -1
    private var uvAttr = -1
    private var texUniform = -1

    private val vertexBuffer: FloatBuffer = ByteBuffer
        .allocateDirect(VERTICES.size * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .apply { put(VERTICES).position(0) }

    private val uvBuffer: FloatBuffer = ByteBuffer
        .allocateDirect(UVS.size * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .apply { put(UVS).position(0) }

    fun initialize(): Boolean {
        programId = shaderRenderer.createProgram(VERTEX_SHADER, FRAGMENT_SHADER)
        if (programId == 0) return false
        posAttr = GLES20.glGetAttribLocation(programId, "aPosition")
        uvAttr = GLES20.glGetAttribLocation(programId, "aTexCoord")
        texUniform = GLES20.glGetUniformLocation(programId, "uTexture")
        return posAttr >= 0 && uvAttr >= 0 && texUniform >= 0
    }

    fun draw(textureId: Int, width: Int, height: Int) {
        if (programId == 0 || textureId == 0 || width <= 0 || height <= 0) return
        GLES20.glViewport(0, 0, width, height)
        GLES20.glUseProgram(programId)

        GLES20.glEnableVertexAttribArray(posAttr)
        GLES20.glVertexAttribPointer(posAttr, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer)
        GLES20.glEnableVertexAttribArray(uvAttr)
        GLES20.glVertexAttribPointer(uvAttr, 2, GLES20.GL_FLOAT, false, 0, uvBuffer)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glUniform1i(texUniform, 0)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        GLES20.glDisableVertexAttribArray(posAttr)
        GLES20.glDisableVertexAttribArray(uvAttr)
    }

    fun release() {
        if (programId != 0) {
            GLES20.glDeleteProgram(programId)
            programId = 0
        }
    }

    companion object {
        private val VERTICES = floatArrayOf(-1f, -1f, 1f, -1f, -1f, 1f, 1f, 1f)
        private val UVS = floatArrayOf(0f, 1f, 1f, 1f, 0f, 0f, 1f, 0f)

        private const val VERTEX_SHADER = """
            attribute vec4 aPosition;
            attribute vec2 aTexCoord;
            varying vec2 vTexCoord;
            void main() {
                gl_Position = aPosition;
                vTexCoord = aTexCoord;
            }
        """

        private const val FRAGMENT_SHADER = """
            precision mediump float;
            varying vec2 vTexCoord;
            uniform sampler2D uTexture;
            void main() {
                gl_FragColor = texture2D(uTexture, vTexCoord);
            }
        """
    }
}
