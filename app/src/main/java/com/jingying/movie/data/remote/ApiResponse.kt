package com.jingying.movie.data.remote

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    @SerializedName("code") val code: Int,
    @SerializedName("msg") val msg: String? = null,
    @SerializedName("data") val data: T? = null,
    @SerializedName("pagination") val pagination: PaginationResponse? = null
)

data class PaginationResponse(
    @SerializedName("page") val page: Int,
    @SerializedName("limit") val limit: Int,
    @SerializedName("total") val total: Int,
    @SerializedName("pages") val pages: Int
)
