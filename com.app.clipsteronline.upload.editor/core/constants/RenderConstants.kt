package com.app.clipsteronline.upload.editor.core.constants

/**
 * Constants for rendering operations.
 * Contains OpenGL settings, texture configs, thread values, and hardware acceleration.
 */
object RenderConstants {

    // Render FPS
    const val RENDER_FPS = 60
    const val RENDER_FPS_PREVIEW = 30
    const val RENDER_FPS_EXPORT = 60
    const val RENDER_FPS_MIN = 24
    const val RENDER_FPS_MAX = 120

    // Frame times (ms)
    const val FRAME_TIME_MS = 1000L / RENDER_FPS
    const val FRAME_TIME_PREVIEW_MS = 1000L / RENDER_FPS_PREVIEW

    // Texture sizes
    const val TEXTURE_SIZE_MIN = 64
    const val TEXTURE_SIZE_MAX = 4096
    const val TEXTURE_SIZE_DEFAULT = 2048
    const val TEXTURE_SIZE_HD = 1920
    const val TEXTURE_SIZE_FHD = 1920
    const val TEXTURE_SIZE_4K = 3840

    // Texture formats
    const val TEXTURE_FORMAT_RGBA = 0x1908 // GL_RGBA
    const val TEXTURE_FORMAT_RGB = 0x1907 // GL_RGB
    const val TEXTURE_FORMAT_LUMINANCE = 0x1909 // GL_LUMINANCE

    // Texture filters
    const val TEXTURE_FILTER_LINEAR = 0x2601 // GL_LINEAR
    const val TEXTURE_FILTER_NEAREST = 0x2600 // GL_NEAREST
    const val TEXTURE_FILTER_MIPMAP = 0x2703 // GL_LINEAR_MIPMAP_LINEAR

    // OpenGL versions
    const val GL_VERSION_MIN = 2
    const val GL_VERSION_MAX = 3
    const val GL_VERSION_DEFAULT = 3

    // Shader configs
    const val SHADER_VERTEX_PRECISION = "highp"
    const val SHADER_FRAGMENT_PRECISION = "highp"
    const val SHADER_MAX_TEXTURES = 8
    const val SHADER_MAX_VERTICES = 65536
    const val SHADER_MAX_UNIFORMS = 256

    // Render thread
    const val RENDER_THREAD_PRIORITY = Thread.NORM_PRIORITY
    const val RENDER_THREAD_PRIORITY_HIGH = Thread.MAX_PRIORITY
    const val RENDER_THREAD_NAME = "RenderThread"

    // Buffer sizes
    const val FRAME_BUFFER_SIZE = 3
    const val RENDER_BUFFER_SIZE = 2
    const val TEXTURE_BUFFER_POOL_SIZE = 16

    // Decoder configs
    const val DECODER_THREAD_COUNT = 4
    const val DECODER_THREAD_PRIORITY = Thread.NORM_PRIORITY
    const val DECODER_INPUT_BUFFER_SIZE = 8192
    const val DECODER_OUTPUT_BUFFER_SIZE = 2097152 // 2MB

    // Encoder configs
    const val ENCODER_THREAD_COUNT = 4
    const val ENCODER_THREAD_PRIORITY = Thread.NORM_PRIORITY
    const val ENCODER_FRAME_POOL_SIZE = 8

    // Memory limits
    const val MAX_TEXTURE_CACHE_SIZE = 256 * 1024 * 1024 // 256MB
    const val MAX_FRAME_CACHE_SIZE = 128 * 1024 * 1024 // 128MB
    const val MAX_RENDER_BUFFER_SIZE = 64 * 1024 * 1024 // 64MB
    const val MEMORY_WARNING_THRESHOLD = 512 * 1024 * 1024 // 512MB

    // Video memory
    const val VIDEO_MEMORY_BUDGET = 512 * 1024 * 1024L // 512MB
    const val VIDEO_MEMORY_CHUNK_SIZE = 16 * 1024 * 1024L // 16MB

    // Hardware acceleration
    const val HW_ACCELERATION_ENABLED = true
    const val HW_DECODING_ENABLED = true
    const val HW_ENCODING_ENABLED = true
    const val HW_RESOLUTION_MAX = 3840

    // Video processing
    const val VIDEO_PROCESSING_ENABLED = true
    const val VIDEO_SCALING_ENABLED = true
    const val VIDEO_ROTATION_ENABLED = true
    const val VIDEO_CROP_ENABLED = true

    // Color space
    const val COLOR_SPACE_BT709 = "BT709"
    const val COLOR_SPACE_BT601 = "BT601"
    const val COLOR_SPACE_DISPLAY_P3 = "DISPLAY_P3"
    const val COLOR_SPACE_SRGB = "SRGB"
    const val COLOR_SPACE_DEFAULT = COLOR_SPACE_BT709

    // HDR configurations
    const val HDR_FORMAT_HDR10 = "HDR10"
    const val HDR_FORMAT_HLG = "HLG"
    const val HDR_FORMAT_DOLBY_VISION = "DOLBY_VISION"
    const val HDR_FORMAT_NONE = "NONE"

    // EOTF (Electro-Optical Transfer Function)
    const val EOTF_SDR = 0 // Traditional gamma
    const val EOTF_PQ = 1 // Perceptual quantizer
    const val EOTF_HLG = 2 // Hybrid log-gamma

    // Tone mapping
    const val TONE_MAP_ENABLED = true
    const val TONE_MAP_MODE = 0 // Auto
    const val TONE_MAP_TARGET_NIT = 1000

    // Color management
    const val COLOR_MANAGEMENT_ENABLED = true
    const val COLOR_CONVERSION_ENABLED = true

    // Output range
    const val OUTPUT_RANGE_FULL = 0..255
    const val OUTPUT_RANGE_LIMITED = 16..235

    // Bit depth
    const val BIT_DEPTH_8 = 8
    const val BIT_DEPTH_10 = 10
    const val BIT_DEPTH_12 = 12
    const val BIT_DEPTH_DEFAULT = BIT_DEPTH_10

    // YUV planes
    const val YUV_PLANE_COUNT = 3
    const val YUV_STRIDE_ALIGNMENT = 16

    // Scaling quality
    const val SCALE_QUALITY_HIGH = 2 // Lanczos
    const val SCALE_QUALITY_MEDIUM = 1 // Bilinear
    const val SCALE_QUALITY_LOW = 0 // Nearest neighbor

    // Anti-aliasing
    const val ANTI_ALIASING_ENABLED = true
    const val ANTI_ALIASING_SAMPLES = 4

    // V-sync
    const val VSYNC_ENABLED = true
    const val VSYNC_MODE = 1 // Adaptive

    // Blend modes
    const val BLEND_MODE_NORMAL = 0
    const val BLEND_MODE_MULTIPLY = 1
    const val BLEND_MODE_SCREEN = 2
    const val BLEND_MODE_OVERLAY = 3

    // Opacity
    const val OPACITY_MIN = 0f
    const val OPACITY_MAX = 1f
    const val OPACITY_DEFAULT = 1f

    // Performance
    const val PERFORMANCE_WARNING_FPS = 25
    const val PERFORMANCE_TARGET_FPS = 60
    const val PERFORMANCE_MIN_FPS = 24

    // Render quality presets
    const val QUALITY_LOW = "low"
    const val QUALITY_MEDIUM = "medium"
    const val QUALITY_HIGH = "high"
    const val QUALITY_ULTRA = "ultra"
    const val QUALITY_DEFAULT = QUALITY_HIGH
}