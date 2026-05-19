package com.app.clipsteronline.upload.editor.core.constants

/**
 * Core constants for the video editor.
 * Contains default values, limits, and configuration values.
 */
object EditorConstants {

    // Default values
    const val DEFAULT_FRAME_RATE = 30
    const val DEFAULT_BIT_RATE = 50_000_000 // 50 Mbps
    const val DEFAULT_PROJECT_NAME = "Untitled Project"
    const val DEFAULT_DURATION_MS = 0L
    const val DEFAULT_TIMELINE_HEIGHT_DP = 120

    // Limits
    const val MAX_TRACKS = 10
    const val MAX_AUDIO_TRACKS = 3
    const val MAX_VIDEO_TRACKS = 8
    const val MAX_CLIPS_PER_TRACK = 100
    const val MAX_CLIP_DURATION_MS = 3600000L // 1 hour
    const val MIN_CLIP_DURATION_MS = 100L // 100ms
    const val MAX_PROJECT_DURATION_MS = 3600000L // 1 hour
    const val MAX_EFFECTS_PER_CLIP = 10
    const val MAX_TEXT_OVERLAYS = 20

    // Aspect ratios
    const val RATIO_9_16 = 9f / 16f // Portrait
    const val RATIO_16_9 = 16f / 9f // Landscape
    const val RATIO_1_1 = 1f // Square
    const val RATIO_4_5 = 4f / 5f // Portrait 4:5
    const val RATIO_9_16_DISPLAY = 0.5625f
    const val RATIO_16_9_DISPLAY = 1.7778f
    const val RATIO_1_1_DISPLAY = 1.0f
    const val RATIO_4_5_DISPLAY = 0.8f

    // Supported aspect ratios
    val SUPPORTED_RATIOS = listOf(RATIO_9_16, RATIO_16_9, RATIO_1_1, RATIO_4_5)

    // Editor modes
    const val MODE_SIMPLE = "simple"
    const val MODE_ADVANCED = "advanced"
    const val MODE_PRO = "pro"

    // Gesture sensitivity
    const val GESTURE_SWIPE_THRESHOLD = 50 // dp
    const val GESTURE_LONG_PRESS_TIMEOUT = 500L // ms
    const val GESTURE_DOUBLE_TAP_TIMEOUT = 300L // ms
    const val GESTURE_PINCH_MIN_SCALE = 0.5f
    const val GESTURE_PINCH_MAX_SCALE = 5.0f
    const val GESTURE_DRAG_SENSITIVITY = 1.0f

    // Animation durations (ms)
    const val ANIM_DURATION_FAST = 150L
    const val ANIM_DURATION_NORMAL = 300L
    const val ANIM_DURATION_SLOW = 500L
    const val ANIM_DURATION_TOOLBAR = 200L
    const val ANIM_DURATION_TRANSITION = 350L

    // UI dimensions (dp)
    const val TOOLBAR_HEIGHT = 56
    const val TOOLBAR_HEIGHT_COMPACT = 48
    const val BOTTOM_BAR_HEIGHT = 64
    const val PLAYER_CONTROL_HEIGHT = 48
    const val TIMELINE_RULER_HEIGHT = 24

    // Auto-save
    const val AUTO_SAVE_INTERVAL_MS = 30000L // 30 seconds
    const val AUTO_SAVE_DEBOUNCE_MS = 5000L // 5 seconds

    // Undo/Redo
    const val MAX_UNDO_STACK_SIZE = 50
    const val MAX_REDO_STACK_SIZE = 50

    // Preview
    const val PREVIEW_STEP_FRAMES = 1
    const val PREVIEW_REWIND_FRAMES = 10
    const val PREVIEW_FORWARD_FRAMES = 10

    // Recent projects
    const val MAX_RECENT_PROJECTS = 20

    // Storage
    const val PROJECT_EXTENSION = ".veditor"
    const val EXPORT_EXTENSION = ".mp4"
    const val EXPORT_EXTENSION_MKV = ".mkv"
    const val EXPORT_EXTENSION_WEBM = ".webm"
}