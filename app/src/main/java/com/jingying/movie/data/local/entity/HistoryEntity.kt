package com.jingying.movie.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "history",
    indices = [Index(value = ["vodId", "episodeUrl"], unique = true)]
)
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vodId: Int,
    val vodName: String,
    val vodPic: String,
    val episodeName: String,
    val episodeUrl: String,
    val position: Long,
    val duration: Long,
    val updatedAt: Long = System.currentTimeMillis()
)
