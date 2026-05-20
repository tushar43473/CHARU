package com.app.clipsteronline.upload.editor.app

import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicBoolean

class EditorInitializer(
    private val dependencies: DependencyProvider,
    private val registry: EditorModuleRegistry,
) {
    private val executor = Executors.newSingleThreadExecutor()
    private val initialized = AtomicBoolean(false)
    private val initializing = AtomicBoolean(false)

    fun initializeAsync(): Future<InitializationReport> {
        check(!initialized.get()) { "Editor already initialized" }
        check(initializing.compareAndSet(false, true)) { "Initialization is already running" }

        return executor.submit<InitializationReport> {
            val startedAt = System.currentTimeMillis()
            registerCoreServices()
            val failures = registry.initializeAll(dependencies)
            initialized.set(failures.isEmpty())
            initializing.set(false)
            InitializationReport(
                success = failures.isEmpty(),
                failedModules = failures,
                durationMs = (System.currentTimeMillis() - startedAt),
            )
        }
    }

    fun isInitialized(): Boolean = initialized.get()

    fun release() {
        registry.destroyAll()
        dependencies.clear()
        executor.shutdownNow()
        initialized.set(false)
        initializing.set(false)
    }

    private fun registerCoreServices() {
        dependencies.put(SessionStore::class.java, SessionStore())
        dependencies.put(CacheStore::class.java, CacheStore())
        dependencies.put(StartupMetrics::class.java, StartupMetrics(System.currentTimeMillis()))
    }
}

data class InitializationReport(
    val success: Boolean,
    val failedModules: List<String>,
    val durationMs: Long,
)

class SessionStore {
    @Volatile
    var activeSessionId: String? = null
}

class CacheStore {
    private val data = HashMap<String, Any>()
    @Synchronized fun put(key: String, value: Any) { data[key] = value }
    @Synchronized fun get(key: String): Any? = data[key]
    @Synchronized fun clear() = data.clear()
}

data class StartupMetrics(val startedAtMs: Long)
