package com.jingying.movie.data.remote

import com.google.gson.annotations.SerializedName

data class MovieDetailResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("resource_site") val resourceSite: String? = null,
    @SerializedName("resource_api") val resourceApi: String? = null,
    @SerializedName("vod_id") val vodId: Int,
    @SerializedName("vod_name") val vodName: String,
    @SerializedName("type_id") val typeId: Int,
    @SerializedName("type_name") val typeName: String,
    @SerializedName("vod_en") val vodEn: String? = null,
    @SerializedName("vod_pic") val vodPic: String,
    @SerializedName("vod_remarks") val vodRemarks: String,
    @SerializedName("vod_play_from") val vodPlayFrom: String? = null,
    @SerializedName("vod_play_url") val vodPlayUrl: String,
    @SerializedName("vod_time") val vodTime: String? = null,
    @SerializedName("vod_director") val vodDirector: String? = null,
    @SerializedName("vod_actor") val vodActor: String? = null,
    @SerializedName("vod_area") val vodArea: String? = null,
    @SerializedName("vod_year") val vodYear: String? = null,
    @SerializedName("vod_content") val vodContent: String,
    @SerializedName("vod_score") val vodScore: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)
