package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.model.MediaRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {

    @Query("SELECT * FROM media_records ORDER BY timestamp DESC")
    fun getAllMedia(): Flow<List<MediaRecord>>

    @Query("SELECT * FROM media_records WHERE isAccessible = 1 ORDER BY timestamp DESC")
    fun getAccessibleMedia(): Flow<List<MediaRecord>>

    @Query("SELECT * FROM media_records WHERE isAccessible = 0 ORDER BY timestamp DESC")
    fun getReferenceMedia(): Flow<List<MediaRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(media: MediaRecord): Long

    @Query("DELETE FROM media_records WHERE messageId = :messageId")
    suspend fun deleteByMessageId(messageId: Long): Int

    @Query("DELETE FROM media_records WHERE messageId IN (:messageIds)")
    suspend fun deleteByMessageIds(messageIds: List<Long>): Int

    @Query("DELETE FROM media_records")
    suspend fun clearAll(): Int

    @Query("SELECT filePath FROM media_records WHERE filePath != ''")
    suspend fun getAllFilePaths(): List<String>
}
