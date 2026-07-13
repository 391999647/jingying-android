package com.jingying.movie.domain.model

data class Pagination(
    val page: Int,
    val limit: Int,
    val total: Int,
    val pages: Int
)
