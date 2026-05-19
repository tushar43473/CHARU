package com.app.clipsteronline.upload.editor.core.constants

/**
 * Constants for timeline operations and display.
 * Contains dimensions, zoom limits, scroll settings, and snap thresholds.
 */
object TimelineConstants {

    // Timeline dimensions (dp)
    const val TIMELINE_HEIGHT = 120
    const val TIMELINE_HEIGHT_COMPACT = 80
    const val TIMELINE_HEIGHT_EXPANDED = 200
    const val TRACK_MIN_HEIGHT = 48
    const val TRACK_MAX_HEIGHT = 160
    const val TRACK_DEFAULT_HEIGHT = 64

    // Zoom levels
    const val ZOOM_MIN = 0.1f
    const val ZOOM_MAX = 10.0f
    const val ZOOM_DEFAULT = 1.0f
    const val ZOOM_STEP = 0.1f
    const val ZOOM_FIT_TO_SCREEN = -1f

    // Zoom thresholds
    const val ZOOM_THRESHOLD_WAVE = 0.5f
    const val ZOOM_THRESHOLD_SECOND = 1.0f
    const val ZOOM_THRESHOLD_MINUTE = 2.0f

    // Time ruler
    const val RULER_HEIGHT = 24
    const val RULER_TEXT_SIZE = 10 // sp
    const val RULER_TICK_HEIGHT = 8
    const val RULER_MAJOR_TICK_HEIGHT = 12

    // Playhead
    const val PLAYHEAD_WIDTH = 2
    const val PLAYHEAD_HANDLE_SIZE = 12
    const val PLAYHEAD_COLOR = 0xFFFF6B6B.toInt() // Red
    const val PLAYHEAD_DRAG_TOLERANCE = 24 // dp

    // Clip dimensions
    const val CLIP_MIN_WIDTH = 40 // dp
    const val CLIP_CORNER_RADIUS = 4
    const val CLIP_THUMBNAIL_HEIGHT = 48
    const val CLIP_THUMBNAIL_MARGIN = 4
    const val CLIP_BORDER_WIDTH = 1

    // Clip spacing (dp)
    const val CLIP_MARGIN_VERTICAL = 2
    const val CLIP_MARGIN_HORIZONTAL = 4
    const val TRACK_SPACING = 4
    const val CLIP_GAP = 0

    // Snap threshold (ms)
    const val SNAP_THRESHOLD = 100L
    const val SNAP_THRESHOLD_STRICT = 50L
    const val SNAP_THRESHOLD_LOOSE = 200L

    // Scroll behavior
    const val FLING_MIN_VELOCITY = 500f
    const val FLING_MAX_VELOCITY = 8000f
    const val FLING_FRICTION = 0.015f
    const val SCROLL_DECELERATION = 0.998f

    // Scroll speed (pixels per ms)
    const val SCROLL_SPEED_NORMAL = 1.0f
    const val SCROLL_SPEED_FAST = 2.0f
    const val SCROLL_SENSITIVITY = 1.0f
    const val SMOOTH_SCROLL_RATIO = 0.8f

    // Time display
    const val TIME_FORMAT_MS = "00:00:000"
    const val TIME_FORMAT_S = "00:00"
    const val TIME_FORMAT_M = "00:00:00"
    const val TIME_SEPARATOR = ":"

    // Waveform display
    const val WAVEFORM_HEIGHT = 32
    const val WAVEFORM_BAR_WIDTH = 2
    const val WAVEFORM_BAR_GAP = 1
    const val WAVEFORM_COLOR = 0xFF4CAF50.toInt() // Green

    // Thumbnail display
    const val THUMBNAIL_SIZE = 48
    const val THUMBNAIL_SPACING = 2
    const val THUMBNAIL_UPDATE_INTERVAL = 1000L // ms

    // Selection
    const val SELECTION_HANDLE_SIZE = 8
    const val SELECTION_BORDER_WIDTH = 2
    const val SELECTION_COLOR = 0xFF2196F3.toInt() // Blue
    const val SELECTION_FADE_DURATION = 200L

    // Transition handles
    const val TRANSITION_HANDLE_WIDTH = 16
    const val TRANSITION_MIN_DURATION = 100L // ms

    // Audio waveform
    const val AUDIO_WAVEFORM_SAMPLES_PER_PIXEL = 256
    const val AUDIO_WAVEFORM_MIN_AMPLITUDE = 2
    const val AUDIO_WAVEFORM_MAX_AMPLITUDE = 48

    // Scrolling bounds
    const val SCROLL_PADDING_START = 0
    const val SCROLL_PADDING_END = 1000 // dp
    const val MIN_SCROLL = 0f
    const val MAX_SCROLL = Float.MAX_VALUE

    // Animation
    const val SCROLL_ANIMATION_DURATION = 300L
    const val ZOOM_ANIMATION_DURATION = 250L
    const val SNAP_ANIMATION_DURATION = 150L

    // Lazy loading
    const val LAZY_LOAD_THRESHOLD = 200 // dp
    const val LAZY_UNLOAD_THRESHOLD = 400 // dp

    // Frame markers
    const val MARKER_SIZE = 6
    const val MARKER_COLOR = 0xFFFFEB3B.toInt() // Yellow

    // In/Out points
    const val IN_OUT_MARKER_WIDTH = 3
    const val IN_OUT_MARKER_COLOR = 0xFFFF9800.toInt() // Orange
}