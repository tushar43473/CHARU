package com.app.clipsteronline.upload.editor.core.model

import android.net.Uri

/**
 * Represents a complete video editor project.
 * Contains all timeline data, settings, and metadata.
 */
data class Project(
    val id: String,
    val name: String,
    val timeline: Timeline,
    val settings: ProjectSettings,
    val metadata: ProjectMetadata,
    val thumbnailUri: Uri? = null,
    val isDraft: Boolean = true,
    val version: Int = CURRENT_VERSION
) {
    companion object {
        const val CURRENT_VERSION = 1
        const val MIN_COMPATIBLE_VERSION = 1
    }
}

/**
 * Project timeline containing all tracks and clips.
 */
data class Timeline(
    val tracks: List<TimelineTrack> = emptyList(),
    val durationMs: Long = 0L,
    val frameRate: Int = 30,
    val timeSignature: Int = 30
) {
    fun getTrackById(trackId: String): TimelineTrack? {
        return tracks.find { it.id == trackId }
    }

    fun getTrackByIndex(index: Int): TimelineTrack? {
        return tracks.getOrNull(index)
    }
}

/**
 * Project-specific settings and preferences.
 */
data class ProjectSettings(
    val aspectRatio: AspectRatio = AspectRatio.RATIO_16_9,
    val resolution: Resolution = Resolution.FHD_1080P,
    val frameRate: Int = 30,
    val bitRate: Int = 50_000_000,
    val colorSpace: ColorSpace = ColorSpace.BT709,
    val enableHDR: Boolean = false,
    val enableStabilization: Boolean = false,
    val enableAutoSave: Boolean = true,
    val autoSaveIntervalMs: Long = 30_000L
)

/**
 * Aspect ratio presets for video export.
 */
enum class AspectRatio(
    val displayName: String,
    val widthRatio: Float,
    val heightRatio: Float
) {
    RATIO_9_16("9:16", 9f / 16f),
    RATIO_16_9("16:9", 16f / 9f),
    RATIO_1_1("1:1", 1f),
    RATIO_4_5("4:5", 4f / 5f),
    RATIO_21_9("21:9", 21f / 9f);

    fun toFloat(): Float = widthRatio / heightRatio
}

/**
 * Resolution presets for video export.
 */
enum class Resolution(
    val displayName: String,
    val width: Int,
    val height: Int
) {
    HD_720P("720p", 1280, 720),
    FHD_1080P("1080p", 1920, 1080),
    QHD_1440P("1440p", 2560, 1440),
    UHD_4K("4K", 3840, 2160),
    UHD_8K("8K", 7680, 4320);

    fun toSize(): Pair<Int, Int> = width to height
}

/**
 * Color space for video rendering.
 */
enum class ColorSpace(val displayName: String, val primaries: String) {
    BT709("Rec. 709", "BT709"),
    BT601("Rec. 601", "BT601"),
    DISPLAY_P3("Display P3", "DISPLAY_P3"),
    BT2020("Rec. 2020", "BT2020"),
    SRGB("sRGB", "SRGB")
}

/**
 * Project metadata for storage and display.
 */
data class ProjectMetadata(
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val createdBy: String = "",
    val description: String = "",
    val tags: List<String> = emptyList(),
    val category: String = "",
    val thumbnailTimeMs: Long = 0L
) {
    fun withUpdate(): ProjectMetadata {
        return copy(updatedAt = System.currentTimeMillis())
    }
}