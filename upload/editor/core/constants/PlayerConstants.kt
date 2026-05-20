package upload.editor.core.constants

object PlayerConstants {
    const val PLAYBACK_SPEED_MIN = 0.25f
    const val PLAYBACK_SPEED_DEFAULT = 1.0f
    const val PLAYBACK_SPEED_MAX = 4.0f

    const val SEEK_STEP_MS = 1000L
    const val SEEK_FINE_STEP_MS = 100L
    const val SEEK_SCRUB_SENSITIVITY = 1.0f
    const val SEEK_FINE_SCRUB_SENSITIVITY = 0.35f

    const val BUFFER_MIN_MS = 15_000
    const val BUFFER_MAX_MS = 50_000
    const val BUFFER_PLAYBACK_MS = 2_500
    const val BUFFER_REBUFFER_MS = 5_000

    const val PREVIEW_SCALE_MIN = 0.5f
    const val PREVIEW_SCALE_DEFAULT = 1.0f
    const val PREVIEW_SCALE_MAX = 3.0f
    const val PREVIEW_DOUBLE_TAP_SCALE = 2.0f

    const val MEDIA3_SEEK_BACK_MS = 5_000L
    const val MEDIA3_SEEK_FORWARD_MS = 10_000L
    const val MEDIA3_LIVE_TARGET_OFFSET_MS = 500L
    const val MEDIA3_RELEASE_TIMEOUT_MS = 2_000L
}
