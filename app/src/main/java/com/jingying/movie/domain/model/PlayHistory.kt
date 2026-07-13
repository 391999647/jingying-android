package com.jingying.movie.domain.model

data class PlayHistory(
    val id: Long = 0,
    val vodId: Int,
    val vodName: String,
    val vodPic: String,
    val episodeName: String,
    val episodeUrl: String,
    val position: Long,
    val duration: Long,
    val updatedAt: Long = System.currentTimeMillis()
)
