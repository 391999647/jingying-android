package com.jingying.movie.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jingying.movie.data.local.entity.DetailEntity

@Dao
interface DetailDao {

    @Query("SELECT * FROM details WHERE vodId = :vodId LIMIT 1")
    suspend fun getDetailById(vodId: Int): DetailEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDetail(detail: DetailEntity)

    @Query("DELETE FROM details WHERE cachedAt < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long)

    @Query("DELETE FROM details")
    suspend fun clearAll()
}
