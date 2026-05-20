package com.app.clipsteronline.upload.editor.core.model

data class ExportConfig(
    val width: Int,
    val height: Int,
    val fps: Int,
    val videoBitrate: Int,
    val videoCodec: VideoCodec,
    val container: Container,
    val audioConfig: AudioConfig,
    val useHardwareAcceleration: Boolean,
    val keyFrameIntervalSec: Int = 2,
) {
    init {
        require(width > 0 && height > 0)
        require(fps in 1..120)
        require(videoBitrate >= 500_000)
        require(keyFrameIntervalSec in 1..10)
    }

    enum class VideoCodec { H264, H265, AV1 }
    enum class Container { MP4, MOV, MKV }

    data class AudioConfig(
        val codec: AudioCodec,
        val sampleRate: Int,
        val bitrate: Int,
        val channels: Int,
    ) {
        init {
            require(sampleRate in 8_000..192_000)
            require(bitrate >= 32_000)
            require(channels in 1..8)
        }

        enum class AudioCodec { AAC, OPUS, PCM }
    }
}
