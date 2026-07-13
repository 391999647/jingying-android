package com.jingying.movie.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jingying.movie.data.local.entity.TypeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TypeDao {

    @Query("SELECT * FROM types ORDER BY typeId ASC")
    fun getTypesFlow(): Flow<List<TypeEntity>>

    @Query("SELECT * FROM types ORDER BY typeId ASC")
    suspend fun getTypes(): List<TypeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTypes(types: List<TypeEntity>)

    @Query("DELETE FROM types")
    suspend fun clearAll()
}
