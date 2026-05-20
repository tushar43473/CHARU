package com.app.clipsteronline.upload.editor.gestures

import com.app.clipsteronline.upload.editor.timeline.engine.SnapResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SnapGuideEngine {
    private val _guideState = MutableStateFlow(SnapGuideState())
    val guideState: StateFlow<SnapGuideState> = _guideState.asStateFlow()

    fun showSnap(result: SnapResult, pxPosition: Float, label: String? = null) {
        if (!result.didSnap || result.target == null) {
            clear()
            return
        }
        _guideState.value = SnapGuideState(
            visible = true,
            lineX = pxPosition,
            label = label ?: result.target.type.name,
            snapTimeMs = result.snappedTimeMs,
        )
    }

    fun clear() {
        if (_guideState.value.visible) {
            _guideState.value = SnapGuideState()
        }
    }
}

data class SnapGuideState(
    val visible: Boolean = false,
    val lineX: Float = 0f,
    val label: String = "",
    val snapTimeMs: Long = 0L,
)
