package com.jingying.movie.data.remote

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import com.google.gson.GsonBuilder
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "https://uuuu.111u.icu"
    private const val CACHE_SIZE = 50 * 1024 * 1024L
    private const val CACHE_MAX_AGE_SECONDS = 60 * 5L
    private const val CACHE_MAX_STALE_SECONDS = 60 * 60 * 24 * 7L

    fun create(context: Context): ApiService {
        val cacheDir = File(context.cacheDir, "http_cache").apply { mkdirs() }
        val cache = Cache(cacheDir, CACHE_SIZE)

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val offlineCacheInterceptor: (okhttp3.Interceptor.Chain) -> okhttp3.Response = { chain ->
            var request = chain.request()
            if (!isNetworkAvailable(context)) {
                val cacheControl = CacheControl.Builder()
                    .onlyIfCached()
                    .maxStale(CACHE_MAX_STALE_SECONDS.toInt(), TimeUnit.SECONDS)
                    .build()
                request = request.newBuilder()
                    .cacheControl(cacheControl)
                    .build()
            }
            chain.proceed(request)
        }

        val client = OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor(logging)
            .addInterceptor(offlineCacheInterceptor)
            .addNetworkInterceptor { chain ->
                val request = chain.request()
                val response = chain.proceed(request)
                response.newBuilder()
                    .header("Cache-Control", "public, max-age=$CACHE_MAX_AGE_SECONDS")
                    .build()
            }
            .addInterceptor { chain ->
                val request = chain.request()
                val response = chain.proceed(request)
                if (response.code == 504) {
                    val cacheControl = okhttp3.CacheControl.Builder()
                        .onlyIfCached()
                        .maxStale(CACHE_MAX_STALE_SECONDS.toInt(), TimeUnit.SECONDS)
                        .build()
                    chain.proceed(request.newBuilder().cacheControl(cacheControl).build())
                } else {
                    response
                }
            }
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()

        val gson = GsonBuilder()
            .setLenient()
            .create()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            connectivityManager.getNetworkCapabilities(network)
                ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        } else {
            @Suppress("DEPRECATION")
            connectivityManager.activeNetworkInfo?.isConnected == true
        }
    }
}
