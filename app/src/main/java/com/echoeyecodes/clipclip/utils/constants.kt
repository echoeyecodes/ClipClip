package com.echoeyecodes.clipclip.utils

enum class VideoQuality(name: String) {
    LOW("low"),
    MEDIUM("medium"),
    HIGH("high")
}

enum class VideoFormat(val extension: String) {
    MP4(".mp4"),
    MP3(".mp3")
}