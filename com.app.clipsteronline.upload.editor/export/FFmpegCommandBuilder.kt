package com.app.clipsteronline.upload.editor.export

/**
 * Builds FFmpeg commands for video export.
 * Handles codec selection, bitrate, resolution, and hardware acceleration.
 */
class FFmpegCommandBuilder {

    private var inputPath: String? = null
    private var outputPath: String? = null
    private var videoCodec = "libx264"
    private var audioCodec = "aac"
    private var videoBitrate = "8M"
    private var audioBitrate = "192k"
    private var width = 1920
    private var height = 1080
    private var frameRate = 30
    private var preset = "medium"
    private var crf = 23
    private var useHardwareAcceleration = false
    private var pixelFormat = "yuv420p"

    /**
     * Set input path.
     */
    fun setInput(path: String): FFmpegCommandBuilder {
        inputPath = path
        return this
    }

    /**
     * Set output path.
     */
    fun setOutput(path: String): FFmpegCommandBuilder {
        outputPath = path
        return this
    }

    /**
     * Set resolution.
     */
    fun setResolution(width: Int, height: Int): FFmpegCommandBuilder {
        this.width = width
        this.height = height
        return this
    }

    /**
     * Set frame rate.
     */
    fun setFrameRate(fps: Int): FFmpegCommandBuilder {
        this.frameRate = fps.coerceIn(1, 120)
        return this
    }

    /**
     * Set video bitrate.
     */
    fun setVideoBitrate(bitrate: String): FFmpegCommandBuilder {
        this.videoBitrate = bitrate
        return this
    }

    /**
     * Set audio bitrate.
     */
    fun setAudioBitrate(bitrate: String): FFmpegCommandBuilder {
        this.audioBitrate = bitrate
        return this
    }

    /**
     * Set video codec.
     */
    fun setVideoCodec(codec: String): FFmpegCommandBuilder {
        this.videoCodec = codec
        return this
    }

    /**
     * Set audio codec.
     */
    fun setAudioCodec(codec: String): FFmpegCommandBuilder {
        this.audioCodec = codec
        return this
    }

    /**
     * Set preset (encoding speed).
     */
    fun setPreset(preset: String): FFmpegCommandBuilder {
        this.preset = preset
        return this
    }

    /**
     * Set CRF (constant rate factor).
     */
    fun setCRF(value: Int): FFmpegCommandBuilder {
        this.crf = value.coerceIn(0, 51)
        return this
    }

    /**
     * Enable hardware acceleration.
     */
    fun setHardwareAcceleration(enabled: Boolean): FFmpegCommandBuilder {
        useHardwareAcceleration = enabled
        return this
    }

    /**
     * Set pixel format.
     */
    fun setPixelFormat(format: String): FFmpegCommandBuilder {
        this.pixelFormat = format
        return this
    }

    /**
     * Build command.
     */
    fun build(): List<String> {
        val args = mutableListOf<String>()

        // Input
        inputPath?.let { args.addAll(listOf("-i", it)) }

        // Video scaling
        if (width > 0 && height > 0) {
            args.addAll(listOf("-vf", "scale=$width:$height"))
        }

        // Frame rate
        args.addAll(listOf("-r", frameRate.toString()))

        // Video codec
        if (useHardwareAcceleration) {
            args.addAll(listOf("-c:v", "h264_mediacodec"))
        } else {
            args.addAll(listOf("-c:v", videoCodec))
        }

        // Video bitrate (CBR for streaming)
        args.addAll(listOf("-b:v", videoBitrate))

        // Preset (speed vs quality tradeoff)
        args.addAll(listOf("-preset", preset))

        // CRF (quality - lower is better)
        if (!useHardwareAcceleration && videoCodec == "libx264") {
            args.addAll(listOf("-crf", crf.toString()))
        }

        // Pixel format
        args.addAll(listOf("-pix_fmt", pixelFormat))

        // Audio codec
        args.addAll(listOf("-c:a", audioCodec))

        // Audio bitrate
        args.addAll(listOf("-b:a", audioBitrate))

        // Force overwrite
        args.add("-y")

        // Output
        outputPath?.let { args.add(it) }

        return args
    }

    /**
     * Build preset for quality.
     */
    fun forQuality(quality: ExportQuality): FFmpegCommandBuilder {
        return when (quality) {
            ExportQuality.LOW -> {
                setResolution(1280, 720)
                    .setVideoBitrate("2M")
                    .setAudioBitrate("128k")
                    .setPreset("veryfast")
                    .setCRF(28)
            }
            ExportQuality.MEDIUM -> {
                setResolution(1920, 1080)
                    .setVideoBitrate("8M")
                    .setAudioBitrate("192k")
                    .setPreset("medium")
                    .setCRF(23)
            }
            ExportQuality.HIGH -> {
                setResolution(3840, 2160)
                    .setVideoBitrate("25M")
                    .setAudioBitrate("320k")
                    .setPreset("slow")
                    .setCRF(18)
            }
            ExportQuality.MAX -> {
                setResolution(3840, 2160)
                    .setVideoBitrate("50M")
                    .setAudioBitrate("384k")
                    .setPreset("slower")
                    .setCRF(15)
            }
        }
    }

    /**
     * Get standard resolutions.
     */
    companion object {
        fun getResolution(quality: ExportQuality): Pair<Int, Int> {
            return when (quality) {
                ExportQuality.LOW -> 1280 to 720
                ExportQuality.MEDIUM -> 1920 to 1080
                ExportQuality.HIGH -> 2560 to 1440
                ExportQuality.MAX -> 3840 to 2160
            }
        }
    }
}

/**
 * Export quality presets.
 */
enum class ExportQuality {
    LOW,    // 720p
    MEDIUM,  // 1080p
    HIGH,    // 1440p
    MAX     // 4K
}