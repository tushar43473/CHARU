package com.app.clipsteronline.upload.editor.database

import androidx.room.*

/**
 * Effect DAO.
 * CRUD operations for effects.
 */
@Dao
interface EffectDao {

    /**
     * Insert effect.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(effect: EffectEntity)

    /**
     * Insert effects batch.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(effects: List<EffectEntity>)

    /**
     * Update effect.
     */
    @Update
    suspend fun update(effect: EffectEntity)

    /**
     * Delete effect.
     */
    @Delete
    suspend fun delete(effect: EffectEntity)

    /**
     * Get effect by ID.
     */
    @Query("SELECT * FROM effects WHERE id = :effectId")
    suspend fun getById(effectId: String): EffectEntity?

    /**
     * Get effects for project.
     */
    @Query("SELECT * FROM effects WHERE projectId = :projectId ORDER BY startMs ASC")
    suspend fun getForProject(projectId: Long): List<EffectEntity>

    /**
     * Get effects at time.
     */
    @Query("SELECT * FROM effects WHERE projectId = :projectId AND startMs <= :timeMs AND endMs > :timeMs ORDER BY startMs ASC")
    suspend fun getAtTime(projectId: Long, timeMs: Long): List<EffectEntity>

    /**
     * Get effects by type.
     */
    @Query("SELECT * FROM effects WHERE projectId = :projectId AND type = :type ORDER BY startMs ASC")
    suspend fun getByType(projectId: Long, type: String): List<EffectEntity>

    /**
     * Delete effects for project.
     */
    @Query("DELETE FROM effects WHERE projectId = :projectId")
    suspend fun deleteForProject(projectId: Long)

    /**
     * Delete effects for track.
     */
    @Query("DELETE FROM effects WHERE projectId = :projectId AND type = :type")
    suspend fun deleteByType(projectId: Long, type: String)

    /**
     * Update effect times.
     */
    @Query("UPDATE effects SET startMs = :startMs, endMs = :endMs WHERE id = :effectId")
    suspend fun updateTimes(effectId: String, startMs: Long, endMs: Long)

    /**
     * Update intensity.
     */
    @Query("UPDATE effects SET intensity = :intensity WHERE id = :effectId")
    suspend fun updateIntensity(effectId: String, intensity: Float)

    /**
     * Get effect count.
     */
    @Query("SELECT COUNT(*) FROM effects WHERE projectId = :projectId")
    suspend fun getCountForProject(projectId: Long): Int
}