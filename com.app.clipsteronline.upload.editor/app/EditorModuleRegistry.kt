package com.app.clipsteronline.upload.editor.app

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

enum class ModuleState { REGISTERED, CREATED, FOREGROUND, BACKGROUND, DESTROYED, FAILED }

class EditorModuleRegistry {
    private val modules = ConcurrentHashMap<String, LifecycleController>()
    private val enabled = ConcurrentHashMap<String, Boolean>()
    private val states = ConcurrentHashMap<String, ModuleState>()
    private val order = CopyOnWriteArrayList<String>()

    fun register(module: LifecycleController, enabledByDefault: Boolean = true) {
        check(!modules.containsKey(module.id)) { "Module already registered: ${module.id}" }
        modules[module.id] = module
        enabled[module.id] = enabledByDefault
        states[module.id] = ModuleState.REGISTERED
        order += module.id
    }

    fun setEnabled(moduleId: String, isEnabled: Boolean) {
        check(modules.containsKey(moduleId)) { "Unknown module: $moduleId" }
        enabled[moduleId] = isEnabled
    }

    fun isEnabled(moduleId: String): Boolean = enabled[moduleId] == true

    fun state(moduleId: String): ModuleState? = states[moduleId]

    fun initializeAll(dependencies: DependencyProvider): List<String> {
        val failed = mutableListOf<String>()
        for (id in order) {
            if (!isEnabled(id)) continue
            val module = modules[id] ?: continue
            try {
                module.onCreate(dependencies)
                states[id] = ModuleState.CREATED
            } catch (_: Throwable) {
                states[id] = ModuleState.FAILED
                failed += id
            }
        }
        return failed
    }

    fun dispatchForeground() = dispatch(ModuleState.FOREGROUND) { it.onForeground() }

    fun dispatchBackground() = dispatch(ModuleState.BACKGROUND) { it.onBackground() }

    fun destroyAll() = dispatch(ModuleState.DESTROYED) { it.onDestroy() }

    private fun dispatch(state: ModuleState, action: (LifecycleController) -> Unit) {
        for (id in order) {
            if (!isEnabled(id)) continue
            val module = modules[id] ?: continue
            try {
                action(module)
                states[id] = state
            } catch (_: Throwable) {
                states[id] = ModuleState.FAILED
            }
        }
    }
}
