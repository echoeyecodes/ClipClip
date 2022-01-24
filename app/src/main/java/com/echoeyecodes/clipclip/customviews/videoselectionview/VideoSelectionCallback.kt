package com.echoeyecodes.clipclip.customviews.videoselectionview

interface VideoSelectionCallback {
    fun onSelectionMoved(startX:Float, endX:Float)
    fun onSelectionStarted(gravity: VideoSelectionGravity, startX: Float, endX: Float)
    fun onSelectionEnded(gravity: VideoSelectionGravity, startX: Float, endX: Float)
}