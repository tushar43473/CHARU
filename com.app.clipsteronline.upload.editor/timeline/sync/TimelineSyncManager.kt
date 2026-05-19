package com.app.clipsteronline.upload.editor.timeline.sync

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import com.app.clipsteronline.upload.editor.player.VideoPlayer

/**
 * Synchronizes Media3 player with timeline.
 * Coordinates playhead position and updates.
 */
class TimelineSyncManager(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main),
    private val player: VideoPlayer? = null
) {
    private val _syncState = MutableStateFlow(SyncState())
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private var syncJob: Job? = null
    private var updateInterval = UPDATE_INTERVAL_MS

    private var playheadController: PlayheadSyncController? = null
    private var positionTracker: PlaybackPositionTracker? = null

    private var isSyncing = false

    companion object {
        private const val UPDATE_INTERVAL_MS = 16L // ~60fps
        private const val AUTO_SCROLL_THRESHOLD = 50L
    }

    /**
     * Set playhead controller.
     */
    fun setPlayheadController(controller: PlayheadSyncController) {
        this.playheadController = controller
    }

    /**
     * Set position tracker.
     */
    fun setPositionTracker(tracker: PlaybackPositionTracker) {
        this.positionTracker = tracker
    }

    /**
     * Start synchronization.
     */
    fun startSync() {
        if (isSyncing) return
        isSyncing = true

        syncJob = scope.launch {
            while (isActive && isSyncing) {
                updatePosition()
                delay(updateInterval)
            }
        }
    }

    /**
     * Stop synchronization.
     */
    fun stopSync() {
        isSyncing = false
        syncJob?.cancel()
        syncJob = null
    }

    /**
     * Update position from player.
     */
    private fun updatePosition() {
        player?.let { p ->
            val position = p.getCurrentPosition()
            val duration = p.getDuration()

            if (position > 0) {
                _syncState.value = _syncState.value.copy(
                    currentPositionMs = position,
                    durationMs = duration,
                    isPlaying = p.isPlaying()
                )

                // Update playhead
                playheadController?.updatePosition(position)

                // Update tracker
                positionTracker?.updatePosition(position, duration)

                // Check auto-scroll
                checkAutoScroll(position)
            }
        }
    }

    /**
     * Check if timeline should auto-scroll.
     */
    private fun checkAutoScroll(position: Long) {
        if (!isSyncing) return

        // Auto-scroll handled by controller
    }

    /**
     * Set sync interval.
     */
    fun setUpdateInterval(intervalMs: Long) {
        this.updateInterval = intervalMs.coerceIn(8L, 100L)
    }

    /**
     * Seek to position.
     */
    fun seekTo(positionMs: Long) {
        player?.seekTo(positionMs)
        _syncState.value = _syncState.value.copy(
            currentPositionMs = positionMs,
            seekRequested = true
        )
    }

    /**
     * Sync to specific frame.
     */
    fun syncToFrame(frameNumber: Int, frameRate: Int = 30) {
        val positionMs = frameNumber * 1000L / frameRate
        seekTo(positionMs)
    }

    /**
     * Release resources.
     */
    fun release() {
        stopSync()
        playheadController = null
        positionTracker = null
    }
}

/**
 * Synchronization state.
 */
data class SyncState(
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val isPlaying: Boolean = false,
    val seekRequested: Boolean = false,
    val isSynced: Boolean = false
)