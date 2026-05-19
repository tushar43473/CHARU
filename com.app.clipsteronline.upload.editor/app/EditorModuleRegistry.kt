package com.app.clipsteronline.upload.editor.app

/**
 * Editor module registry.
 * Dynamic module registration.
 */
object EditorModuleRegistry {

    private val renderModules = mutableMapOf<String, RenderModule>()
    private val effectModules = mutableMapOf<String, EffectModule>()
    private val exportModules = mutableMapOf<String, ExportModule>()
    private val featureFlags = mutableMapOf<String, Boolean>()

    /**
     * Register render module.
     */
    fun registerRenderModule(module: RenderModule) {
        renderModules[module.id] = module
    }

    /**
     * Register effect module.
     */
    fun registerEffectModule(module: EffectModule) {
        effectModules[module.id] = module
    }

    /**
     * Register export module.
     */
    fun registerExportModule(module: ExportModule) {
        exportModules[module.id] = module
    }

    /**
     * Get render module.
     */
    fun getRenderModule(id: String): RenderModule? = renderModules[id]

    /**
     * Get effect module.
     */
    fun getEffectModule(id: String): EffectModule? = effectModules[id]

    /**
     * Get export module.
     */
    fun getExportModule(id: String): ExportModule? = exportModules[id]

    /**
     * Get all render modules.
     */
    fun getRenderModules(): List<RenderModule> = renderModules.values.toList()

    /**
     * Get all effect modules.
     */
    fun getEffectModules(): List<EffectModule> = effectModules.values.toList()

    /**
     * Get all export modules.
     */
    fun getExportModules(): List<ExportModule> = exportModules.values.toList()

    /**
     * Enable feature.
     */
    fun enableFeature(featureId: String, enabled: Boolean) {
        featureFlags[featureId] = enabled
    }

    /**
     * Is feature enabled.
     */
    fun isFeatureEnabled(featureId: String): Boolean = featureFlags[featureId] ?: false

    /**
     * Unregister module.
     */
    fun unregister(id: String) {
        renderModules.remove(id)
        effectModules.remove(id)
        exportModules.remove(id)
    }

    /**
     * Clear all.
     */
    fun clear() {
        renderModules.clear()
        effectModules.clear()
        exportModules.clear()
    }
}

/**
 * Render module interface.
 */
interface RenderModule {
    val id: String
    val name: String
    fun initialize()
    fun release()
}

/**
 * Effect module interface.
 */
interface EffectModule {
    val id: String
    val name: String
    fun apply()
    fun remove()
    fun update()
}

/**
 * Export module interface.
 */
interface ExportModule {
    val id: String
    val name: String
    fun start()
    fun cancel(): Boolean
    fun getProgress(): Float
}

/**
 * Built-in effect modules.
 */
object BuiltInEffects {
    val BLUR = object : EffectModule {
        override val id = "blur"
        override val name = "Blur"
        override fun apply() {}
        override fun remove() {}
        override fun update() {}
    }

    val VHS = object : EffectModule {
        override val id = "vhs"
        override val name = "VHS"
        override fun apply() {}
        override fun remove() {}
        override fun update() {}
    }

    val GLITCH = object : EffectModule {
        override val id = "glitch"
        override val name = "Glitch"
        override fun apply() {}
        override fun remove() {}
        override fun update() {}
    }
}