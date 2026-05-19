package com.app.clipsteronline.upload.editor.app

/**
 * Editor configuration.
 * Global settings, debug flags.
 */
object EditorConfig {

    // Timeline Settings
    var DEFAULT_TIMELINE_ZOOM = 1f
    var TIMELINE_MIN_ZOOM = 0.1f
    var TIMELINE_MAX_ZOOM = 10f
    var SNAP_THRESHOLD_MS = 50L

    // Render Settings
    var DEFAULT_RESOLUTION_WIDTH = 1920
    var DEFAULT_RESOLUTION_HEIGHT = 1080
    var DEFAULT_FRAME_RATE = 30
    var MAX_FRAME_RATE = 60
    var USE_HARDWARE_DECODING = true
    var ENABLE_GL_RENDERING = true

    // Export Settings
    var DEFAULT_EXPORT_QUALITY = "1080p"
    var DEFAULT_EXPORT_FORMAT = "mp4"
    var DEFAULT_BITRATE = 10_000_000
    var ENABLE_HARDWARE_ENCODING = true

    // Memory Settings
    var MAX_BITMAP_CACHE_SIZE_MB = 50
    var MAX_THUMBNAIL_CACHE_SIZE_MB = 20
    var MAX_WAVEFORM_CACHE_SIZE_MB = 10
    var ENABLE_LOW_MEMORY_MODE = false

    // Playback Settings
    var PLAYBACK_BUFFER_SIZE_MS = 3000L
    var PLAYBACK_PRELOAD_AHEAD_MS = 10000L
    var ENABLE_PLAYBACK_CACHING = true

    // Debug Settings
    var DEBUG_OVERLAY_ENABLED = false
    var DEBUG_FPS_COUNTER = false
    var DEBUG_MEMORY_LOGGING = false

    // Device Optimization
    var ADAPTIVE_QUALITY_ENABLED = true
    var LOW_END_DEVICE_FPS_LIMIT = 30
    var LOW_END_DEVICE_MEM_LIMIT = 100

    /**
     * Apply device optimizations.
     */
    fun applyDeviceOptimizations(deviceRAM: Long, deviceFPS: Int) {
        when {
            deviceRAM < LOW_END_DEVICE_MEM_LIMIT -> {
                ENABLE_LOW_MEMORY_MODE = true
                MAX_BITMAP_CACHE_SIZE_MB = 25
            }
        }

        when {
            deviceFPS < 60 -> {
                DEFAULT_FRAME_RATE = deviceFPS
            }
        }
    }

    /**
     * Reset to defaults.
     */
    fun reset() {
        DEFAULT_TIMELINE_ZOOM = 1f
        DEFAULT_EXPORT_QUALITY = "1080p"
        ENABLE_LOW_MEMORY_MODE = false
    }
}