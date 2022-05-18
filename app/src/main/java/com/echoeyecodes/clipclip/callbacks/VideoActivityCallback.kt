package com.echoeyecodes.clipclip.callbacks

import com.echoeyecodes.clipclip.models.VideoCanvasModel
import com.google.android.exoplayer2.Player

interface VideoActivityCallback {
    fun getPlayer(): Player?
    fun restorePlayerView()
    fun registerVideoActivityCallback(videoPlayerCallback: VideoPlayerCallback)
    fun removeVideoActivityCallback(videoPlayerCallback: VideoPlayerCallback)
    fun closeFragment()
    fun setVideoFrameProperties(videoCanvasModel: VideoCanvasModel, blurFactor: Int)
    fun onBlurSeekStarted()
    fun onBlurSeekEnded()
    fun playVideo()
    fun pauseVideo()
    fun togglePlayState()
}