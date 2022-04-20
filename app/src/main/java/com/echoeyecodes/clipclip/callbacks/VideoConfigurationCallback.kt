package com.echoeyecodes.clipclip.callbacks

import com.echoeyecodes.clipclip.utils.VideoFormat
import com.echoeyecodes.clipclip.utils.VideoQuality

interface VideoConfigurationCallback {
    fun onFinish(splitTime: Int, quality: VideoQuality, format:VideoFormat)
}