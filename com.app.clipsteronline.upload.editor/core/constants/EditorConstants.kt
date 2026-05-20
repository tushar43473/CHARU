package com.app.clipsteronline.upload.editor.core.constants

object EditorConstants {
    const val PROJECT_VERSION = 1
    const val MAX_PROJECT_DURATION_MS = 3_600_000L

    const val DEFAULT_FPS = 30
    const val HIGH_FPS = 60
    const val MAX_FPS = 120

    const val MAX_VIDEO_TRACKS = 8
    const val MAX_AUDIO_TRACKS = 8
    const val MAX_OVERLAY_TRACKS = 6

    const val DEFAULT_UNDO_STACK_SIZE = 200
    const val AUTO_SAVE_INTERVAL_MS = 10_000L
    const val SESSION_RECOVERY_TTL_MS = 86_400_000L

    const val DEBUG_TAG = "ClipsterEditor"
}
