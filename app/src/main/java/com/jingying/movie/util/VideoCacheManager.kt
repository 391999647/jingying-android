package com.jingying.movie.util

import android.content.Context
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import java.io.File

/**
 * 视频分片磁盘缓存单例
 * 管理 HLS/MP4 分片的 LRU 缓存，上限 200MB，避免重复下载。
 */
object VideoCacheManager {

    private const val MAX_CACHE_SIZE = 200 * 1024 * 1024L // 200MB
    private const val CACHE_DIR = "video_cache"

    @Volatile
    private var cache: SimpleCache? = null

    @Synchronized
    fun getCache(context: Context): SimpleCache {
        return cache ?: run {
            val cacheDir = File(context.cacheDir, CACHE_DIR).apply { mkdirs() }
            val evictor = LeastRecentlyUsedCacheEvictor(MAX_CACHE_SIZE)
            val databaseProvider = StandaloneDatabaseProvider(context)
            SimpleCache(cacheDir, evictor, databaseProvider).also { cache = it }
        }
    }

    /** 释放缓存实例（在 Application 销毁时调用） */
    fun release() {
        cache?.release()
        cache = null
    }
}