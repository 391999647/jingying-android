package com.jingying.movie.domain.model

data class Movie(
    val vodId: Int,
    val vodName: String,
    val typeName: String,
    val vodPic: String,
    val vodRemarks: String,
    val vodDirector: String? = null,
    val vodActor: String? = null,
    val vodArea: String? = null,
    val vodYear: String? = null,
    val vodScore: String? = null,
    val vodTime: String? = null
)
