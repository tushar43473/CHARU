package com.app.clipsteronline.upload.editor.project

import com.app.clipsteronline.upload.editor.core.model.Project

class AutosaveEngine(
    private val saver: ProjectSaver,
    private val serializer: ProjectSerializer = ProjectSerializer(),
    private val draftManager: DraftManager = DraftManager(),
) {
    private var lastAutosaveMs: Long = 0L

    fun configure() = Unit

    fun maybeAutosave(project: Project, nowMs: Long = System.currentTimeMillis(), intervalMs: Long = 5_000L): AutosaveResult {
        if (nowMs - lastAutosaveMs < intervalMs) return AutosaveResult(false, lastAutosaveMs, "interval-not-reached")
        val save = saver.save(project)
        return if (save.success) {
            val payload = serializer.serialize(project)
            draftManager.createDraft(project, payload)
            lastAutosaveMs = save.savedAtMs
            AutosaveResult(true, lastAutosaveMs, null)
        } else {
            AutosaveResult(false, lastAutosaveMs, save.error)
        }
    }

    data class AutosaveResult(val saved: Boolean, val timestampMs: Long, val error: String?)
}
