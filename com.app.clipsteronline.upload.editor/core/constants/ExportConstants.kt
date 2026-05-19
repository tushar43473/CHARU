package com.app.clipsteronline.upload.editor.core.constants

/**
 * Constants for export operations.
 * Contains resolutions, codecs, bitrates, quality presets, and container formats.
 */
object ExportConstants {

    // Resolution presets
    object Resolution {
        const val HD_720P_WIDTH = 1280
        const val HD_720P_HEIGHT = 720
        const val FHD_1080P_WIDTH = 1920
        const val FHD_1080P_HEIGHT = 1080
        const val QHD_1440P_WIDTH = 2560
        const val QHD_1440P_HEIGHT = 1440
        const val UHD_4K_WIDTH = 3840
        const val UHD_4K_HEIGHT = 2160
        const val UHD_8K_WIDTH = 7680
        const val UHD_8K_HEIGHT = 4320

        // Social media resolutions
        const val SQUARE_1080 = 1080
        const val SQUARE_720 = 720
        const val PORTRAIT_1080 = 1080
        const val PORTRAIT_720 = 720
        const val STORY_1080_WIDTH = 1080
        const val STORY_1080_HEIGHT = 1920
    }

    // Aspect ratio presets for export
    const val EXPORT_RATIO_9_16 = 9f / 16f
    const val EXPORT_RATIO_16_9 = 16f / 9f
    const val EXPORT_RATIO_1_1 = 1f
    const val EXPORT_RATIO_4_5 = 4f / 5f
    const val EXPORT_RATIO_21_9 = 21f / 9f

    // Video codecs
    const val CODEC_H264 = "h264"
    const val CODEC_H265 = "h265"
    const val CODEC_HEVC = "hevc"
    const val CODEC_VP9 = "vp9"
    const val CODEC_AV1 = "av1"
    const val CODEC_PRORES = "prores"
    const val CODEC_DEFAULT = CODEC_H264

    // H.264 profiles
    const val H264_PROFILE_BASELINE = "baseline"
    const val H264_PROFILE_MAIN = "main"
    const val H264_PROFILE_HIGH = "high"
    const val H264_PROFILE_HIGH_10 = "high10"
    const val H264_PROFILE_DEFAULT = H264_PROFILE_HIGH

    // H.264 levels
    const val H264_LEVEL_3_1 = "3.1"
    const val H264_LEVEL_4_0 = "4.0"
    const val H264_LEVEL_4_1 = "4.1"
    const val H264_LEVEL_5_0 = "5.0"
    const val H264_LEVEL_5_1 = "5.1"
    const val H264_LEVEL_DEFAULT = H264_LEVEL_4_0

    // H.265 profiles
    const val H265_PROFILE_MAIN = "main"
    const val H265_PROFILE_MAIN_10 = "main10"
    const val H265_PROFILE_DEFAULT = H265_PROFILE_MAIN

    // Bitrate presets (bps)
    const val BITRATE_LOW = 2_000_000 // 2 Mbps
    const val BITRATE_MEDIUM = 8_000_000 // 8 Mbps
    const val BITRATE_HIGH = 15_000_000 // 15 Mbps
    const val BITRATE_VERY_HIGH = 25_000_000 // 25 Mbps
    const val BITRATE_4K = 50_000_000 // 50 Mbps

    // Resolution-specific bitrates
    const val BITRATE_720P = 5_000_000 // 5 Mbps
    const val BITRATE_1080P = 10_000_000 // 10 Mbps
    const val BITRATE_1440P = 20_000_000 // 20 Mbps
    const val BITRATE_4K_SOURCE = 35_000_000 // 35 Mbps from source

    // Variable bitrate settings
    const val VBR_ENABLED = true
    const val VBR_CRF_MIN = 18
    const val VBR_CRF_MAX = 28
    const val VBR_CRF_DEFAULT = 23

    // Container formats
    const val CONTAINER_MP4 = "mp4"
    const val CONTAINER_MKV = "mkv"
    const val CONTAINER_WEBM = "webm"
    const val CONTAINER_MOV = "mov"
    const val CONTAINER_AVI = "avi"
    const val CONTAINER_DEFAULT = CONTAINER_MP4

    // Audio codecs
    const val AUDIO_AAC = "aac"
    const val AUDIO_MP3 = "mp3"
    const val AUDIO_OPUS = "opus"
    const val AUDIO_VORBIS = "vorbis"
    const val AUDIO_PCM = "pcm"
    const val AUDIO_DEFAULT = AUDIO_AAC

    // Audio bitrates (bps)
    const val AUDIO_BITRATE_128 = 128_000
    const val AUDIO_BITRATE_160 = 160_000
    const val AUDIO_BITRATE_192 = 192_000
    const val AUDIO_BITRATE_256 = 256_000
    const val AUDIO_BITRATE_320 = 320_000
    const val AUDIO_BITRATE_DEFAULT = AUDIO_BITRATE_256

    // Audio sample rates (Hz)
    const val AUDIO_SAMPLE_RATE_44100 = 44100
    const val AUDIO_SAMPLE_RATE_48000 = 48000
    const val AUDIO_SAMPLE_RATE_96000 = 96000
    const val AUDIO_SAMPLE_RATE_DEFAULT = AUDIO_SAMPLE_RATE_48000

    // Audio channels
    const val AUDIO_CHANNELS_MONO = 1
    const val AUDIO_CHANNELS_STEREO = 2
    const val AUDIO_CHANNELS_5_1 = 6
    const val AUDIO_CHANNELS_7_1 = 8
    const val AUDIO_CHANNELS_DEFAULT = AUDIO_CHANNELS_STEREO

    // Quality presets
    const val QUALITY_LOW = "low"
    const val QUALITY_MEDIUM = "medium"
    const val QUALITY_HIGH = "high"
    const val QUALITY_VERY_HIGH = "very_high"
    const val QUALITY_LOSSLESS = "lossless"
    const val QUALITY_DEFAULT = QUALITY_HIGH

    // Quality to bitrate mapping
    val QUALITY_BITRATE_MAP = mapOf(
        QUALITY_LOW to BITRATE_LOW,
        QUALITY_MEDIUM to BITRATE_MEDIUM,
        QUALITY_HIGH to BITRATE_HIGH,
        QUALITY_VERY_HIGH to BITRATE_VERY_HIGH
    )

    // Frame rates for export
    const val FRAME_RATE_24 = 24
    const val FRAME_RATE_25 = 25
    const val FRAME_RATE_30 = 30
    const val FRAME_RATE_50 = 50
    const val FRAME_RATE_60 = 60
    const val FRAME_RATE_DEFAULT = FRAME_RATE_30

    // Keyframe intervals
    const val KEYFRAME_INTERVAL_1 = 1
    const val KEYFRAME_INTERVAL_2 = 2
    const val KEYFRAME_INTERVAL_5 = 5
    const val KEYFRAME_INTERVAL_10 = 10
    const val KEYFRAME_INTERVAL_AUTO = 0
    const val KEYFRAME_INTERVAL_DEFAULT = KEYFRAME_INTERVAL_2

    // GOP size (frames)
    const val GOP_SIZE_MIN = 1
    const val GOP_SIZE_MAX = 500
    const val GOP_SIZE_DEFAULT = 60

    // B-frames
    const val B_FRAMES_MIN = 0
    const val B_FRAMES_MAX = 5
    const val B_FRAMES_DEFAULT = 2

    // Reference frames
    const val REFERENCE_FRAMES_MIN = 1
    const val REFERENCE_FRAMES_MAX = 16
    const val REFERENCE_FRAMES_DEFAULT = 3

    // Deblocking
    const val DEBLOCK_ENABLED = true
    const val DEBLOCK_ALPHA = 0
    const val DEBLOCK_BETA = 0

    // Scene change detection
    const val SCENE_CHANGE_DETECTION_ENABLED = true
    const val SCENE_CHANGE_THRESHOLD = 0.4f

    // Two-pass encoding
    const val TWO_PASS_ENABLED = true
    const val TWO_PASS_DEFAULT = false

    // Hardware acceleration
    const val HW_ENCODING_NVENC = "nvenc"
    const val HW_ENCODING_QSV = "qsv"
    const val HW_ENCODING_AMF = "amf"
    const val HW_ENCODING_ENABLED = true

    // Export progress
    const val PROGRESS_UPDATE_INTERVAL_MS = 500L
    const val PROGRESS_STEP_PERCENT = 5

    // Time limits
    const val EXPORT_TIMEOUT_MS = 3600000L // 1 hour
    const val EXPORT_CHUNK_SIZE_MS = 60000L // 1 minute

    // File size limits
    const val FILE_SIZE_LIMIT_2GB = 2L * 1024 * 1024 * 1024
    const val FILE_SIZE_LIMIT_4GB = 4L * 1024 * 1024 * 1024

    // FFmpeg presets (encoding speed vs quality)
    const val FFPRESET_ULTRAFAST = "ultrafast"
    const val FFPRESET_SUPERFAST = "superfast"
    const val FFPRESET_VERYFAST = "veryfast"
    const val FFPRESET_FAST = "fast"
    const val FFPRESET_MEDIUM = "medium"
    const val FFPRESET_SLOW = "slow"
    const val FFPRESET_SLOWER = "slower"
    const val FFPRESET_VERYSLOW = "veryslow"
    const val FFPRESET_DEFAULT = FFPRESET_MEDIUM

    // FFmpeg tune options
    const val FFTUNE_FILM = "film"
    const val FFTUNE_ANIMATION = "animation"
    const val FFTUNE_GRAIN = "grain"
    const val FFTUNE_STILLIMAGE = "stillimage"
    const val FFTUNE_DEFAULT = FFTUNE_FILM

    // Metadata
    const val METADATA_TITLE_MAX_LENGTH = 256
    const val METADATA_AUTHOR_MAX_LENGTH = 128
    const val METADATA_COPYRIGHT_MAX_LENGTH = 256

    // Output
    const val OUTPUT_PREFIX = "export_"
    const val OUTPUT_TEMP_SUFFIX = ".tmp"
    const val OUTPUT_BACKUP_SUFFIX = ".bak"
}