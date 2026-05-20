package com.app.clipsteronline.upload.editor.performance

import java.util.concurrent.ConcurrentLinkedQueue

class BackgroundTaskManager {
    private val queue = ConcurrentLinkedQueue<Task>()

    fun configure() = Unit

    fun enqueue(task: Task) {
        queue.add(task)
    }

    fun drain(maxTasks: Int = 4): List<Task> {
        val out = mutableListOf<Task>()
        repeat(maxTasks.coerceAtLeast(1)) {
            val task = queue.poll() ?: return@repeat
            out += task
        }
        return out
    }

    fun pendingCount(): Int = queue.size

    data class Task(val id: String, val priority: Int = 0, val kind: Kind) {
        enum class Kind { THUMBNAIL, AUTOSAVE, IMPORT, EXPORT }
    }
}
