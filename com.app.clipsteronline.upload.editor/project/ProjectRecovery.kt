package com.app.clipsteronline.upload.editor.project

import com.app.clipsteronline.upload.editor.core.model.Project
import com.app.clipsteronline.upload.editor.core.model.TimelineTrack

class ProjectRecovery(
    private val loader: ProjectLoader,
    private val draftManager: DraftManager,
    private val serializer: ProjectSerializer = ProjectSerializer(),
) {
    fun configure() = Unit

    fun recover(projectId: String, tracks: List<TimelineTrack> = emptyList()): RecoveryResult {
        val loaded = loader.load(projectId, tracks)
        if (loaded.project != null) return RecoveryResult(loaded.project, RecoverySource.STORED_PROJECT, null)

        val latestDraft = draftManager.latest(projectId)
        if (latestDraft != null) {
            val fromDraft = serializer.deserialize(latestDraft.payload, tracks)
            if (fromDraft != null) return RecoveryResult(fromDraft, RecoverySource.DRAFT, "recovered-from-draft")
        }
        return RecoveryResult(null, RecoverySource.NONE, loaded.error ?: "no-recovery-state")
    }

    data class RecoveryResult(
        val project: Project?,
        val source: RecoverySource,
        val note: String?,
    )

    enum class RecoverySource { STORED_PROJECT, DRAFT, NONE }
}
