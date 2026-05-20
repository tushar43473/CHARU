package upload.editor.core.constants

object ExportConstants {
    const val RESOLUTION_540P = "960x540"
    const val RESOLUTION_720P = "1280x720"
    const val RESOLUTION_1080P = "1920x1080"
    const val RESOLUTION_2K = "2560x1440"
    const val RESOLUTION_4K = "3840x2160"

    const val CODEC_H264 = "libx264"
    const val CODEC_H265 = "libx265"
    const val CODEC_AV1 = "libaom-av1"

    const val BITRATE_540P = 2_500_000
    const val BITRATE_720P = 4_500_000
    const val BITRATE_1080P = 8_000_000
    const val BITRATE_2K = 16_000_000
    const val BITRATE_4K = 35_000_000

    const val QUALITY_DRAFT_CRF = 28
    const val QUALITY_STANDARD_CRF = 23
    const val QUALITY_HIGH_CRF = 20
    const val QUALITY_MASTER_CRF = 17

    const val CONTAINER_MP4 = "mp4"
    const val CONTAINER_MOV = "mov"
    const val CONTAINER_MKV = "mkv"

    const val AUDIO_CODEC_AAC = "aac"
    const val AUDIO_CODEC_OPUS = "libopus"
    const val AUDIO_SAMPLE_RATE_44K = 44_100
    const val AUDIO_SAMPLE_RATE_48K = 48_000
    const val AUDIO_BITRATE_STANDARD = 192_000
    const val AUDIO_BITRATE_HIGH = 320_000

    const val FFMPEG_PRESET_VERYFAST = "veryfast"
    const val FFMPEG_PRESET_FAST = "fast"
    const val FFMPEG_PRESET_MEDIUM = "medium"
    const val FFMPEG_PRESET_SLOW = "slow"
}
