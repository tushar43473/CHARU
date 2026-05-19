package com.app.clipsteronline.upload.editor.timeline.engine

/**
 * Immutable timeline state.
 * Contains zoom, scroll, selection, and editing state.
 */
data class TimelineState(
    val duration: Long = 0L,
    val currentPosition: Long = 0L,
    val zoom: Float = 1f,
    val scrollX: Float = 0f,
    val trackCount: Int = 0,
    val tracks: List<TimelineTrack> = emptyList(),
    val selectedClipIds: List<String> = emptyList(),
    val selectedTrackId: String? = null,
    val isPlaying: Boolean = false,
    val isEditing: Boolean = false,
    val isDragging: Boolean = false,
    val isScrolling: Boolean = false,
    val isZooming: Boolean = false,
    val editingMode: EditingMode = EditingMode.SELECT,
    val selectionMode: SelectionMode = SelectionMode.SINGLE
) {
    /**
     * Visible time range.
     */
    val visibleStartMs: Long get() = (scrollX * 1000 / (zoom * 100)).toLong()
    val visibleEndMs: Long get() = visibleStartMs + (zoom * 100 * 100 / 1000).toLong()

    /**
     * Playhead position as progress.
     */
    val progress: Float
        get() = if (duration > 0) currentPosition.toFloat() / duration else 0f

    /**
     * Has selection.
     */
    val hasSelection: Boolean get() = selectedClipIds.isNotEmpty()

    /**
     * Is a clip selected.
     */
    fun isClipSelected(clipId: String): Boolean = clipId in selectedClipIds

    /**
     * Is track selected.
     */
    fun isTrackSelected(trackId: String): Boolean = selectedTrackId == trackId

    /**
     * With position.
     */
    fun withPosition(position: Long): TimelineState = copy(currentPosition = position)

    /**
     * With scroll.
     */
    fun withScroll(scroll: Float): TimelineState = copy(scrollX = scroll)

    /**
     * With zoom.
     */
    fun withZoom(zoom: Float): TimelineState = copy(zoom = zoom.coerceIn(0.1f, 10f))

    /**
     * With selection.
     */
    fun withSelection(clipIds: List<String>): TimelineState = copy(selectedClipIds = clipIds)

    /**
     * With track selection.
     */
    fun withTrackSelection(trackId: String?): TimelineState = copy(selectedTrackId = trackId)

    /**
     * With editing mode.
     */
    fun withEditingMode(mode: EditingMode): TimelineState = copy(editingMode = mode)

    /**
     * With playing state.
     */
    fun withPlaying(playing: Boolean): TimelineState = copy(isPlaying = playing)

    /**
     * Add clip to selection.
     */
    fun addToSelection(clipId: String): TimelineState {
        return when (selectionMode) {
            SelectionMode.SINGLE -> copy(selectedClipIds = listOf(clipId))
            SelectionMode.MULTI -> copy(selectedClipIds = selectedClipIds + clipId)
            SelectionMode.ADD -> copy(selectedClipIds = selectedClipIds + clipId)
        }
    }

    /**
     * Remove clip from selection.
     */
    fun removeFromSelection(clipId: String): TimelineState {
        return copy(selectedClipIds = selectedClipIds - clipId)
    }

    /**
     * Clear selection.
     */
    fun clearSelection(): TimelineState {
        return copy(selectedClipIds = emptyList())
    }

    companion object {
        val EMPTY = TimelineState()
    }
}

/**
 * Editing mode.
 */
enum class EditingMode {
    SELECT,
    TRIM,
    SPLIT,
    MOVE,
    DELETE
}

/**
 * Selection mode.
 */
enum class SelectionMode {
    SINGLE,
    MULTI,
    ADD
}

/**
 * Playback direction.
 */
enum class Direction {
    FORWARD,
    BACKWARD
}

/**
 * Timeline track for state.
 */
data class TimelineTrack(
    val id: String,
    val name: String,
    val type: TrackType = TrackType.VIDEO,
    val clips: List<String> = emptyList(),
    val isVisible: Boolean = true,
    val isLocked: Boolean = false
)

/**
 * Track type.
 */
enum class TrackType {
    VIDEO,
    AUDIO,
    TEXT,
    STICKER,
    EFFECT
}