package com.app.clipsteronline.upload.editor.app

import com.app.clipsteronline.upload.editor.core.constants.EditorConstants
import com.app.clipsteronline.upload.editor.core.constants.ExportConstants
import com.app.clipsteronline.upload.editor.core.constants.PlayerConstants
import com.app.clipsteronline.upload.editor.core.constants.RenderConstants
import com.app.clipsteronline.upload.editor.core.constants.TimelineConstants

data class EditorConfig(
    val performanceProfile: PerformanceProfile = PerformanceProfile.BALANCED,
    val debugMode: Boolean = false,
    val timeline: TimelineConfig = TimelineConfig(),
    val render: RenderConfig = RenderConfig(),
    val playback: PlaybackConfig = PlaybackConfig(),
    val export: ExportConfig = ExportConfig(),
    val cache: CacheConfig = CacheConfig(),
) {
    enum class PerformanceProfile { LOW_END, BALANCED, HIGH_QUALITY }

    data class TimelineConfig(
        val minZoom: Float = TimelineConstants.MIN_ZOOM,
        val maxZoom: Float = TimelineConstants.MAX_ZOOM,
        val defaultZoom: Float = TimelineConstants.DEFAULT_ZOOM,
        val snapThresholdMs: Long = TimelineConstants.SNAP_THRESHOLD_MS,
    )

    data class RenderConfig(
        val targetFps: Int = RenderConstants.PREVIEW_RENDER_FPS,
        val textureLimit: Int = RenderConstants.SAFE_TEXTURE_LIMIT,
        val hardwareAcceleration: Boolean = RenderConstants.ENABLE_HARDWARE_ACCELERATION,
    )

    data class PlaybackConfig(
        val minSpeed: Float = PlayerConstants.MIN_PLAYBACK_SPEED,
        val maxSpeed: Float = PlayerConstants.MAX_PLAYBACK_SPEED,
        val defaultSpeed: Float = PlayerConstants.DEFAULT_PLAYBACK_SPEED,
        val bufferMinMs: Int = PlayerConstants.BUFFER_MIN_MS,
        val bufferMaxMs: Int = PlayerConstants.BUFFER_MAX_MS,
    )

    data class ExportConfig(
        val defaultResolution: String = ExportConstants.RES_1080P,
        val defaultFps: Int = ExportConstants.FPS_30,
        val defaultBitrate: Int = ExportConstants.BITRATE_1080P,
        val defaultCodec: String = ExportConstants.CODEC_H264,
        val defaultContainer: String = ExportConstants.CONTAINER_MP4,
    )

    data class CacheConfig(
        val thumbnailLimit: Int = 300,
        val frameLimit: Int = PlayerConstants.PREVIEW_CACHE_FRAMES,
        val diskCacheMb: Int = 1024,
    )

    companion object {
        fun forProfile(profile: PerformanceProfile): EditorConfig {
            return when (profile) {
                PerformanceProfile.LOW_END -> EditorConfig(
                    performanceProfile = profile,
                    timeline = TimelineConfig(maxZoom = 8f),
                    render = RenderConfig(targetFps = 24, textureLimit = RenderConstants.DEFAULT_TEXTURE_LIMIT),
                    export = ExportConfig(defaultResolution = ExportConstants.RES_720P, defaultBitrate = ExportConstants.BITRATE_720P),
                    cache = CacheConfig(thumbnailLimit = 180, frameLimit = 12, diskCacheMb = 512),
                )
                PerformanceProfile.BALANCED -> EditorConfig(performanceProfile = profile)
                PerformanceProfile.HIGH_QUALITY -> EditorConfig(
                    performanceProfile = profile,
                    timeline = TimelineConfig(maxZoom = 12f),
                    render = RenderConfig(targetFps = EditorConstants.HIGH_FPS, textureLimit = RenderConstants.FLAGSHIP_TEXTURE_LIMIT),
                    export = ExportConfig(defaultResolution = ExportConstants.RES_4K, defaultBitrate = ExportConstants.BITRATE_4K, defaultCodec = ExportConstants.CODEC_H265),
                    cache = CacheConfig(thumbnailLimit = 500, frameLimit = 40, diskCacheMb = 2048),
                )
            }
        }
    }
}
