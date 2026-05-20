package upload.editor.core.constants

object TimelineConstants {
    const val TRACK_HEIGHT_DP = 64
    const val AUDIO_TRACK_HEIGHT_DP = 56
    const val RULER_HEIGHT_DP = 28
    const val HEADER_WIDTH_DP = 56

    const val PLAYHEAD_WIDTH_DP = 2
    const val PLAYHEAD_TOUCH_WIDTH_DP = 24
    const val CLIP_CORNER_RADIUS_DP = 6
    const val CLIP_SPACING_DP = 2
    const val TRACK_VERTICAL_SPACING_DP = 6

    const val MIN_ZOOM_SCALE = 0.25f
    const val DEFAULT_ZOOM_SCALE = 1.0f
    const val MAX_ZOOM_SCALE = 12.0f
    const val ZOOM_STEP = 0.05f

    const val SNAP_THRESHOLD_MS = 80L
    const val SNAP_TO_FRAME_THRESHOLD_MS = 20L
    const val AUTO_SCROLL_EDGE_ZONE_DP = 32

    const val FLING_MIN_VELOCITY_PX_PER_SEC = 1200
    const val FLING_MAX_VELOCITY_PX_PER_SEC = 12000
    const val BASE_SCROLL_SPEED_PX_PER_SEC = 2400f
    const val FINE_SCROLL_MULTIPLIER = 0.5f
    const val FAST_SCROLL_MULTIPLIER = 1.8f
}
