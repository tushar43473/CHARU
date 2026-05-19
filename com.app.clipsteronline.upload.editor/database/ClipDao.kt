package com.app.clipsteronline.upload.editor.database

import androidx.room.*

/**
 * Clip DAO.
 * CRUD operations for timeline clips.
 */
@Dao
interface ClipDao {

    /**
     * Insert clip.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(clip: ClipEntity)

    /**
     * Insert clips batch.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(clips: List<ClipEntity>)

    /**
     * Update clip.
     */
    @Update
    suspend fun update(clip: ClipEntity)

    /**
     * Delete clip.
     */
    @Delete
    suspend fun delete(clip: ClipEntity)

    /**
     * Get clip by ID.
     */
    @Query("SELECT * FROM clips WHERE id = :clipId")
    suspend fun getById(clipId: String): ClipEntity?

    /**
     * Get clips for project.
     */
    @Query("SELECT * FROM clips WHERE projectId = :projectId ORDER BY orderIndex ASC")
    suspend fun getForProject(projectId: Long): List<ClipEntity>

    /**
     * Get clips for track.
     */
    @Query("SELECT * FROM clips WHERE projectId = :projectId AND trackId = :trackId ORDER BY orderIndex ASC")
    suspend fun getForTrack(projectId: Long, trackId: String): List<ClipEntity>

    /**
     * Delete clips for project.
     */
    @Query("DELETE FROM clips WHERE projectId = :projectId")
    suspend fun deleteForProject(projectId: Long)

    /**
     * Delete clips for track.
     */
    @Query("DELETE FROM clips WHERE projectId = :projectId AND trackId = :trackId")
    suspend fun deleteForTrack(projectId: Long, trackId: String)

    /**
     * Update clip times.
     */
    @Query("UPDATE clips SET startMs = :startMs, endMs = :endMs WHERE id = :clipId")
    suspend fun updateTimes(clipId: String, startMs: Long, endMs: Long)

    /**
     * Update clip trim.
     */
    @Query("UPDATE clips SET trimStartMs = :trimStart, trimEndMs = :trimEnd WHERE id = :clipId")
    suspend fun updateTrim(clipId: String, trimStart: Long, trimEnd: Long)

    /**
     * Reorder clips.
     */
    @Query("UPDATE clips SET orderIndex = :index WHERE id = :clipId")
    suspend fun reorder(clipId: String, index: Int)

    /**
     * Update track.
     */
    @Query("UPDATE clips SET trackId = :trackId WHERE id = :clipId")
    suspend fun updateTrack(clipId: String, trackId: String)

    /**
     * Get clip count.
     */
    @Query("SELECT COUNT(*) FROM clips WHERE projectId = :projectId")
    suspend fun getCountForProject(projectId: Long): Int
}