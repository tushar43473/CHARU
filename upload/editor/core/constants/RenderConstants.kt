package upload.editor.core.constants

object RenderConstants {
    const val EGL_CONTEXT_CLIENT_VERSION = 3
    const val GL_SWAP_INTERVAL = 1

    const val RENDER_FPS_PREVIEW = 30
    const val RENDER_FPS_HIGH = 60
    const val RENDER_FPS_EXPORT_MAX = 120

    const val DEFAULT_TEXTURE_SIZE = 2048
    const val MAX_TEXTURE_SIZE_SAFE = 4096
    const val MAX_TEXTURE_SIZE_FLAGSHIP = 8192

    const val FRAME_QUEUE_CAPACITY = 3
    const val RENDER_THREAD_PRIORITY = -4
    const val RENDER_THREAD_KEEP_ALIVE_SEC = 10L

    const val ENABLE_HARDWARE_ACCELERATION = true
    const val ENABLE_SURFACE_PERSISTENCE = true
    const val ENABLE_COLOR_DITHER = false

    const val SHADER_WARMUP_COUNT = 3
    const val SHADER_COMPILE_TIMEOUT_MS = 1200L
    const val SHADER_CACHE_LIMIT = 128
}
