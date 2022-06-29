package com.echoeyecodes.clipclip.models

class VideoTrimConfigModel(
    val uri: String,
    val startTime: Long,
    val endTime: Long,
    val splitTime: Long,
    val quality: String,
    val format: String
)