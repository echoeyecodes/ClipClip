package com.echoeyecodes.clipclip.callbacks

import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerView

interface VideoPlayerCallback {
    fun onPlayerProgress(timestamp: Long)
    fun onIsPlaying(isPlaying: Boolean)
}