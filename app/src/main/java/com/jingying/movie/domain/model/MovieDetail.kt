package com.jingying.movie.domain.model

data class MovieDetail(
    val id: Int,
    val resourceSite: String? = null,
    val resourceApi: String? = null,
    val vodId: Int,
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
    val createdAt: String? = null
) {
    val episodes: List<Episode>
        get() = if (vodPlayUrl.isBlank()) {
            emptyList()
        } else {
            vodPlayUrl.split("#").mapNotNull { part ->
                val seg = part.split("$")
                val name = if (seg.size > 1) seg[0].trim() else "正片"
                val url = if (seg.size > 1) seg[1].trim() else seg[0].trim()
                if (url.isNotBlank()) Episode(name, url) else null
            }
        }
}
