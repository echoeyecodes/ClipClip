package com.echoeyecodes.clipclip.models

import android.net.Uri

data class VideoModel(val id: Long, val title: String, val path: String, val duration: Long) {
    var videoUri: Uri? = null
}