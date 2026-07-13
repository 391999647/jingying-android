package com.jingying.movie.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "details")
data class DetailEntity(
    @PrimaryKey val vodId: Int,
    val id: Int,
    val resourceSite: String? = null,
    val resourceApi: String? = null,
    val vodName: String,
    val typeId: Int,
    val typeName: String,
    val vodEn: String? = null,
    val vodPic: String,
    val vodRemarks: String,
    val vodPlayFrom: String? = null,
    val vodPlayUrl: String,
    val vodTime: String? = null,
    val vodDirector: String? = null,
    val vodActor: String? = null,
    val vodArea: String? = null,
    val vodYear: String? = null,
    val vodContent: String,
    val vodScore: String? = null,
    val createdAt: String? = null,
    val cachedAt: Long = System.currentTimeMillis()
)
