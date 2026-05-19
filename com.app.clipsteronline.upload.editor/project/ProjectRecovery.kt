package com.app.clipsteronline.upload.editor.project

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Project recovery.
 * Crash recovery, autosave restoration.
 */
class ProjectRecovery(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) {
    private val autosaveDir: File get() = File(context.filesDir, "autosave")
    private val backupDir: File get() = File(context.filesDir, "backups")

    /**
     * Initialize recovery folders.
     */
    fun initialize() {
        autosaveDir.mkdirs()
        backupDir.mkdirs()
    }

    /**
     * Find latest autosave.
     */
    suspend fun findLatestAutosave(projectName: String): File? = withContext(Dispatchers.IO) {
        val pattern = "autosave_${projectName}_"

        autosaveDir.listFiles()
            ?.filter { it.name.startsWith(pattern) && it.extension == "json" }
            ?.maxByOrNull { it.lastModified() }
    }

    /**
     * Find all autosaves.
     */
    suspend fun findAllAutosaves(projectName: String): List<AutosaveFile> = withContext(Dispatchers.IO) {
        val pattern = "autosave_${projectName}_"

        autosaveDir.listFiles()
            ?.filter { it.name.startsWith(pattern) && it.extension == "json" }
            ?.map { file ->
                AutosaveFile(
                    file = file,
                    timestamp = file.lastModified(),
                    projectName = projectName
                )
            }
            ?.sortedByDescending { it.timestamp }
            ?: emptyList()
    }

    /**
     * Recover from autosave.
     */
    suspend fun recoverFromAutosave(autosaveFile: File): RecoveryResult = withContext(Dispatchers.IO) {
        try {
            if (!autosaveFile.exists()) {
                return@withContext RecoveryResult.Error("Autosave file not found")
            }

            val content = autosaveFile.readText()
            val json = org.json.JSONObject(content)

            // Validate content
            if (!json.has("version") || !json.has("name")) {
                return@withContext RecoveryResult.Error("Invalid autosave format")
            }

            RecoveryResult.Recovered(json.toString(2))
        } catch (e: Exception) {
            RecoveryResult.Error("Recovery failed: ${e.message}")
        }
    }

    /**
     * Attempt recovery with latest autosave.
     */
    suspend fun attemptRecovery(projectName: String): RecoveryResult = withContext(Dispatchers.IO) {
        val latestAutosave = findLatestAutosave(projectName)

        if (latestAutosave == null) {
            return@withContext RecoveryResult.NoAutosaveFound
        }

        recoverFromAutosave(latestAutosave)
    }

    /**
     * Create backup before save.
     */
    suspend fun createBackup(projectDir: File): Boolean = withContext(Dispatchers.IO) {
        try {
            val projectFile = File(projectDir, "project.json")
            if (!projectFile.exists()) return@withContext false

            val timestamp = System.currentTimeMillis()
            val backupFile = File(backupDir, "backup_${timestamp}.json")

            projectFile.copyTo(backupFile, overwrite = true)

            // Keep only last 5 backups
            cleanupOldBackups()

            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Recover from backup.
     */
    suspend fun recoverFromBackup(timestamp: Long): RecoveryResult = withContext(Dispatchers.IO) {
        val backupFile = File(backupDir, "backup_${timestamp}.json")

        if (!backupFile.exists()) {
            return@withContext RecoveryResult.Error("Backup not found")
        }

        try {
            val content = backupFile.readText()
            RecoveryResult.Recovered(content)
        } catch (e: Exception) {
            RecoveryResult.Error("Backup recovery failed: ${e.message}")
        }
    }

    /**
     * List available backups.
     */
    fun listBackups(): List<BackupFile> {
        return backupDir.listFiles()
            ?.filter { it.name.startsWith("backup_") && it.extension == "json" }
            ?.map { file ->
                BackupFile(
                    file = file,
                    timestamp = file.lastModified()
                )
            }
            ?.sortedByDescending { it.timestamp }
            ?: emptyList()
    }

    /**
     * Save autosave snapshot.
     */
    suspend fun saveAutosave(projectName: String, projectData: ProjectData) = withContext(Dispatchers.IO) {
        try {
            val timestamp = System.currentTimeMillis()
            val autosaveFile = File(autosaveDir, "autosave_${projectName}_${timestamp}.json")

            val serializer = ProjectSerializer(scope)
            val serialized = serializer.serialize(projectData)
            autosaveFile.writeText(serialized)

            // Cleanup old autosaves
            cleanupOldAutosaves(projectName)

            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Cleanup old autosaves.
     */
    private fun cleanupOldAutosaves(projectName: String, keepCount: Int = 3) {
        val autosaves = findAllAutosaves(projectName)
        
        if (autosaves.size > keepCount) {
            autosaves.drop(keepCount).forEach { autosave ->
                autosave.file.delete()
            }
        }
    }

    /**
     * Cleanup old backups.
     */
    private fun cleanupOldBackups(keepCount: Int = 5) {
        val backups = listBackups()

        if (backups.size > keepCount) {
            backups.drop(keepCount).forEach { backup ->
                backup.file.delete()
            }
        }
    }

    /**
     * Validate recovery candidate.
     */
    suspend fun validateCandidate(file: File): ValidationResult = withContext(Dispatchers.IO) {
        try {
            if (!file.exists()) {
                return@withContext ValidationResult.Invalid(listOf("File not found"))
            }

            val content = file.readText()
            val json = org.json.JSONObject(content)

            val issues = mutableListOf<String>()

            // Basic validation
            if (!json.has("version")) issues.add("Missing version")
            if (!json.has("name")) issues.add("Missing project name")
            if (!json.has("timeline")) issues.add("Missing timeline")

            if (issues.isEmpty()) ValidationResult.Valid else ValidationResult.Invalid(issues)
        } catch (e: Exception) {
            ValidationResult.Invalid(listOf("Parse error: ${e.message}"))
        }
    }
}

/**
 * Recovery result.
 */
sealed class RecoveryResult {
    data class Recovered(val content: String) : RecoveryResult()
    data class Error(val message: String) : RecoveryResult()
    object NoAutosaveFound : RecoveryResult()
}

/**
 * Autosave file info.
 */
data class AutosaveFile(
    val file: File,
    val timestamp: Long,
    val projectName: String
)

/**
 * Backup file info.
 */
data class BackupFile(
    val file: File,
    val timestamp: Long
)