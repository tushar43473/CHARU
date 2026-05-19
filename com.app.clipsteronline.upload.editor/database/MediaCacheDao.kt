package com.app.clipsteronline.upload.editor.database

import androidx.room.*

/**
 * Media cache DAO.
 * Cached asset management.
 */
@Dao
interface MediaCacheDao {

    /**
     * Insert cache entry.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cacheEntry: MediaCacheEntity)

    /**
     * Update cache entry.
     */
    @Update
    suspend fun update(cacheEntry: MediaCacheEntity)

    /**
     * Delete cache entry.
     */
    @Delete
    suspend fun delete(cacheEntry: MediaCacheEntity)

    /**
     * Get cache entry.
     */
    @Query("SELECT * FROM media_cache WHERE uri = :uri")
    suspend fun getByUri(uri: String): MediaCacheEntity?

    /**
     * Get all entries.
     */
    @Query("SELECT * FROM media_cache ORDER BY lastAccessed DESC")
    suspend fun getAll(): List<MediaCacheEntity>

    /**
     * Get stale entries.
     */
    @Query("SELECT * FROM media_cache WHERE lastAccessed < :threshold ORDER BY lastAccessed ASC")
    suspend fun getStaleEntries(threshold: Long): List<MediaCacheEntity>

    /**
     * Update last accessed.
     */
    @Query("UPDATE media_cache SET lastAccessed = :timestamp WHERE uri = :uri")
    suspend fun updateLastAccessed(uri: String, timestamp: Long)

    /**
     * Update thumbnail path.
     */
    @Query("UPDATE media_cache SET thumbnailPath = :path WHERE uri = :uri")
    suspend fun updateThumbnailPath(uri: String, path: String)

    /**
     * Update waveform path.
     */
    @Query("UPDATE media_cache SET waveformPath = :path WHERE uri = :uri")
    suspend fun updateWaveformPath(uri: String, path: String)

    /**
     * Update proxy path.
     */
    @Query("UPDATE media_cache SET proxyPath = :path WHERE uri = :uri")
    suspend fun updateProxyPath(uri: String, path: String)

    /**
     * Get total cache size.
     */
    @Query("SELECT SUM(sizeBytes) FROM media_cache")
    suspend fun getTotalSize(): Long?

    /**
     * Delete oldest entries.
     */
    @Query("DELETE FROM media_cache WHERE uri IN (SELECT uri FROM media_cache ORDER BY lastAccessed ASC LIMIT :count)")
    suspend fun deleteOldest(count: Int)

    /**
     * Clear all cache.
     */
    @Query("DELETE FROM media_cache")
    suspend fun clearAll()

    /**
     * Get cache count.
     */
    @Query("SELECT COUNT(*) FROM media_cache")
    suspend fun getCount(): Int

    /**
     * Cleanup by size limit.
     */
    suspend fun cleanupToSize(maxBytes: Long): Long {
        var freed = 0L
        val stale = getStaleEntries(System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L))
        
        var currentSize = getTotalSize() ?: 0L
        
        for (entry in stale) {
            if (currentSize <= maxBytes) break
            delete(entry)
            freed += entry.sizeBytes
            currentSize -= entry.sizeBytes
        }
        
        return freed
    }
}