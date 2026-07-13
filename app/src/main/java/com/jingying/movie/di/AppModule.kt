package com.jingying.movie.di

import android.content.Context
import androidx.room.Room
import com.jingying.movie.data.local.AppDatabase
import com.jingying.movie.data.remote.ApiService
import com.jingying.movie.data.remote.RetrofitClient
import com.jingying.movie.data.repository.HistoryRepository
import com.jingying.movie.data.repository.MovieRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideApiService(@ApplicationContext context: Context): ApiService {
        return RetrofitClient.create(context)
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "jingying.db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideMovieRepository(
        apiService: ApiService,
        database: AppDatabase
    ): MovieRepository {
        return MovieRepository(apiService, database)
    }

    @Provides
    @Singleton
    fun provideHistoryRepository(
        database: AppDatabase
    ): HistoryRepository {
        return HistoryRepository(database)
    }
}
