package com.jingying.movie.data.mapper

import com.jingying.movie.data.local.entity.DetailEntity
import com.jingying.movie.data.local.entity.HistoryEntity
import com.jingying.movie.data.local.entity.MovieEntity
import com.jingying.movie.data.local.entity.TypeEntity
import com.jingying.movie.data.remote.MovieDetailResponse
import com.jingying.movie.data.remote.MovieSummaryResponse
import com.jingying.movie.data.remote.MovieTypeResponse
import com.jingying.movie.data.repository.MovieRepository
import com.jingying.movie.domain.model.Movie
import com.jingying.movie.domain.model.MovieDetail
import com.jingying.movie.domain.model.MovieType
import com.jingying.movie.domain.model.PlayHistory
import javax.inject.Inject

fun MovieTypeResponse.toDomain(): MovieType = MovieType(
    typeId = typeId,
    typeName = typeName
)

fun MovieType.toEntity(): TypeEntity = TypeEntity(
    typeId = typeId,
    typeName = typeName
)

fun TypeEntity.toDomain(): MovieType = MovieType(
    typeId = typeId,
    typeName = typeName
)

fun MovieSummaryResponse.toDomain(): Movie = Movie(
    vodId = vodId,
    vodName = vodName,
    typeName = typeName,
    vodPic = vodPic,
    vodRemarks = vodRemarks,
    vodDirector = vodDirector,
    vodActor = vodActor,
    vodArea = vodArea,
    vodYear = vodYear,
    vodScore = vodScore,
    vodTime = vodTime
)

fun Movie.toEntity(): MovieEntity = MovieEntity(
    vodId = vodId,
    vodName = vodName,
    typeName = typeName,
    vodPic = vodPic,
    vodRemarks = vodRemarks,
    vodDirector = vodDirector,
    vodActor = vodActor,
    vodArea = vodArea,
    vodYear = vodYear,
    vodScore = vodScore,
    vodTime = vodTime
)

fun MovieEntity.toDomain(): Movie = Movie(
    vodId = vodId,
    vodName = vodName,
    typeName = typeName,
    vodPic = vodPic,
    vodRemarks = vodRemarks,
    vodDirector = vodDirector,
    vodActor = vodActor,
    vodArea = vodArea,
    vodYear = vodYear,
    vodScore = vodScore,
    vodTime = vodTime
)

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

fun MovieDetailResponse.toDomain(): MovieDetail {
    return MovieDetail(
        id = id,
        resourceSite = resourceSite,
        resourceApi = resourceApi,
        vodId = vodId,
        vodName = stripHtml(vodName),
        typeId = typeId,
        typeName = stripHtml(typeName),
        vodEn = vodEn,
        vodPic = vodPic,
        vodRemarks = stripHtml(vodRemarks),
        vodPlayFrom = vodPlayFrom,
        vodPlayUrl = vodPlayUrl,
        vodTime = vodTime,
        vodDirector = stripHtml(vodDirector),
        vodActor = stripHtml(vodActor),
        vodArea = stripHtml(vodArea),
        vodYear = stripHtml(vodYear),
        vodContent = stripHtml(vodContent),
        vodScore = stripHtml(vodScore),
        createdAt = createdAt
    )
}

fun MovieDetail.toEntity(): DetailEntity = DetailEntity(
    vodId = vodId,
    id = id,
    resourceSite = resourceSite,
    resourceApi = resourceApi,
    vodName = vodName,
    typeId = typeId,
    typeName = typeName,
    vodEn = vodEn,
    vodPic = vodPic,
    vodRemarks = vodRemarks,
    vodPlayFrom = vodPlayFrom,
    vodPlayUrl = vodPlayUrl,
    vodTime = vodTime,
    vodDirector = vodDirector,
    vodActor = vodActor,
    vodArea = vodArea,
    vodYear = vodYear,
    vodContent = vodContent,
    vodScore = vodScore,
    createdAt = createdAt
)

fun DetailEntity.toDomain(): MovieDetail = MovieDetail(
    id = id,
    resourceSite = resourceSite,
    resourceApi = resourceApi,
    vodId = vodId,
    vodName = vodName,
    typeId = typeId,
    typeName = typeName,
    vodEn = vodEn,
    vodPic = vodPic,
    vodRemarks = vodRemarks,
    vodPlayFrom = vodPlayFrom,
    vodPlayUrl = vodPlayUrl,
    vodTime = vodTime,
    vodDirector = vodDirector,
    vodActor = vodActor,
    vodArea = vodArea,
    vodYear = vodYear,
    vodContent = vodContent,
    vodScore = vodScore,
    createdAt = createdAt
)

fun HistoryEntity.toDomain(): PlayHistory = PlayHistory(
    id = id,
    vodId = vodId,
    vodName = vodName,
    vodPic = vodPic,
    episodeName = episodeName,
    episodeUrl = episodeUrl,
    position = position,
    duration = duration,
    updatedAt = updatedAt
)

fun PlayHistory.toEntity(): HistoryEntity = HistoryEntity(
    id = id,
    vodId = vodId,
    vodName = vodName,
    vodPic = vodPic,
    episodeName = episodeName,
    episodeUrl = episodeUrl,
    position = position,
    duration = duration,
    updatedAt = updatedAt
)