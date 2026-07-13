package com.jingying.movie.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jingying.movie.data.local.dao.DetailDao
import com.jingying.movie.data.local.dao.HistoryDao
import com.jingying.movie.data.local.dao.MovieDao
import com.jingying.movie.data.local.dao.TypeDao
import com.jingying.movie.data.local.entity.DetailEntity
import com.jingying.movie.data.local.entity.HistoryEntity
import com.jingying.movie.data.local.entity.MovieEntity
import com.jingying.movie.data.local.entity.TypeEntity

@Database(
    entities = [
        MovieEntity::class,
        TypeEntity::class,
        HistoryEntity::class,
        DetailEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao
    abstract fun typeDao(): TypeDao
    abstract fun historyDao(): HistoryDao
    abstract fun detailDao(): DetailDao
}
