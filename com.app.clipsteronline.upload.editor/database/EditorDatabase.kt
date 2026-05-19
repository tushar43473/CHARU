package com.app.clipsteronline.upload.editor.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Dao

/**
 * Editor database.
 * Room configuration, migrations, singleton.
 */
@Database(
    entities = [
        ProjectEntity::class,
        ClipEntity::class,
        EffectEntity::class,
        MediaCacheEntity::class,
        TimelineStateEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class EditorDatabase : RoomDatabase() {

    abstract fun projectDao(): ProjectDao
    abstract fun clipDao(): ClipDao
    abstract fun effectDao(): EffectDao
    abstract fun mediaCacheDao(): MediaCacheDao
    abstract fun timelineStateDao(): TimelineStateDao

    companion object {
        private const val DATABASE_NAME = "editor_database"

        @Volatile
        private var INSTANCE: EditorDatabase? = null

        fun getInstance(context: Context): EditorDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): EditorDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                EditorDatabase::class.java,
                DATABASE_NAME
            )
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}

/**
 * Project entity.
 */
@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val path: String,
    val thumbnailPath: String? = null,
    val duration: Long = 0L,
    val clipCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = System.currentTimeMillis(),
    val isDraft: Boolean = true,
    val isAutosave: Boolean = false
)

/**
 * Clip entity.
 */
@Entity(tableName = "clips")
data class ClipEntity(
    @PrimaryKey
    val id: String,
    val projectId: Long,
    val trackId: String,
    val uri: String,
    val startMs: Long,
    val endMs: Long,
    val trimStartMs: Long = 0L,
    val trimEndMs: Long = 0L,
    val orderIndex: Int = 0
)

/**
 * Effect entity.
 */
@Entity(tableName = "effects")
data class EffectEntity(
    @PrimaryKey
    val id: String,
    val projectId: Long,
    val type: String,
    val startMs: Long,
    val endMs: Long,
    val intensity: Float = 1f,
    val orderIndex: Int = 0
)

/**
 * Media cache entity.
 */
@Entity(tableName = "media_cache")
data class MediaCacheEntity(
    @PrimaryKey
    val uri: String,
    val thumbnailPath: String? = null,
    val waveformPath: String? = null,
    val proxyPath: String? = null,
    val cachedAt: Long = System.currentTimeMillis(),
    val lastAccessed: Long = System.currentTimeMillis(),
    val sizeBytes: Long = 0L
)

/**
 * Timeline state entity.
 */
@Entity(tableName = "timeline_state")
data class TimelineStateEntity(
    @PrimaryKey
    val projectId: Long,
    val playheadPosition: Long = 0L,
    val zoomLevel: Float = 1f,
    val scrollPosition: Float = 0f,
    val selectedClipIds: String = "", // comma-separated
    val selectedTrackId: String? = null,
    val isPlaying: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)