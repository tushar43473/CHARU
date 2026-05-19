package com.app.clipsteronline.upload.editor.timeline.engine

import android.net.Uri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.app.clipsteronline.upload.editor.core.model.Clip
import com.app.clipsteronline.upload.editor.core.model.Project
import com.app.clipsteronline.upload.editor.core.model.Timeline
import com.app.clipsteronline.upload.editor.core.model.TimelineTrack
import com.app.clipsteronline.upload.editor.core.model.Track

/**
 * Central timeline coordinator.
 * Manages timeline state, tracks, and playback synchronization.
 */
class TimelineEngine(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
) {
    private val _state = MutableStateFlow(TimelineState())
    val state: StateFlow<TimelineState> = _state.asStateFlow()

    private var timeline: Timeline = Timeline()
    private var playheadPositionMs: Long = 0L

    private var timelineListener: TimelineListener? = null

    private val scrollEngine = TimelineScrollEngine(scope)
    private val zoomEngine = TimelineZoomEngine()
    private val snapEngine = TimelineSnapEngine()

    init {
        scope.launch {
            scrollEngine.scroll.collect { scroll ->
                updateState { copy(currentScroll = scroll) }
            }
        }

        scope.launch {
            zoomEngine.zoom.collect { zoom ->
                updateState { copy(zoom = zoom) }
            }
        }
    }

    /**
     * Load timeline from project.
     */
    fun loadTimeline(project: Project) {
        timeline = project.timeline
        updateState {
            copy(
                duration = project.timeline.durationMs,
                trackCount = project.timeline.tracks.size
            )
        }
    }

    /**
     * Load timeline directly.
     */
    fun loadTimeline(timeline: Timeline) {
        this.timeline = timeline
        updateState {
            copy(
                duration = timeline.durationMs,
                trackCount = timeline.tracks.size
            )
        }
    }

    /**
     * Set playhead position.
     */
    fun setPlayheadPosition(positionMs: Long) {
        playheadPositionMs = positionMs.coerceIn(0L, _state.value.duration)
        updateState { copy(playheadPosition = playheadPositionMs) }
        timelineListener?.onPlayheadMoved(playheadPositionMs)
    }

    /**
     * Add track to timeline.
     */
    fun addTrack(track: TimelineTrack) {
        val updatedTracks = timeline.tracks + track
        timeline = timeline.copy(tracks = updatedTracks, tracks = updatedTracks)
        updateState { copy(trackCount = updatedTracks.size) }
        timelineListener?.onTrackAdded(track)
    }

    /**
     * Remove track from timeline.
     */
    fun removeTrack(trackId: String) {
        val updatedTracks = timeline.tracks.filter { it.id != trackId }
        timeline = timeline.copy(tracks = updatedTracks)
        updateState { copy(trackCount = updatedTracks.size) }
        timelineListener?.onTrackRemoved(trackId)
    }

    /**
     * Add clip to track.
     */
    fun addClip(clip: Clip, trackId: String) {
        val track = timeline.getTrackById(trackId) ?: return
        val updatedTrack = track.addClip(clip)
        updateTrack(trackId, updatedTrack)
    }

    /**
     * Remove clip from track.
     */
    fun removeClip(clipId: String, trackId: String) {
        val track = timeline.getTrackById(trackId) ?: return
        val updatedTrack = track.removeClip(clipId)
        updateTrack(trackId, updatedTrack)
    }

    /**
     * Move clip.
     */
    fun moveClip(clipId: String, fromTrackId: String, toTrackId: String, newStartMs: Long) {
        val fromTrack = timeline.getTrackById(fromTrackId) ?: return
        val targetTrack = timeline.getTrackById(toTrackId) ?: return

        // Get the clip
        val clip = fromTrack.getClipById(clipId)?.let { it } ?: return

        // Remove from source
        val updatedFrom = fromTrack.removeClip(clipId)

        // Create new clip with updated position
        val movedClip = clip.copy(
            timelineStartMs = newStartMs,
            timelineEndMs = newStartMs + clip.durationMs
        )

        // Add to target
        val updatedTarget = targetTrack.addClip(movedClip)

        // Update timeline
        val updatedTracks = timeline.tracks.map {
            when (it.id) {
                fromTrackId -> updatedFrom
                toTrackId -> updatedTarget
                else -> it
            }
        }

        timeline = timeline.copy(tracks = updatedTracks)
        timelineListener?.onClipMoved(clipId, fromTrackId, toTrackId, newStartMs)
    }

    /**
     * Update track.
     */
    fun updateTrack(trackId: String, updatedTrack: TimelineTrack) {
        val updatedTracks = timeline.tracks.map {
            if (it.id == trackId) updatedTrack else it
        }
        timeline = timeline.copy(tracks = updatedTracks)
        timelineListener?.onTrackUpdated(trackId)
    }

    /**
     * Get scroll engine.
     */
    fun getScrollEngine(): TimelineScrollEngine = scrollEngine

    /**
     * Get zoom engine.
     */
    fun getZoomEngine(): TimelineZoomEngine = zoomEngine

    /**
     * Get snap engine.
     */
    fun getSnapEngine(): TimelineSnapEngine = snapEngine

    /**
     * Get duration.
     */
    fun getDuration(): Long = _state.value.duration

    /**
     * Get track.
     */
    fun getTrack(trackId: String): TimelineTrack? = timeline.getTrackById(trackId)

    /**
     * Get all tracks.
     */
    fun getTracks(): List<TimelineTrack> = timeline.tracks

    /**
     * Get playhead position.
     */
    fun getPlayheadPosition(): Long = playheadPositionMs

    /**
     * Set timeline listener.
     */
    fun setTimelineListener(listener: TimelineListener?) {
        this.timelineListener = listener
    }

    /**
     * Update state.
     */
    private fun updateState(update: (TimelineState) -> TimelineState) {
        _state.value = update(_state.value)
    }
}

/**
 * Timeline state.
 */
data class TimelineState(
    val duration: Long = 0L,
    val playheadPosition: Long = 0L,
    val currentScroll: Float = 0f,
    val zoom: Float = 1f,
    val trackCount: Int = 0,
    val selectedClips: List<String> = emptyList(),
    val selectedTrack: String? = null,
    val isEditing: Boolean = false,
    val isDragging: Boolean = false,
    val isScrolling: Boolean = false
)

/**
 * Timeline listener interface.
 */
interface TimelineListener {
    fun onPlayheadMoved(positionMs: Long)
    fun onTrackAdded(track: TimelineTrack)
    fun onTrackRemoved(trackId: String)
    fun onTrackUpdated(trackId: String)
    fun onClipAdded(clipId: String, trackId: String)
    fun onClipRemoved(clipId: String, trackId: String)
    fun onClipMoved(clipId: String, fromTrackId: String, toTrackId: String, newStartMs: Long)
    fun onSelectionChanged(clipIds: List<String>)
    fun onStateChanged(state: TimelineState)
}