package com.echoeyecodes.clipclip.utils

import android.content.res.Resources


fun getScreenSize(): Pair<Int, Int> {
    return Pair(
        Resources.getSystem().displayMetrics.widthPixels,
        Resources.getSystem().displayMetrics.heightPixels
    )
}


enum class VideoQuality(val qName: String) {
    VERY_LOW("very_low"),
    LOW("low"),
    MEDIUM("medium"),
    HIGH("high"),
    NORMAL("normal"),
}

enum class VideoFormat(val extension: String) {
    MP4(".mp4"),
    MP3(".mp3")
}