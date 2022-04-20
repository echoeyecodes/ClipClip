package com.echoeyecodes.clipclip.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.echoeyecodes.clipclip.utils.VideoFormat
import com.echoeyecodes.clipclip.utils.VideoQuality

class VideoConfigurationViewModel(application: Application):AndroidViewModel(application) {
    var splitTime = 30000L
    var quality = VideoQuality.HIGH
    var format = VideoFormat.MP4
}