package com.app.clipsteronline.upload.editor.database

import androidx.room.*

/**
 * Timeline state DAO.
 * Timeline preference persistence.
 */
@Dao
interface TimelineStateDao {

    /**
     * Insert or update state.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(state: TimelineStateEntity)

    /**
     * Get state for project.
     */
    @Query("SELECT * FROM timeline_state WHERE projectId = :projectId")
    suspend fun getForProject(projectId: Long): TimelineStateEntity?

    /**
     * Delete state for project.
     */
    @Query("DELETE FROM timeline_state WHERE projectId = :projectId")
    suspend fun deleteForProject(projectId: Long)

    /**
     * Update playhead position.
     */
    @Query("UPDATE timeline_state SET playheadPosition = :position WHERE projectId = :projectId")
    suspend fun updatePlayheadPosition(projectId: Long, position: Long)

    /**
     * Update zoom level.
     */
    @Query("UPDATE timeline_state SET zoomLevel = :level WHERE projectId = :projectId")
    suspend fun updateZoomLevel(projectId: Long, level: Float)

    /**
     * Update scroll position.
     */
    @Query("UPDATE timeline_state SET scrollPosition = :position WHERE projectId = :projectId")
    suspend fun updateScrollPosition(projectId: Long, position: Float)

    /**
     * Update selected clips.
     */
    @Query("UPDATE timeline_state SET selectedClipIds = :clipIds WHERE projectId = :projectId")
    fun updateSelectedClips(projectId: Long, clipIds: String) {
        // Implementation requires transaction
    }

    /**
     * Update playing state.
     */
    @Query("UPDATE timeline_state SET isPlaying = :playing WHERE projectId = :projectId")
    suspend fun updatePlayingState(projectId: Long, playing: Boolean)

    /**
     * Save full state.
     */
    suspend fun saveState(projectId: Long, position: Long, zoom: Float, scroll: Float, selectedIds: List<String>) {
        val existing = getForProject(projectId)
        
        val state = TimelineStateEntity(
            projectId = projectId,
            playheadPosition = position,
            zoomLevel = zoom,
            scrollPosition = scroll,
            selectedClipIds = selectedIds.joinToString(","),
            isPlaying = existing?.isPlaying ?: false,
            updatedAt = System.currentTimeMillis()
        )
        
        upsert(state)
    }

    /**
     * Restore state.
     */
    suspend fun restoreState(projectId: Long): TimelineStateEntity? = getForProject(projectId)
}