package com.echoeyecodes.clipclip.callbacks

import com.echoeyecodes.clipclip.model.VideoModel

interface VideoAdapterCallback {
    fun onVideoSelected(model: VideoModel)
}