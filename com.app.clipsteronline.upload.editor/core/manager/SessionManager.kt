package com.app.clipsteronline.upload.editor.core.manager

import com.app.clipsteronline.upload.editor.app.EditorSession
import com.app.clipsteronline.upload.editor.app.EditorState
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

class SessionManager {
    private val sessionStore = ConcurrentHashMap<String, EditorSession>()
    private val activeSessionId = AtomicReference<String?>(null)

    fun startSession(projectId: String): EditorSession {
        require(projectId.isNotBlank())
        val session = EditorSession(projectId = projectId)
        sessionStore[session.id] = session
        activeSessionId.set(session.id)
        return session
    }

    fun activeSession(): EditorSession? = activeSessionId.get()?.let { sessionStore[it] }

    fun updateSnapshot(state: EditorState) {
        val id = activeSessionId.get() ?: return
        val existing = sessionStore[id] ?: return
        sessionStore[id] = existing.copy(updatedAtMs = System.currentTimeMillis(), lastSnapshot = state)
    }

    fun restore(sessionId: String): EditorSession? {
        val session = sessionStore[sessionId] ?: return null
        activeSessionId.set(session.id)
        return session
    }

    fun endActiveSession() {
        activeSessionId.set(null)
    }
}
