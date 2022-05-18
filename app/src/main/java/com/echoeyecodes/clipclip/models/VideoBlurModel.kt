package com.echoeyecodes.clipclip.models

import com.echoeyecodes.clipclip.utils.Dimension

class VideoBlurModel(
    val videoCanvasModel: VideoCanvasModel,
    val dimension: Dimension,
    val blurFactor: Int
)