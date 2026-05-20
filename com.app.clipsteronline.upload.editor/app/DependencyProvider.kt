package com.app.clipsteronline.upload.editor.app

import java.util.concurrent.ConcurrentHashMap

class DependencyProvider private constructor() {
    private val services = ConcurrentHashMap<Class<*>, Any>()

    fun <T : Any> put(type: Class<T>, instance: T) {
        services[type] = instance
    }

    fun <T : Any> get(type: Class<T>): T {
        val value = services[type] ?: error("Dependency not found: ${type.name}")
        @Suppress("UNCHECKED_CAST")
        return value as T
    }

    fun <T : Any> getOrNull(type: Class<T>): T? {
        @Suppress("UNCHECKED_CAST")
        return services[type] as? T
    }

    fun contains(type: Class<*>): Boolean = services.containsKey(type)

    fun remove(type: Class<*>) {
        services.remove(type)
    }

    fun clear() {
        services.clear()
    }

    companion object {
        @Volatile
        private var instance: DependencyProvider? = null

        fun global(): DependencyProvider {
            return instance ?: synchronized(this) {
                instance ?: DependencyProvider().also { instance = it }
            }
        }
    }
}
