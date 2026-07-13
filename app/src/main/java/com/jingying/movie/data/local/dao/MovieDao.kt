package com.jingying.movie.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jingying.movie.data.local.entity.MovieEntity

@Dao
interface MovieDao {

    @Query("SELECT * FROM movies ORDER BY cachedAt DESC LIMIT :limit")
    suspend fun getCachedMovies(limit: Int = 100): List<MovieEntity>

    @Query("SELECT * FROM movies WHERE vodId = :vodId LIMIT 1")
    suspend fun getMovieById(vodId: Int): MovieEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovies(movies: List<MovieEntity>)

    @Query("DELETE FROM movies WHERE cachedAt < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long)

    @Query("DELETE FROM movies")
    suspend fun clearAll()
}
