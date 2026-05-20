package com.app.clipsteronline.upload.editor.app

interface LifecycleController {
    val id: String
    fun onCreate(dependencies: DependencyProvider)
    fun onForeground()
    fun onBackground()
    fun onDestroy()
}
