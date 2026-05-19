package com.app.clipsteronline.upload.editor.render

import android.opengl.GLES20
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.util.LinkedHashMap

/**
 * Texture pool for efficient GPU resource reuse.
 * Manages texture allocation and caching.
 */
class TexturePool(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {
    private val freeTextures = mutableListOf<Int>()
    private val usedTextures = mutableSetOf<Int>()
    private val textureSizes = mutableMapOf<Int, Pair<Int, Int>>()

    private var maxPoolSize = 16

    /**
     * Allocate texture.
     */
    fun allocate(width: Int, height: Int): Int {
        // Find reusable texture
        val cached = findReusable(width, height)
        if (cached != null) {
            usedTextures.add(cached)
            return cached
        }

        // Create new texture
        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        val textureId = textures[0]

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glTexImage2D(
            GLES20.GL_TEXTURE_2D, 0,
            GLES20.GL_RGBA, width, height,
            0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null
        )

        // Set parameters
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)

        usedTextures.add(textureId)
        textureSizes[textureId] = width to height

        return textureId
    }

    /**
     * Find reusable texture.
     */
    private fun findReusable(width: Int, height: Int): Int? {
        for (textureId in freeTextures) {
            val size = textureSizes[textureId] ?: continue
            if (size.first >= width && size.second >= height) {
                freeTextures.remove(textureId)
                usedTextures.add(textureId)
                return textureId
            }
        }
        return null
    }

    /**
     * Release texture.
     */
    fun release(textureId: Int) {
        usedTextures.remove(textureId)
        freeTextures.add(textureId)

        // Trim pool if too large
        while (freeTextures.size + usedTextures.size > maxPoolSize) {
            freeTextures.firstOrNull()?.let { oldest ->
                freeTextures.remove(oldest)
                deleteTexture(oldest)
            }
        }
    }

    /**
     * Delete texture.
     */
    private fun deleteTexture(textureId: Int) {
        val textures = intArrayOf(textureId)
        GLES20.glDeleteTextures(1, textures, 0)
        textureSizes.remove(textureId)
    }

    /**
     * Clear all textures.
     */
    fun clear() {
        for (textureId in freeTextures) {
            deleteTexture(textureId)
        }
        for (textureId in usedTextures) {
            deleteTexture(textureId)
        }
        freeTextures.clear()
        usedTextures.clear()
    }

    /**
     * Set pool size.
     */
    fun setPoolSize(size: Int) {
        maxPoolSize = size.coerceIn(1, 64)
    }
}

/**
 * Framebuffer pool for render target reuse.
 */
class FramebufferPool(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {
    private val freeFBOs = mutableListOf<Int>()
    private val usedFBOs = mutableSetOf<Int>()
    private val fboSizes = mutableMapOf<Int, Pair<Int, Int>>()

    private var maxPoolSize = 8

    /**
     * Allocate framebuffer.
     */
    fun allocate(width: Int, height: Int): Int {
        val cached = findReusable(width, height)
        if (cached != null) {
            usedFBOs.add(cached)
            return cached
        }

        val framebuffers = IntArray(1)
        GLES20.glGenFramebuffers(1, framebuffers, 0)
        val fbo = framebuffers[0]

        usedFBOs.add(fbo)
        fboSizes[fbo] = width to height

        return fbo
    }

    /**
     * Find reusable framebuffer.
     */
    private fun findReusable(width: Int, height: Int): Int? {
        for (fbo in freeFBOs) {
            val size = fboSizes[fbo] ?: continue
            if (size.first >= width && size.second >= height) {
                freeFBOs.remove(fbo)
                return fbo
            }
        }
        return null
    }

    /**
     * Release framebuffer.
     */
    fun release(fbo: Int) {
        usedFBOs.remove(fbo)
        freeFBOs.add(fbo)

        while (freeFBOs.size + usedFBOs.size > maxPoolSize) {
            freeFBOs.firstOrNull()?.let { oldest ->
                freeFBOs.remove(oldest)
                deleteFBO(oldest)
            }
        }
    }

    /**
     * Delete framebuffer.
     */
    private fun deleteFBO(fbo: Int) {
        val framebuffers = intArrayOf(fbo)
        GLES20.glDeleteFramebuffers(1, framebuffers, 0)
        fboSizes.remove(fbo)
    }

    /**
     * Clear all.
     */
    fun clear() {
        freeFBOs.forEach { deleteFBO(it) }
        usedFBOs.forEach { deleteFBO(it) }
        freeFBOs.clear()
        usedFBOs.clear()
    }
}