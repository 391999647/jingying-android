package com.jingying.movie.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jingying.movie.data.local.entity.HistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {

    @Query("SELECT * FROM history ORDER BY updatedAt DESC")
    fun getHistoryFlow(): Flow<List<HistoryEntity>>

    @Query("SELECT * FROM history ORDER BY updatedAt DESC LIMIT :limit")
    suspend fun getHistory(limit: Int = 100): List<HistoryEntity>

    @Query("SELECT * FROM history WHERE vodId = :vodId AND episodeUrl = :episodeUrl LIMIT 1")
    suspend fun getHistoryByEpisode(vodId: Int, episodeUrl: String): HistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: HistoryEntity)

    @Query("DELETE FROM history WHERE vodId = :vodId AND episodeUrl = :episodeUrl")
    suspend fun deleteHistory(vodId: Int, episodeUrl: String)

    @Query("DELETE FROM history")
    suspend fun clearAll()
}
