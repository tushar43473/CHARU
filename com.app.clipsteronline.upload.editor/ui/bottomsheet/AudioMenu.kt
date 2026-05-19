package com.app.clipsteronline.upload.editor.ui.bottomsheet

import android.animation.ValueAnimator
import android.content.Context
import android.view.View
import android.widget.LinearLayout
import android.widget.SeekBar

/**
 * Bottom audio menu.
 * Volume, fade, beat sync, voice effects.
 */
class AudioMenu(context: Context) : LinearLayout(context) {

    private var volumeSlider: SeekBar? = null
    private var audioCallback: ((AudioAction) -> Unit)? = null

    /**
     * Set audio callback.
     */
    fun setOnAudioActionListener(callback: (AudioAction) -> Unit) {
        audioCallback = callback
    }

    /**
     * Set volume level.
     */
    fun setVolume(level: Float) {
        volumeSlider?.progress = (level * 100).toInt()
    }

    /**
     * Audio action data.
     */
    data class AudioAction(
        val type: String,
        val value: Float = 0f
    )
}

/**
 * Audio sliders container.
 */
class AudioSlidersView(context: Context) : LinearLayout(context) {

    private var volumeSeekBar: SeekBar? = null
    private var fadeInSeekBar: SeekBar? = null
    private var fadeOutSeekBar: SeekBar? = null

    fun initialize() {
        orientation = VERTICAL
        setPadding(32, 16, 32, 16)

        // Volume slider
        addSlider("Volume", 100).apply {
            volumeSeekBar = this
        }

        // Fade in slider
        addSlider("Fade In", 0).apply {
            fadeInSeekBar = this
        }

        // Fade out slider
        addSlider("Fade Out", 0).apply {
            fadeOutSeekBar = this
        }
    }

    private fun addSlider(label: String, initialProgress: Int): SeekBar {
        return SeekBar(context).apply {
            max = 100
            progress = initialProgress
            setBackgroundColor(0xFF333333.toInt())
        }
    }

    /**
     * Get volume level.
     */
    fun getVolume(): Float = (volumeSeekBar?.progress ?: 0) / 100f

    /**
     * Get fade in.
     */
    fun getFadeIn(): Float = (fadeInSeekBar?.progress ?: 0) / 100f

    /**
     * Get fade out.
     */
    fun getFadeOut(): Float = (fadeOutSeekBar?.progress ?: 0) / 100f
}