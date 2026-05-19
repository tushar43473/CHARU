package com.app.clipsteronline.upload.editor.sticker

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Animated sticker playback.
 * Frame sequencing and animation control.
 */
class AnimatedSticker(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
) {
    private val _animationState = MutableStateFlow(AnimationState())
    val animationState: StateFlow<AnimationState> = _animationState.asStateFlow()

    private var frameCount = 0
    private var currentFrame = 0
    private var frameRate = 30
    private var isPlaying = false
    private var isLooping = true
    private var frameDurationMs = 33L // Default 30fps

    private val frameCache = mutableListOf<Bitmap>()
    private var frameDelayMs = longArrayOf()

    private val mainHandler = Handler(Looper.getMainLooper())
    private var frameRunnable: Runnable? = null

    /**
     * Load frames.
     */
    fun loadFrames(framePaths: List<String>) {
        frameCount = framePaths.size
        frameCache.clear()

        for (path in framePaths) {
            try {
                val bitmap = BitmapFactory.decodeFile(path)
                frameCache.add(bitmap)
            } catch (e: Exception) {
                // Handle error
            }
        }

        // Default timing
        frameDelayMs = LongArray(frameCount) { frameDurationMs }

        _animationState.value = _animationState.value.copy(
            frameCount = frameCount,
            isLoaded = true
        )
    }

    /**
     * Load frames from resource.
     */
    fun loadFrames(frames: List<Bitmap>) {
        frameCount = frames.size
        frameCache.clear()
        frameCache.addAll(frames)

        frameDelayMs = LongArray(frameCount) { frameDurationMs }

        _animationState.value = _animationState.value.copy(
            frameCount = frameCount,
            isLoaded = true
        )
    }

    /**
     * Play animation.
     */
    fun play() {
        if (isPlaying || frameCount == 0) return

        isPlaying = true
        _animationState.value = _animationState.value.copy(isPlaying = true)

        scheduleNextFrame()
    }

    /**
     * Pause animation.
     */
    fun pause() {
        isPlaying = false
        frameRunnable?.let { mainHandler.removeCallbacks(it) }
        frameRunnable = null

        _animationState.value = _animationState.value.copy(isPlaying = false)
    }

    /**
     * Stop animation.
     */
    fun stop() {
        pause()
        currentFrame = 0
        _animationState.value = _animationState.value.copy(
            currentFrame = 0,
            progress = 0f
        )
    }

    /**
     * Seek to frame.
     */
    fun seekToFrame(frame: Int) {
        currentFrame = frame.coerceIn(0, frameCount - 1)
        _animationState.value = _animationState.value.copy(
            currentFrame = currentFrame,
            progress = currentFrame.toFloat() / frameCount
        )
    }

    /**
     * Get current frame.
     */
    fun getCurrentFrame(): Int = currentFrame

    /**
     * Get frame bitmap.
     */
    fun getCurrentBitmap(): Bitmap? {
        return frameCache.getOrNull(currentFrame)
    }

    /**
     * Set frame rate.
     */
    fun setFrameRate(fps: Int) {
        frameRate = fps.coerceIn(1, 60)
        frameDurationMs = 1000L / fps
    }

    /**
     * Set looping.
     */
    fun setLooping(loop: Boolean) {
        isLooping = loop
    }

    /**
     * Set frame delay.
     */
    fun setFrameDelay(frameIndex: Int, delayMs: Long) {
        if (frameIndex in 0 until frameCount) {
            frameDelayMs[frameIndex] = delayMs
        }
    }

    /**
     * Schedule next frame.
     */
    private fun scheduleNextFrame() {
        if (!isPlaying) return

        frameRunnable = Runnable {
            advanceFrame()
            if (isPlaying) {
                scheduleNextFrame()
            }
        }

        val delay = frameDelayMs.getOrElse(currentFrame) { frameDurationMs }
        mainHandler.postDelayed(frameRunnable!!, delay)
    }

    /**
     * Advance frame.
     */
    private fun advanceFrame() {
        currentFrame++

        if (currentFrame >= frameCount) {
            if (isLooping) {
                currentFrame = 0
            } else {
                pause()
                return
            }
        }

        _animationState.value = _animationState.value.copy(
            currentFrame = currentFrame,
            progress = currentFrame.toFloat() / frameCount
        )
    }

    /**
     * Release resources.
     */
    fun release() {
        pause()
        frameRunnable?.let { mainHandler.removeCallbacks(it) }

        frameCache.forEach { it.recycle() }
        frameCache.clear()
        frameCount = 0
    }
}

/**
 * Animation state.
 */
data class AnimationState(
    val isPlaying: Boolean = false,
    val isLoaded: Boolean = false,
    val currentFrame: Int = 0,
    val frameCount: Int = 0,
    val progress: Float = 0f
)