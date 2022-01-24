package com.echoeyecodes.clipclip.model

import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore

data class VideoModel(val id: Long, val title: String, val path: String, val duration: Long) {

    fun getVideoUri(): Uri {
        return ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
    }
}