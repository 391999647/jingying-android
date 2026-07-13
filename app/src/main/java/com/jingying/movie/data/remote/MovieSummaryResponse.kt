package com.jingying.movie.data.remote

import com.google.gson.annotations.SerializedName

data class MovieSummaryResponse(
    @SerializedName("vod_id") val vodId: Int,
    @SerializedName("vod_name") val vodName: String,
    @SerializedName("type_name") val typeName: String,
    @SerializedName("vod_pic") val vodPic: String,
    @SerializedName("vod_remarks") val vodRemarks: String,
    @SerializedName("vod_director") val vodDirector: String? = null,
    @SerializedName("vod_actor") val vodActor: String? = null,
    @SerializedName("vod_area") val vodArea: String? = null,
    @SerializedName("vod_year") val vodYear: String? = null,
    @SerializedName("vod_score") val vodScore: String? = null,
    @SerializedName("vod_time") val vodTime: String? = null
)
