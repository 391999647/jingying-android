package com.jingying.movie.data.repository

import com.jingying.movie.data.local.AppDatabase
import com.jingying.movie.data.mapper.toDomain
import com.jingying.movie.data.mapper.toEntity
import com.jingying.movie.data.remote.ApiResponse
import com.jingying.movie.data.remote.ApiService
import com.jingying.movie.data.remote.PaginationResponse
import com.jingying.movie.domain.model.Movie
import com.jingying.movie.domain.model.MovieDetail
import com.jingying.movie.domain.model.MovieType
import com.jingying.movie.domain.model.Pagination
import com.jingying.movie.domain.model.Resource
import com.jingying.movie.util.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MovieRepository @Inject constructor(
    private val apiService: ApiService,
    private val database: AppDatabase
) {

    companion object {
        private const val TAG = "MovieRepository"

        /** 内置修正分类映射 — 不调用 action=types 接口 */
        val CORRECT_TYPE_MAP: Map<Int, String> = linkedMapOf(
            0 to "全部",
            3 to "欧美剧",
            4 to "香港剧",
            5 to "韩剧",
            6 to "动作片",
            7 to "喜剧片",
            8 to "爱情片",
            9 to "科幻片",
            10 to "恐怖片",
            11 to "剧情片",
            12 to "战争片",
            13 to "国产剧",
            14 to "欧美剧",
            15 to "韩剧",
            16 to "日剧",
            20 to "纪录片",
            23 to "海外剧",
            24 to "中国动漫",
            25 to "大陆综艺",
            26 to "日韩综艺",
            27 to "港台综艺",
            28 to "欧美综艺",
            30 to "日韩动漫",
            31 to "欧美动漫",
            32 to "足球",
            33 to "篮球",
            34 to "台球",
            35 to "其他赛事"
        )
    }

    /** 从内置映射获取分类，不调用 API */
    fun getTypes(): List<MovieType> {
        return CORRECT_TYPE_MAP.entries.map { (id, name) ->
            MovieType(typeId = id, typeName = name)
        }
    }

    suspend fun getMovieList(
        page: Int = 1,
        limit: Int = 18,
        typeId: Int? = null,
        forceRefresh: Boolean = false
    ): Resource<Pair<List<Movie>, Pagination>> = withContext(Dispatchers.IO) {
        try {
            if (!forceRefresh && page == 1 && typeId == null) {
                val cached = database.movieDao().getCachedMovies(limit)
                if (cached.isNotEmpty()) {
                    val pagination = Pagination(page, limit, cached.size, 1)
                    return@withContext Resource.Success(cached.map { it.toDomain() } to pagination)
                }
            }
            AppLogger.i(TAG, "getMovieList: 请求API page=$page typeId=$typeId")
            val response = apiService.getMovieList(page = page, limit = limit, typeId = typeId)
            AppLogger.i(TAG, "getMovieList: API返回 ${response.data?.size ?: 0} 部影片")
            val list = response.requireData().map { it.toDomain() }
            val pagination = response.pagination?.toDomain() ?: Pagination(page, limit, list.size, 1)
            if (page == 1 && typeId == null) {
                database.movieDao().insertMovies(list.map { it.toEntity() })
            }
            Resource.Success(list to pagination)
        } catch (e: Exception) {
            AppLogger.e(TAG, "getMovieList 失败: ${e.message}", e)
            if (page == 1 && typeId == null) {
                val cached = database.movieDao().getCachedMovies(limit)
                if (cached.isNotEmpty()) {
                    Resource.Success(cached.map { it.toDomain() } to Pagination(page, limit, cached.size, 1))
                } else {
                    Resource.Error(e.message ?: "加载影片失败")
                }
            } else {
                Resource.Error(e.message ?: "加载影片失败")
            }
        }
    }

    suspend fun getMovieDetail(
        vodId: Int,
        forceRefresh: Boolean = false
    ): Resource<MovieDetail> = withContext(Dispatchers.IO) {
        try {
            if (!forceRefresh) {
                val cached = database.detailDao().getDetailById(vodId)
                if (cached != null) {
                    return@withContext Resource.Success(cached.toDomain())
                }
            }
            val response = apiService.getMovieDetail(vodId = vodId)
            val detail = response.requireData().toDomain()
            database.detailDao().insertDetail(detail.toEntity())
            Resource.Success(detail)
        } catch (e: Exception) {
            AppLogger.e(TAG, "getMovieDetail 失败: ${e.message}", e)
            val cached = database.detailDao().getDetailById(vodId)
            if (cached != null) {
                Resource.Success(cached.toDomain())
            } else {
                Resource.Error(e.message ?: "加载详情失败")
            }
        }
    }

    fun searchMovies(
        keyword: String,
        page: Int = 1,
        limit: Int = 18
    ): Flow<Resource<Pair<List<Movie>, Pagination>>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.searchMovies(keyword = keyword, page = page, limit = limit)
            val list = response.requireData().map { it.toDomain() }
            val pagination = response.pagination?.toDomain() ?: Pagination(page, limit, list.size, 1)
            emit(Resource.Success(list to pagination))
        } catch (e: Exception) {
            AppLogger.e(TAG, "searchMovies 失败: ${e.message}", e)
            emit(Resource.Error(e.message ?: "搜索失败"))
        }
    }.flowOn(Dispatchers.IO)

    /** 清洗 HTML 实体（与 1.md 的 _stripHtml 一致） */
    fun stripHtml(input: String?): String {
        if (input == null) return ""
        return input
            .replace(Regex("<[^>]+>"), " ")
            .replace(Regex("&(?:nbsp|#160);", RegexOption.IGNORE_CASE), " ")
            .replace(Regex("&(?:amp|#38);", RegexOption.IGNORE_CASE), "&")
            .replace(Regex("&(?:lt|#60);", RegexOption.IGNORE_CASE), "<")
            .replace(Regex("&(?:gt|#62);", RegexOption.IGNORE_CASE), ">")
            .replace(Regex("&(?:quot|#34);", RegexOption.IGNORE_CASE), "\"")
            .replace(Regex("&(?:apos|#39);", RegexOption.IGNORE_CASE), "'")
            .replace(Regex("&[a-zA-Z0-9#]+;"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun PaginationResponse.toDomain(): Pagination = Pagination(
        page = page,
        limit = limit,
        total = total,
        pages = pages
    )

    private fun <T> ApiResponse<T>.requireData(): T {
        if (code != 0) throw Exception(msg ?: "API error")
        return data ?: throw Exception("Empty data")
    }
}