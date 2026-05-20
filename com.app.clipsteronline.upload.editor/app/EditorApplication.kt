package com.app.clipsteronline.upload.editor.app

class EditorApplication {
    val dependencies: DependencyProvider = DependencyProvider.global()
    val moduleRegistry: EditorModuleRegistry = EditorModuleRegistry()
    val initializer: EditorInitializer = EditorInitializer(dependencies, moduleRegistry)

    fun boot(): InitializationReport {
        val future = initializer.initializeAsync()
        return future.get()
    }

    fun onForeground() {
        moduleRegistry.dispatchForeground()
    }

    fun onBackground() {
        moduleRegistry.dispatchBackground()
    }

    fun shutdown() {
        initializer.release()
    }
}
