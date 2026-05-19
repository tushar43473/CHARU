package com.app.clipsteronline.upload.editor.project

import android.content.Context
import android.graphics.BitmapFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileNotFoundException

/**
 * Project loader.
 * Load projects, restore state, validation.
 */
class ProjectLoader(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) {
    private var minimumVersion = 1

    /**
     * Load project.
     */
    suspend fun loadProject(projectDir: File): LoadResult = withContext(Dispatchers.IO) {
        try {
            val projectFile = File(projectDir, "project.json")
            if (!projectFile.exists()) {
                return@withContext LoadResult.Error("Project file not found")
            }

            val json = JSONObject(projectFile.readText())

            // Validate version
            val version = json.optInt("version", 1)
            if (version < minimumVersion) {
                return@withContext LoadResult.Error("Unsupported project version")
            }

            // Parse project
            val project = parseProject(json, projectDir)

            // Load thumbnail
            val thumbnail = loadThumbnail(projectDir)

            LoadResult.Success(project.copy(thumbnail = thumbnail))
        } catch (e: FileNotFoundException) {
            LoadResult.Error("Project not found")
        } catch (e: Exception) {
            LoadResult.Error(e.message ?: "Failed to load project")
        }
    }

    /**
     * Load only metadata.
     */
    suspend fun loadMetadata(projectDir: File): ProjectMetadata? = withContext(Dispatchers.IO) {
        try {
            val metaFile = File(projectDir, "metadata.json")
            if (metaFile.exists()) {
                val json = JSONObject(metaFile.readText())
                ProjectMetadata(
                    savedAt = json.optLong("savedAt"),
                    durationMs = json.optLong("duration"),
                    clipCount = json.optInt("clipCount")
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Validate project.
     */
    suspend fun validateProject(projectDir: File): ValidationResult = withContext(Dispatchers.IO) {
        val issues = mutableListOf<String>()

        // Check project file exists
        val projectFile = File(projectDir, "project.json")
        if (!projectFile.exists()) {
            issues.add("Missing project.json")
            return@withContext ValidationResult.Invalid(issues)
        }

        // Check can parse
        try {
            val json = JSONObject(projectFile.readText())
            
            // Validate required fields
            if (!json.has("version")) issues.add("Missing version")
            if (!json.has("name")) issues.add("Missing project name")
            if (!json.has("timeline")) issues.add("Missing timeline")
            
            // Validate clips exist and URIs are accessible
            val timeline = json.optJSONObject("timeline")
            if (timeline != null) {
                val clips = timeline.optJSONArray("clips")
                if (clips != null) {
                    for (i in 0 until clips.length()) {
                        val clip = clips.getJSONObject(i)
                        if (!clip.has("uri")) {
                            issues.add("Clip ${i + 1}: Missing URI")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            issues.add("Corrupted project file: ${e.message}")
        }

        if (issues.isEmpty()) ValidationResult.Valid else ValidationResult.Invalid(issues)
    }

    /**
     * Quick load (minimal data).
     */
    suspend fun quickLoad(projectDir: File): QuickProjectData? = withContext(Dispatchers.IO) {
        try {
            val projectFile = File(projectDir, "project.json")
            if (!projectFile.exists()) return@withContext null

            val json = JSONObject(projectFile.readText())

            QuickProjectData(
                name = json.optString("name", "Untitled"),
                version = json.optInt("version", 1),
                duration = json.optJSONObject("timeline")?.optLong("duration") ?: 0L,
                clipCount = json.optJSONObject("timeline")?.optJSONArray("clips")?.length() ?: 0
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Load thumbnail.
     */
    private fun loadThumbnail(dir: File): android.graphics.Bitmap? {
        return try {
            val thumbFile = File(dir, "thumbnail.jpg")
            if (thumbFile.exists()) {
                BitmapFactory.decodeFile(thumbFile.absolutePath)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Parse project JSON.
     */
    private fun parseProject(json: JSONObject, dir: File): ProjectData {
        return ProjectData(
            version = json.optInt("version", 1),
            name = json.optString("name", "Untitled"),
            createdAt = json.optLong("createdAt"),
            modifiedAt = json.optLong("modifiedAt"),
            resolution = json.optString("resolution", "1920x1080"),
            frameRate = json.optInt("frameRate", 30),
            timeline = parseTimeline(json.optJSONObject("timeline")),
            clips = parseClips(json.optJSONObject("timeline")?.optJSONArray("clips")),
            effects = parseEffects(json.optJSONArray("effects")),
            texts = parseTexts(json.optJSONArray("texts")),
            stickers = parseStickers(json.optJSONArray("stickers")),
            audioTracks = parseAudioTracks(json.optJSONArray("audioTracks")),
            exportSettings = parseExportSettings(json.optJSONObject("export"))
        )
    }

    private fun parseTimeline(json: JSONObject?): TimelineData = TimelineData(
        duration = json?.optLong("duration") ?: 0L
    )

    private fun parseClips(arr: org.json.JSONArray?): List<ClipData> {
        if (arr == null) return emptyList()
        val clips = mutableListOf<ClipData>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            clips.add(ClipData(
                id = obj.getString("id"),
                uri = android.net.Uri.parse(obj.getString("uri")),
                trackId = obj.optString("trackId", ""),
                startMs = obj.optLong("startMs"),
                endMs = obj.optLong("endMs"),
                trimStartMs = obj.optLong("trimStartMs"),
                trimEndMs = obj.optLong("trimEndMs")
            ))
        }
        return clips
    }

    private fun parseEffects(arr: org.json.JSONArray?): List<EffectData> = emptyList()
    private fun parseTexts(arr: org.json.JSONArray?): List<TextData> = emptyList()
    private fun parseStickers(arr: org.json.JSONArray?): List<StickerData> = emptyList()
    private fun parseAudioTracks(arr: org.json.JSONArray?): List<AudioTrackData> = emptyList()

    private fun parseExportSettings(json: JSONObject?): ExportSettingsData = ExportSettingsData(
        quality = json?.optString("quality") ?: "1080p",
        format = json?.optString("format") ?: "mp4"
    )
}

/**
 * Load result.
 */
sealed class LoadResult {
    data class Success(val project: ProjectData) : LoadResult()
    data class Error(val message: String) : LoadResult()
}

/**
 * Validation result.
 */
sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val issues: List<String>) : ValidationResult()
}

/**
 * Quick project data.
 */
data class QuickProjectData(
    val name: String,
    val version: Int,
    val duration: Long,
    val clipCount: Int
)