package upload.editor.core.constants

object EditorConstants {
    const val PROJECT_NAME_MAX_LENGTH = 64
    const val MAX_TIMELINE_DURATION_MS = 60L * 60L * 1000L

    const val MAX_VIDEO_TRACKS = 8
    const val MAX_AUDIO_TRACKS = 8
    const val MAX_OVERLAY_TRACKS = 6
    const val MAX_TEXT_TRACKS = 6

    const val DEFAULT_FRAME_RATE = 30
    const val HIGH_FRAME_RATE = 60
    const val MAX_SUPPORTED_FRAME_RATE = 120

    const val ASPECT_RATIO_9_16 = "9:16"
    const val ASPECT_RATIO_1_1 = "1:1"
    const val ASPECT_RATIO_16_9 = "16:9"
    const val ASPECT_RATIO_4_5 = "4:5"

    const val MODE_STANDARD = "standard"
    const val MODE_PRO = "pro"
    const val MODE_AUTO_BEAT = "auto_beat"
    const val MODE_TEMPLATE = "template"

    const val GESTURE_SENSITIVITY_PRECISE = 0.65f
    const val GESTURE_SENSITIVITY_BALANCED = 1.0f
    const val GESTURE_SENSITIVITY_FAST = 1.35f

    const val ANIM_DURATION_MICRO_MS = 90L
    const val ANIM_DURATION_SHORT_MS = 140L
    const val ANIM_DURATION_MEDIUM_MS = 240L
    const val ANIM_DURATION_LONG_MS = 360L
    const val ANIM_DURATION_XLONG_MS = 480L
}
