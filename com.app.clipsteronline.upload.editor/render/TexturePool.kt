package com.app.clipsteronline.upload.editor.render

import android.opengl.GLES20
import java.util.ArrayDeque

class TexturePool(
    private val maxSize: Int = 24,
) {
    private val free = ArrayDeque<Int>()
    private val inUse = LinkedHashSet<Int>()

    @Synchronized
    fun acquire(): Int {
        if (free.isNotEmpty()) {
            val id = free.removeFirst()
            inUse.add(id)
            return id
        }
        val id = createTexture()
        if (id != 0) inUse.add(id)
        return id
    }

    @Synchronized
    fun release(textureId: Int) {
        if (textureId == 0) return
        if (!inUse.remove(textureId)) return
        if (free.size >= maxSize) {
            GLES20.glDeleteTextures(1, intArrayOf(textureId), 0)
            return
        }
        free.addLast(textureId)
    }

    @Synchronized
    fun clear() {
        val all = IntArray(free.size + inUse.size)
        var i = 0
        free.forEach { all[i++] = it }
        inUse.forEach { all[i++] = it }
        if (all.isNotEmpty()) GLES20.glDeleteTextures(all.size, all, 0)
        free.clear()
        inUse.clear()
    }

    private fun createTexture(): Int {
        val arr = IntArray(1)
        GLES20.glGenTextures(1, arr, 0)
        val id = arr[0]
        if (id == 0) return 0
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, id)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        return id
    }
}
