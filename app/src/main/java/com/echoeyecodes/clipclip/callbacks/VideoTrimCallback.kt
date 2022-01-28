package com.echoeyecodes.clipclip.callbacks

interface VideoTrimCallback {
    fun onTrimStarted(index: Int, total:Int)
    fun onTrimEnded()
}