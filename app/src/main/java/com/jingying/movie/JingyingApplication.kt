package com.jingying.movie

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import dagger.hilt.android.HiltAndroidApp
import com.jingying.movie.util.AppLogger

@HiltAndroidApp
class JingyingApplication : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()
        AppLogger.init(this)
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .crossfade(true)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(256 * 1024 * 1024L)
                    .build()
            }
            .build()
    }
}
