package com.echoeyecodes.clipclip.models

import java.io.Serializable

data class VideoCanvasModel(val width: Float, val height: Float) : Serializable {
    var isSelected = false
}