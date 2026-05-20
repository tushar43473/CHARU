package com.app.clipsteronline.upload.editor.core.constants

object PlayerConstants {
    const val MIN_PLAYBACK_SPEED = 0.25f
    const val DEFAULT_PLAYBACK_SPEED = 1.0f
    const val MAX_PLAYBACK_SPEED = 4.0f

    const val BUFFER_MIN_MS = 15_000
    const val BUFFER_MAX_MS = 50_000
    const val BUFFER_PLAYBACK_MS = 2_500
    const val BUFFER_REBUFFER_MS = 5_000

    const val PREVIEW_FPS = 30
    const val FRAME_SYNC_INTERVAL_MS = 16L

    const val PREVIEW_CACHE_FRAMES = 24
    const val SEEK_STEP_MS = 1000L
}
