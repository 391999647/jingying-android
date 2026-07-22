package com.jingying.movie.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("/api.php")
    suspend fun getMovieList(
        @Query("action") action: String = "list",
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 18,
        @Query("type_id") typeId: Int? = null
    ): ApiResponse<List<MovieSummaryResponse>>

    @GET("/api.php")
    suspend fun getMovieDetail(
        @Query("action") action: String = "detail",
        @Query("vod_id") vodId: Int
    ): ApiResponse<MovieDetailResponse>

    @GET("/api.php")
    suspend fun searchMovies(
        @Query("action") action: String = "search",
        @Query("keyword") keyword: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 18
    ): ApiResponse<List<MovieSummaryResponse>>
}