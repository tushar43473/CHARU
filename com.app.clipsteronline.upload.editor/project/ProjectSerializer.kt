package com.app.clipsteronline.upload.editor.project

import android.net.Uri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.json.JSONArray
import org.json.JSONObject
import java.nio.charset.StandardCharsets

/**
 * Project serializer.
 * JSON serialization/deserialization.
 */
class ProjectSerializer(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) {
    private var currentVersion = 1

    /**
     * Serialize project to JSON string.
     */
    fun serialize(project: ProjectData): String {
        val json = toJson(project)
        return json.toString(2)
    }

    /**
     * Deserialize from JSON string.
     */
    fun deserialize(jsonString: String): ProjectData {
        val json = JSONObject(jsonString)
        return fromJson(json)
    }

    /**
     * Serialize to compact JSON.
     */
    fun serializeCompact(project: ProjectData): ByteArray {
        val json = toJson(project)
        return json.toString(0).toByteArray(StandardCharsets.UTF_8)
    }

    /**
     * Deserialize from compact JSON.
     */
    fun deserializeCompact(bytes: ByteArray): ProjectData {
        val json = JSONObject(String(bytes, StandardCharsets.UTF_8))
        return fromJson(json)
    }

    /**
     * Convert ProjectData to JSONObject.
     */
    fun toJson(project: ProjectData): JSONObject {
        return JSONObject().apply {
            put("version", project.version)
            put("name", project.name)
            put("createdAt", project.createdAt)
            put("modifiedAt", project.modifiedAt)
            put("resolution", project.resolution)
            put("frameRate", project.frameRate)

            // Timeline
            put("timeline", JSONObject().apply {
                put("duration", project.timeline.duration)
            })

            // Clips
            put("clips", JSONArray().apply {
                project.clips.forEach { clip ->
                    put(JSONObject().apply {
                        put("id", clip.id)
                        put("uri", clip.uri.toString())
                        put("trackId", clip.trackId)
                        put("startMs", clip.startMs)
                        put("endMs", clip.endMs)
                        if (clip.trimStartMs > 0) put("trimStartMs", clip.trimStartMs)
                        if (clip.trimEndMs > 0) put("trimEndMs", clip.trimEndMs)
                    })
                }
            })

            // Texts
            put("texts", JSONArray().apply {
                project.texts.forEach { text ->
                    put(JSONObject().apply {
                        put("id", text.id)
                        put("text", text.text)
                        put("style", text.style)
                        put("startMs", text.startMs)
                        put("endMs", text.endMs)
                    })
                }
            })

            // Effects
            put("effects", JSONArray().apply {
                project.effects.forEach { effect ->
                    put(JSONObject().apply {
                        put("id", effect.id)
                        put("type", effect.type)
                        put("startMs", effect.startMs)
                        put("endMs", effect.endMs)
                        put("intensity", effect.intensity)
                    })
                }
            })

            // Stickers
            put("stickers", JSONArray().apply {
                project.stickers.forEach { sticker ->
                    put(JSONObject().apply {
                        put("id", sticker.id)
                        put("uri", sticker.uri.toString())
                        put("startMs", sticker.startMs)
                        put("endMs", sticker.endMs)
                    })
                }
            })

            // Audio tracks
            put("audioTracks", JSONArray().apply {
                project.audioTracks.forEach { track ->
                    put(JSONObject().apply {
                        put("id", track.id)
                        put("uri", track.uri.toString())
                        put("volume", track.volume)
                    })
                }
            })

            // Export settings
            put("export", JSONObject().apply {
                put("quality", project.exportSettings.quality)
                put("format", project.exportSettings.format)
            })
        }
    }

    /**
     * Convert JSONObject to ProjectData.
     */
    fun fromJson(json: JSONObject): ProjectData {
        return ProjectData(
            version = json.optInt("version", 1),
            name = json.optString("name", "Untitled"),
            createdAt = json.optLong("createdAt"),
            modifiedAt = json.optLong("modifiedAt"),
            resolution = json.optString("resolution", "1920x1080"),
            frameRate = json.optInt("frameRate", 30),
            timeline = parseTimeline(json.optJSONObject("timeline")),
            clips = parseClips(json.optJSONArray("clips")),
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

    private fun parseClips(json: JSONArray?): List<ClipData> {
        if (json == null) return emptyList()
        return (0 until json.length()).map { i ->
            val obj = json.getJSONObject(i)
            ClipData(
                id = obj.getString("id"),
                uri = Uri.parse(obj.getString("uri")),
                trackId = obj.optString("trackId", ""),
                startMs = obj.optLong("startMs"),
                endMs = obj.optLong("endMs"),
                trimStartMs = obj.optLong("trimStartMs", 0),
                trimEndMs = obj.optLong("trimEndMs", 0)
            )
        }
    }

    private fun parseTexts(json: JSONArray?): List<TextData> {
        if (json == null) return emptyList()
        return (0 until json.length()).map { i ->
            val obj = json.getJSONObject(i)
            TextData(
                id = obj.getString("id"),
                text = obj.getString("text"),
                style = obj.optString("style", "default"),
                startMs = obj.optLong("startMs"),
                endMs = obj.optLong("endMs")
            )
        }
    }

    private fun parseEffects(json: JSONArray?): List<EffectData> {
        if (json == null) return emptyList()
        return (0 until json.length()).map { i ->
            val obj = json.getJSONObject(i)
            EffectData(
                id = obj.getString("id"),
                type = obj.getString("type"),
                startMs = obj.optLong("startMs"),
                endMs = obj.optLong("endMs"),
                intensity = obj.optDouble("intensity", 1.0).toFloat()
            )
        }
    }

    private fun parseStickers(json: JSONArray?): List<StickerData> {
        if (json == null) return emptyList()
        return (0 until json.length()).map { i ->
            val obj = json.getJSONObject(i)
            StickerData(
                id = obj.getString("id"),
                uri = Uri.parse(obj.getString("uri")),
                startMs = obj.optLong("startMs"),
                endMs = obj.optLong("endMs")
            )
        }
    }

    private fun parseAudioTracks(json: JSONArray?): List<AudioTrackData> {
        if (json == null) return emptyList()
        return (0 until json.length()).map { i ->
            val obj = json.getJSONObject(i)
            AudioTrackData(
                id = obj.getString("id"),
                uri = Uri.parse(obj.getString("uri")),
                volume = obj.optDouble("volume", 1.0).toFloat()
            )
        }
    }

    private fun parseExportSettings(json: JSONObject?): ExportSettingsData = ExportSettingsData(
        quality = json?.optString("quality") ?: "1080p",
        format = json?.optString("format") ?: "mp4"
    )
}

/**
 * Migration support.
 */
class ProjectMigrator {
    /**
     * Migrate from older version.
     */
    fun migrate(json: JSONObject, fromVersion: Int, toVersion: Int): JSONObject {
        var current = json
        var version = fromVersion

        while (version < toVersion) {
            current = when (version) {
                1 -> migrateV1ToV2(current)
                else -> current
            }
            version++
        }

        return current
    }

    private fun migrateV1ToV2(json: JSONObject): JSONObject {
        // Example: Add new fields introduced in v2
        return json.put("frameRate", json.optInt("frameRate", 30))
    }
}