package com.echoeyecodes.clipclip.callbacks

import com.echoeyecodes.clipclip.models.VideoModel

interface VideoAdapterCallback {
    fun onVideoSelected(model: VideoModel)
}