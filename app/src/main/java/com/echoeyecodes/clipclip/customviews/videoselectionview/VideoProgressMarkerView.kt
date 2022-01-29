package com.echoeyecodes.clipclip.customviews.videoselectionview

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.RelativeLayout

class VideoProgressMarkerView(context: Context, attr: AttributeSet) :
    RelativeLayout(context, attr) {

    init {
        setBackgroundColor(Color.WHITE)
    }

    fun selectMarkerPosition(value: Float) {
        x = value
    }
}