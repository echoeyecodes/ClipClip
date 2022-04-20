package com.echoeyecodes.clipclip.models

import com.echoeyecodes.clipclip.utils.VideoFormat
import com.echoeyecodes.clipclip.utils.VideoQuality
import java.io.Serializable
import kotlin.math.ceil

class VideoConfigModel(
    val startTime: Long,
    val endTime: Long,
    val splitTime: Int,
    val format: VideoFormat,
    val quality: VideoQuality
) : Serializable {

    fun getSplitCount(): Int {
        val timeDifference = (endTime - startTime).toFloat() / 1000 / splitTime
        return ceil(timeDifference).toInt()
    }
}