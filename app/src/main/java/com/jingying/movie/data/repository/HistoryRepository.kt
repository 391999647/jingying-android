package com.jingying.movie.data.repository

import com.jingying.movie.data.local.AppDatabase
import com.jingying.movie.data.mapper.toDomain
import com.jingying.movie.data.mapper.toEntity
import com.jingying.movie.domain.model.PlayHistory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoryRepository @Inject constructor(
    private val database: AppDatabase
) {

    fun getHistoryFlow(): Flow<List<PlayHistory>> {
        return database.historyDao().getHistoryFlow().map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun getHistoryByEpisode(vodId: Int, episodeUrl: String): PlayHistory? = withContext(Dispatchers.IO) {
        database.historyDao().getHistoryByEpisode(vodId, episodeUrl)?.toDomain()
    }

    suspend fun saveHistory(history: PlayHistory) = withContext(Dispatchers.IO) {
        val existing = database.historyDao().getHistoryByEpisode(history.vodId, history.episodeUrl)
        database.historyDao().insertHistory(
            history.toEntity().copy(id = existing?.id ?: 0L)
        )
    }

    suspend fun deleteHistory(vodId: Int, episodeUrl: String) = withContext(Dispatchers.IO) {
        database.historyDao().deleteHistory(vodId, episodeUrl)
    }

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        database.historyDao().clearAll()
    }
}
