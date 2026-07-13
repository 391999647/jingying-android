package com.jingying.movie.data.remote

import com.google.gson.annotations.SerializedName

data class MovieTypeResponse(
    @SerializedName("type_id") val typeId: Int,
    @SerializedName("type_name") val typeName: String
)
