package com.echoeyecodes.clipclip.models

import com.echoeyecodes.clipclip.utils.VideoFormat
import com.echoeyecodes.clipclip.utils.VideoQuality
import java.io.Serializable

class VideoConfigModel(val splitTime:Int, val format:VideoFormat, val quality: VideoQuality) : Serializable