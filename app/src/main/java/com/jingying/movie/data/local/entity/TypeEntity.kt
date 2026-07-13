package com.jingying.movie.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "types")
data class TypeEntity(
    @PrimaryKey val typeId: Int,
    val typeName: String,
    val cachedAt: Long = System.currentTimeMillis()
)
