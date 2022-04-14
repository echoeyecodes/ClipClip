package com.echoeyecodes.clipclip.customviews.videoselectionview

interface VideoSelectionCallback {
    fun onSelectionMoved(startX:Float, endX:Float)
    fun onSelectionStarted(startX: Float, endX: Float)
    fun onSelectionEnded(startX: Float, endX: Float)
}