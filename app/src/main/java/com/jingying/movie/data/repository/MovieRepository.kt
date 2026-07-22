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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import com.jingying.movie.util.AppLogger

@Singleton
class MovieRepository @Inject constructor(
    private val apiService: ApiService,
    private val database: AppDatabase
) {

    companion object {
        private const val TAG = "MovieRepository"
    }

    suspend fun getTypes(forceRefresh: Boolean = false): Resource<List<MovieType>> = withContext(Dispatchers.IO) {
        try {
            if (!forceRefresh) {
                val cached = database.typeDao().getTypes()
                if (cached.isNotEmpty()) {
                    AppLogger.d(TAG, "getTypes: 从缓存加载 ${cached.size} 个分类")
                    return@withContext Resource.Success(cached.map { it.toDomain() })
                }
            }
            AppLogger.i(TAG, "getTypes: 请求API...")
            val response = apiService.getTypes()
            AppLogger.i(TAG, "getTypes: API返回 ${response.data?.size ?: 0} 个分类")
            val types = response.requireData().map { it.toDomain() }
            database.typeDao().insertTypes(types.map { it.toEntity() })
            Resource.Success(types)
        } catch (e: Exception) {
            AppLogger.e(TAG, "getTypes 失败: ${e.message}", e)
            val cached = database.typeDao().getTypes()
            if (cached.isNotEmpty()) {
                Resource.Success(cached.map { it.toDomain() })
            } else {
                Resource.Error(e.message ?: "加载分类失败")
            }
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
        resourceSite: String? = null,
        forceRefresh: Boolean = false
    ): Resource<MovieDetail> = withContext(Dispatchers.IO) {
        try {
            if (!forceRefresh && resourceSite == null) {
                val cached = database.detailDao().getDetailById(vodId)
                if (cached != null) {
                    return@withContext Resource.Success(cached.toDomain())
                }
            }
            val response = apiService.getMovieDetail(vodId = vodId, resourceSite = resourceSite)
            val detail = response.requireData().toDomain()
            if (resourceSite == null) {
                database.detailDao().insertDetail(detail.toEntity())
            }
            Resource.Success(detail)
        } catch (e: Exception) {
            if (resourceSite == null) {
                val cached = database.detailDao().getDetailById(vodId)
                if (cached != null) {
                    Resource.Success(cached.toDomain())
                } else {
                    Resource.Error(e.message ?: "加载详情失败")
                }
            } else {
                Resource.Error(e.message ?: "加载资源失败")
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
            emit(Resource.Error(e.message ?: "搜索失败"))
        }
    }.flowOn(Dispatchers.IO)

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
