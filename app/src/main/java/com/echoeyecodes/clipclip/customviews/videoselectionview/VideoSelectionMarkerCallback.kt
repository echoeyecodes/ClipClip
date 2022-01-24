package com.echoeyecodes.clipclip.customviews.videoselectionview

interface VideoSelectionMarkerCallback {
    fun onMarkerMove(gravity: VideoSelectionGravity, valueX:Float)
    fun onMarkerSelected(gravity: VideoSelectionGravity)
    fun onMarkerReleased(gravity: VideoSelectionGravity)
}