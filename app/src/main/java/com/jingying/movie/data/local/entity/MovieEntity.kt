package com.jingying.movie.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movies")
data class MovieEntity(
    @PrimaryKey val vodId: Int,
    val vodName: String,
    val typeName: String,
    val vodPic: String,
    val vodRemarks: String,
    val vodDirector: String? = null,
    val vodActor: String? = null,
    val vodArea: String? = null,
    val vodYear: String? = null,
    val vodScore: String? = null,
    val vodTime: String? = null,
    val cachedAt: Long = System.currentTimeMillis()
)
