package com.app.clipsteronline.upload.editor.performance

import android.app.ActivityManager
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MemoryManager(context: Context) {
    private val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

    private val _state = MutableStateFlow(MemoryState())
    val state: StateFlow<MemoryState> = _state.asStateFlow()

    fun sample() {
        val info = ActivityManager.MemoryInfo()
        am.getMemoryInfo(info)
        _state.value = MemoryState(
            availMemBytes = info.availMem,
            totalMemBytes = info.totalMem,
            lowMemory = info.lowMemory,
            pressureLevel = if (info.lowMemory) MemoryPressure.HIGH else MemoryPressure.NORMAL,
        )
    }

    fun forceHighPressure() {
        val s = _state.value
        _state.value = s.copy(pressureLevel = MemoryPressure.HIGH, lowMemory = true)
    }
}

enum class MemoryPressure { NORMAL, HIGH }

data class MemoryState(
    val availMemBytes: Long = 0L,
    val totalMemBytes: Long = 0L,
    val lowMemory: Boolean = false,
    val pressureLevel: MemoryPressure = MemoryPressure.NORMAL,
)
