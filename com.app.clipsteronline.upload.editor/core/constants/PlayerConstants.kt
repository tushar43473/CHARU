package com.app.clipsteronline.upload.editor.core.constants

/**
 * Constants for media playback operations.
 * Contains speed limits, seek sensitivity, buffering configs, and Media3 defaults.
 */
object PlayerConstants {

    // Playback speed limits
    const val SPEED_MIN = 0.25f
    const val SPEED_MAX = 4.0f
    const val SPEED_DEFAULT = 1.0f
    const val SPEED_STEP = 0.25f

    // Speed presets
    val SPEED_PRESETS = listOf(0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f, 4.0f)

    // Reverse playback
    const val REVERSE_SPEED_MIN = -2.0f
    const val REVERSE_SPEED_MAX = -0.25f
    const val REVERSE_ENABLED = true

    // Frame-by-frame
    const val FRAME_STEP_FORWARD = 1
    const val FRAME_STEP_BACKWARD = -1
    const val FRAME_DURATION_MS = 33 // At 30fps

    // Seek sensitivity
    const val SEEK_INCREMENT_MS = 100L
    const val SEEK_LONG_INCREMENT_MS = 1000L
    const val SEEK_SENSITIVITY = 1.0f
    const val SEEK_SNAP_ENABLED = true

    // Seek modes
    const val SEEK_MODE_ACCURATE = 0
    const val SEEK_MODE_CLOSEST = 1
    const val SEEK_MODE_CLOSEST_SYNC = 2
    const val SEEK_MODE_PREVIOUS_SYNC = 3
    const val SEEK_MODE_NEXT_SYNC = 4
    const val SEEK_MODE_DEFAULT = SEEK_MODE_CLOSEST_SYNC

    // Buffering
    const val BUFFER_MIN_MS = 1000L
    const val BUFFER_MAX_MS = 10000L
    const val BUFFER_DEFAULT_MS = 5000L
    const val BUFFER_FORWARD_MS = 15000L
    const val BUFFER_BACKWARD_MS = 5000L

    // Buffer size
    const val BUFFER_SIZE_MIN = 1024 * 1024 // 1MB
    const val BUFFER_SIZE_DEFAULT = 8 * 1024 * 1024 // 8MB
    const val BUFFER_SIZE_MAX = 32 * 1024 * 1024 // 32MB

    // Progressive loading
    const val PROGRESSIVE_LOAD_ENABLED = true
    const val PROGRESSIVE_LOAD_THRESHOLD = 0.5f

    // Preview scale
    const val PREVIEW_SCALE_1X = 1.0f
    const val PREVIEW_SCALE_2X = 2.0f
    const val PREVIEW_SCALE_4X = 4.0f
    const val PREVIEW_SCALE_DEFAULT = PREVIEW_SCALE_1X

    // Media3 player defaults
    const val PLAYER_VERSION = 1 // ExoPlayer 1.x
    const val PLAYER_MIN_SDK = 21

    // Load control
    const val LOAD_CONTROLLER_MIN_BUFFER_MS = 1500L
    const val LOAD_CONTROLLER_MAX_BUFFER_MS = 15000L
    const val LOAD_CONTROLLER_BUFFER_FOR_PLAYBACK_MS = 2500L
    const val LOAD_CONTROLLER_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS = 5000L

    // Track selection
    const val PREFER_REPRESENTATION_HEIGHT = 720
    const val PREFER_REPRESENTATION_BITRATE = 5_000_000

    // Audio focus
    const val AUDIO_FOCUS_ENABLED = true
    const val AUDIO_FOCUS_LOSSFOCUS = "loss"
    const val AUDIO_FOCUS_TRY_GAIN = "try"
    const val AUDIO_FOCUS_GAIN = "gain"
    const val AUDIO_FOCUS_MODE_DEFAULT = AUDIO_FOCUS_GAIN

    // Audio attributes
    const val AUDIO_USAGE_MEDIA = 1
    const val AUDIO_CONTENT_TYPE_MOVIE = 3
    const val AUDIO_CONTENT_TYPE_MUSIC = 2

    // Video renderer
    const val VIDEO_RENDERER_ENABLED = true
    const val VIDEO_RENDERER_EXTENSION = false

    // Audio renderer
    const val AUDIO_RENDERER_ENABLED = true
    const val AUDIO_RENDERER_EXTENSION = false

    // Metadata renderer
    const val METADATA_RENDERER_ENABLED = true

    // Text renderer
    const val TEXT_RENDERER_ENABLED = true

    // Clock
    const val CLOCK_CHRONOMETER = true

    // Wake lock
    const val WAKE_LOCK_ENABLED = true
    const val WAKE_LOCK_MODE = 0 // Screen on

    // Analytics
    const val ANALYTICS_ENABLED = true

    // Debug
    const val DEBUG_ENABLED = false
    const val DEBUG_SHOW_FPS = false
    const val DEBUG_SHOW_BUFFER = false

    // Repeat modes
    const val REPEAT_MODE_OFF = 0
    const val REPEAT_MODE_ONE = 1
    const val REPEAT_MODE_ALL = 2

    // Shuffle modes
    const val SHUFFLE_MODE_OFF = 0
    const val SHUFFLE_MODE_ON = 1

    // Playback state listeners
    const val PLAYBACK_SETTLING_TIME_MS = 500L

    // Gapless playback
    const val GAPLESS_ENABLED = true

    // Audio ducking
    const val AUDIO_DUCKING_ENABLED = true
    const val AUDIO_DUCKING_MIN_VOLUME = 0.2f

    // Fade on start/stop
    const val FADE_ENABLED = true
    const val FADE_DURATION_MS = 300L

    // Pitch correction
    const val PITCH_CORRECTION_ENABLED = false

    // Skip silence
    const val SKIP_SILENCE_ENABLED = false

    // Encoder
    const val ENCODER_MIME_TYPE = "audio/mp4a-latm"

    // Bandwidth
    const val BANDWIDTH_AUTO = -1
    const val BANDWIDTH_DEFAULT = 10_000_000

    // Network
    const val NETWORK_TIMEOUT_MS = 15000L
    const val NETWORK_RETRY_COUNT = 3
    const val NETWORK_RETRY_DELAY_MS = 1000L

    // Caching
    const val CACHE_ENABLED = true
    const val CACHE_MAX_SIZE = 100 * 1024 * 1024 // 100MB

    // Subtitle
    const val SUBTITLE_ENABLED = true
    const val SUBTITLE_STYLE = "default"

    // Remote playback
    const val REMOTE_ENABLED = false

    //casting
    const val CAST_ENABLED = false
}