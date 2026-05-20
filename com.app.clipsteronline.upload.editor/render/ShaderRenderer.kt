package com.app.clipsteronline.upload.editor.render

import android.opengl.GLES20

class ShaderRenderer {
    private val programCache = mutableMapOf<String, Int>()

    fun getOrCreateProgram(vertexSource: String, fragmentSource: String): Int {
        val key = vertexSource + "::" + fragmentSource
        return programCache[key]?.takeIf { it != 0 } ?: createProgram(vertexSource, fragmentSource).also {
            if (it != 0) programCache[key] = it
        }
    }

    fun setUniform(program: Int, name: String, value: Float) {
        if (program == 0) return
        val location = GLES20.glGetUniformLocation(program, name)
        if (location >= 0) GLES20.glUniform1f(location, value)
    }

    fun createProgram(vertexSource: String, fragmentSource: String): Int {
        val vertex = compileShader(GLES20.GL_VERTEX_SHADER, vertexSource)
        val fragment = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource)
        if (vertex == 0 || fragment == 0) {
            if (vertex != 0) GLES20.glDeleteShader(vertex)
            if (fragment != 0) GLES20.glDeleteShader(fragment)
            return 0
        }
        val program = GLES20.glCreateProgram()
        if (program == 0) return 0
        GLES20.glAttachShader(program, vertex)
        GLES20.glAttachShader(program, fragment)
        GLES20.glLinkProgram(program)

        val status = IntArray(1)
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, status, 0)
        GLES20.glDeleteShader(vertex)
        GLES20.glDeleteShader(fragment)

        if (status[0] == 0) {
            GLES20.glDeleteProgram(program)
            return 0
        }
        return program
    }

    fun compileShader(type: Int, source: String): Int {
        val shader = GLES20.glCreateShader(type)
        if (shader == 0) return 0
        GLES20.glShaderSource(shader, source)
        GLES20.glCompileShader(shader)
        val status = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, status, 0)
        if (status[0] == 0) {
            GLES20.glDeleteShader(shader)
            return 0
        }
        return shader
    }

    fun clearCache() {
        programCache.values.forEach { if (it != 0) GLES20.glDeleteProgram(it) }
        programCache.clear()
    }
}
