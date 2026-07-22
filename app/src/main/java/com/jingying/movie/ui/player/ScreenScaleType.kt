package com.jingying.movie.ui.player

import androidx.media3.ui.AspectRatioFrameLayout

enum class ScreenScaleType(val value: Int, val description: String) {
    FIT(AspectRatioFrameLayout.RESIZE_MODE_FIT, "自适应"),
    FILL(AspectRatioFrameLayout.RESIZE_MODE_FILL, "拉伸"),
    ZOOM(AspectRatioFrameLayout.RESIZE_MODE_ZOOM, "裁剪")
}
