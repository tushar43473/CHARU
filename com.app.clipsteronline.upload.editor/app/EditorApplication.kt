package com.app.clipsteronline.upload.editor.app

import android.app.Application
import android.content.Context
import com.app.clipsteronline.upload.editor.database.EditorDatabase
import com.app.clipsteronline.upload.editor.performance.MemoryManager
import com.app.clipsteronline.upload.editor.performance.BackgroundTaskManager

/**
 * Editor Application.
 * Global initialization, system startup.
 */
class EditorApplication : Application() {

    lateinit var database: EditorDatabase
        private set

    lateinit var memoryManager: MemoryManager
        private set

    lateinit var taskManager: BackgroundTaskManager
        private set

    private var isInitialized = false

    override fun onCreate() {
        super.onCreate()

        instance = this

        // Initialize core systems
        initializeCore()

        isInitialized = true
    }

    /**
     * Initialize core editor systems.
     */
    private fun initializeCore() {
        // Database
        database = EditorDatabase.getInstance(this)

        // Performance managers
        memoryManager = MemoryManager(this)
        taskManager = BackgroundTaskManager(this)
    }

    /**
     * Get context.
     */
    fun getContext(): Context = applicationContext

    /**
     * Is initialized.
     */
    fun isReady(): Boolean = isInitialized

    /**
     * Handle low memory.
     */
    fun onLowMemory() {
        memoryManager.onLowMemory()
    }

    /**
     * Handle trim memory.
     */
    fun onTrimMemory(level: Int) {
        when {
            level >= android.content.ComponentCallbacks2.TRIM_MEMORY_MODERATE -> {
                memoryManager.clearCache()
            }
        }
    }

    companion object {
        lateinit var instance: EditorApplication
            private set
    }
}