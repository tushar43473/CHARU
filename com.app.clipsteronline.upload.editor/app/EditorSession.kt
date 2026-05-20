package com.app.clipsteronline.upload.editor.app

import java.util.UUID

data class EditorSession(
    val id: String = UUID.randomUUID().toString(),
    val projectId: String,
    val createdAtMs: Long = System.currentTimeMillis(),
    val updatedAtMs: Long = createdAtMs,
    val lastSnapshot: EditorState? = null,
)
