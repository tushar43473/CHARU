package com.app.clipsteronline.upload.editor.core.model

/**
 * Export configuration for video rendering.
 * Contains all settings needed for exporting a video.
 */
data class ExportConfig(
    val resolution: ExportResolution = ExportResolution.FHD_1080P,
    val frameRate: Int = 30,
    val bitRate: Int = 10_000_000,
    val codec: VideoCodec = VideoCodec.H264,
    val audioCodec: AudioCodec = AudioCodec.AAC,
    val container: ContainerFormat = ContainerFormat.MP4,
    val audioSettings: AudioExportSettings = AudioExportSettings(),
    val videoSettings: VideoExportSettings = VideoExportSettings(),
    val hardwareAcceleration: HardwareAcceleration = HardwareAcceleration.AUTO,
    val includeAudio: Boolean = true,
    val includeVideo: Boolean = true,
    val outputPath: String = "",
    val outputFileName: String = "export"
) {
    fun withResolution(resolution: ExportResolution): ExportConfig {
        return copy(resolution = resolution)
    }

    fun withFrameRate(frameRate: Int): ExportConfig {
        return copy(frameRate = frameRate.coerceIn(15, 120))
    }

    fun withBitRate(bitRate: Int): ExportConfig {
        return copy(bitRate = bitRate)
    }

    fun withCodec(codec: VideoCodec): ExportConfig {
        return copy(codec = codec)
    }

    fun withAudioCodec(codec: AudioCodec): ExportConfig {
        return copy(audioCodec = codec)
    }

    fun withContainer(container: ContainerFormat): ExportConfig {
        return copy(container = container)
    }

    fun withAudioSettings(settings: AudioExportSettings): ExportConfig {
        return copy(audioSettings = settings)
    }

    fun withVideoSettings(settings: VideoExportSettings): ExportConfig {
        return copy(videoSettings = settings)
    }

    fun withHardwareAcceleration(acceleration: HardwareAcceleration): ExportConfig {
        return copy(hardwareAcceleration = acceleration)
    }

    fun withoutAudio(): ExportConfig {
        return copy(includeAudio = false)
    }

    fun withoutVideo(): ExportConfig {
        return copy(includeVideo = false)
    }
}

/**
 * Export resolution presets.
 */
enum class ExportResolution(
    val displayName: String,
    val width: Int,
    val height: Int,
    val defaultBitRate: Int
) {
    SD_480P("480p", 640, 480, 2_000_000),
    HD_720P("720p", 1280, 720, 5_000_000),
    FHD_1080P("1080p", 1920, 1080, 10_000_000),
    QHD_1440P("1440p", 2560, 1440, 20_000_000),
    UHD_4K("4K", 3840, 2160, 35_000_000),
    UHD_8K("8K", 7680, 4320, 80_000_000),
    SQUARE_1080("Square 1080", 1080, 1080, 10_000_000),
    SQUARE_720("Square 720", 720, 720, 5_000_000),
    PORTRAIT_1080("Portrait 1080", 1080, 1920, 10_000_000),
    PORTRAIT_720("Portrait 720", 720, 1280, 5_000_000),
    STORY_1080("Story", 1080, 1920, 10_000_000);

    fun toSize(): Pair<Int, Int> = width to height
}

/**
 * Video codec options.
 */
enum class VideoCodec(
    val displayName: String,
    val mimeType: String,
    val supportsHDR: Boolean,
    val supportsAlpha: Boolean
) {
    H264("H.264", "video/avc", false, false),
    H265("H.265/HEVC", "video/hevc", true, false),
    VP8("VP8", "video/x-vnd.on2.vp8", false, false),
    VP9("VP9", "video/x-vnd.on2.vp9", true, false),
    AV1("AV1", "video/av01", true, false),
    PRORES("ProRes", "video/prores", true, true);

    fun supportsResolution(width: Int, height: Int): Boolean {
        return when (this) {
            H264 -> width <= 4096 && height <= 4096
            H265 -> width <= 8192 && height <= 8192
            VP8 -> width <= 4096 && height <= 4096
            VP9 -> width <= 8192 && height <= 8192
            AV1 -> width <= 8192 && height <= 8192
            PRORES -> width <= 8192 && height <= 8192
        }
    }
}

/**
 * Audio codec options.
 */
enum class AudioCodec(
    val displayName: String,
    val mimeType: String,
    val maxBitRate: Int
) {
    AAC("AAC", "audio/mp4a-latm", 512_000),
    AAC_HE("AAC-HE", "audio/eac3", 256_000),
    MP3("MP3", "audio/mpeg", 320_000),
    OPUS("Opus", "audio/opus", 510_000),
    VORBIS("Vorbis", "audio/vorbis", 500_000),
    PCM("PCM", "audio/raw", 1411_200);

    fun supportsSampleRate(sampleRate: Int): Boolean {
        return when (this) {
            AAC, AAC_HE -> sampleRate in listOf(8000, 11025, 12000, 16000, 22050, 24000, 32000, 44100, 48000, 64000, 88200, 96000)
            MP3 -> sampleRate in listOf(16000, 22050, 24000, 32000, 44100, 48000)
            else -> true
        }
    }
}

/**
 * Container format options.
 */
enum class ContainerFormat(
    val displayName: String,
    val extension: String,
    val mimeType: String,
    val supportsVideo: Boolean,
    val supportsAudio: Boolean
) {
    MP4("MP4", "mp4", "video/mp4", true, true),
    MKV("MKV/Matroska", "mkv", "video/x-matroska", true, true),
    WEBM("WebM", "webm", "video/webm", true, true),
    MOV("MOV/QuickTime", "mov", "video/quicktime", true, true),
    AVI("AVI", "avi", "video/x-msvideo", true, false),
    GIF("GIF", "gif", "image/gif", false, false);

    fun getFileName(baseName: String): String {
        return "$baseName.$extension"
    }
}

/**
 * Audio export settings.
 */
data class AudioExportSettings(
    val sampleRate: Int = 48000,
    val channelCount: Int = 2,
    val bitRate: Int = 256_000,
    val fadeInDurationMs: Long = 0L,
    val fadeOutDurationMs: Long = 0L,
    val normalize: Boolean = false
)

/**
 * Video export settings.
 */
data class VideoExportSettings(
    val profile: String = "high",
    val level: String = "4.0",
    val keyFrameInterval: Int = 2,
    val bFrames: Int = 2,
    val referenceFrames: Int = 3,
    val entropyMode: String = "cabac",
    val deblockEnabled: Boolean = true,
    val studyMode: Boolean = false
)

/**
 * Hardware acceleration options.
 */
enum class HardwareAcceleration(val displayName: String) {
    AUTO("Auto"),
    ON("On"),
    OFF("Off");

    fun shouldUse(): Boolean = this != OFF
}