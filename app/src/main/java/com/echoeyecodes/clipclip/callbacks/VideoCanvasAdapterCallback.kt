package com.echoeyecodes.clipclip.callbacks

import com.echoeyecodes.clipclip.models.VideoCanvasModel

interface VideoCanvasAdapterCallback {

    fun onCanvasItemSelected(model: VideoCanvasModel)
}