package com.jingying.movie.domain.model

data class ResourceSite(
    val name: String,
    val apiUrl: String,
    val playFrom: String
)

object ResourceSites {
    val DEFAULT = ResourceSite(
        name = "默认",
        apiUrl = "",
        playFrom = ""
    )

    val JINYING = ResourceSite(
        name = "金鹰资源",
        apiUrl = "https://jyzyapi.com/provide/vod/from/jinyingm3u8/at/json",
        playFrom = "jinyingm3u8"
    )

    val WSYZY = ResourceSite(
        name = "无水印资源",
        apiUrl = "https://api.wsyzy.net/api.php/provide/vod/",
        playFrom = "wsym3u8"
    )

    val ALL = listOf(DEFAULT, JINYING, WSYZY)

    fun fromName(name: String?): ResourceSite {
        return ALL.find { it.name == name } ?: DEFAULT
    }

    fun fromPlayFrom(playFrom: String?): ResourceSite {
        return ALL.find { it.playFrom == playFrom } ?: DEFAULT
    }
}