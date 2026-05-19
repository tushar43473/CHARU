package com.app.clipsteronline.upload.editor.database

import androidx.room.*

/**
 * Project DAO.
 * CRUD operations for projects.
 */
@Dao
interface ProjectDao {

    /**
     * Insert project.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(project: ProjectEntity): Long

    /**
     * Update project.
     */
    @Update
    suspend fun update(project: ProjectEntity)

    /**
     * Delete project.
     */
    @Delete
    suspend fun delete(project: ProjectEntity)

    /**
     * Get project by ID.
     */
    @Query("SELECT * FROM projects WHERE id = :projectId")
    suspend fun getById(projectId: Long): ProjectEntity?

    /**
     * Get all projects.
     */
    @Query("SELECT * FROM projects ORDER BY modifiedAt DESC")
    suspend fun getAll(): List<ProjectEntity>

    /**
     * Get recent projects.
     */
    @Query("SELECT * FROM projects WHERE isDraft = 0 ORDER BY modifiedAt DESC LIMIT :limit")
    suspend fun getRecent(limit: Int = 20): List<ProjectEntity>

    /**
     * Get drafts.
     */
    @Query("SELECT * FROM projects WHERE isDraft = 1 ORDER BY modifiedAt DESC")
    suspend fun getDrafts(): List<ProjectEntity>

    /**
     * Search projects.
     */
    @Query("SELECT * FROM projects WHERE name LIKE '%' || :query || '%' ORDER BY modifiedAt DESC")
    suspend fun search(query: String): List<ProjectEntity>

    /**
     * Delete by ID.
     */
    @Query("DELETE FROM projects WHERE id = :projectId")
    suspend fun deleteById(projectId: Long)

    /**
     * Get project count.
     */
    @Query("SELECT COUNT(*) FROM projects")
    suspend fun getCount(): Int

    /**
     * Update modified time.
     */
    @Query("UPDATE projects SET modifiedAt = :timestamp WHERE id = :projectId")
    suspend fun updateModifiedAt(projectId: Long, timestamp: Long)

    /**
     * Update thumbnail path.
     */
    @Query("UPDATE projects SET thumbnailPath = :path WHERE id = :projectId")
    suspend fun updateThumbnail(projectId: Long, path: String)

    /**
     * Update draft flag.
     */
    @Query("UPDATE projects SET isDraft = :isDraft WHERE id = :projectId")
    suspend fun updateDraftFlag(projectId: Long, isDraft: Boolean)
}