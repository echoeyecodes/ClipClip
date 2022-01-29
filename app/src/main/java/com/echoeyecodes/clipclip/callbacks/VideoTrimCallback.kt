package com.echoeyecodes.clipclip.callbacks

import android.net.Uri

interface VideoTrimCallback {
    fun onTrimStarted(index: Int, total:Int)
    fun onTrimEnded(uris: List<Uri>)
}