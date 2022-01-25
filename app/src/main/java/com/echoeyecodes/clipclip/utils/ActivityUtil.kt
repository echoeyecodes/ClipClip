package com.echoeyecodes.clipclip.utils

import android.content.Context
import android.content.Intent
import com.echoeyecodes.clipclip.activities.VideoActivity
import com.echoeyecodes.clipclip.activities.VideoSplitActivity

class ActivityUtil {

    companion object {
        fun startVideoActivity(context: Context, uri: String, duration: Long) {
            context.startActivity(Intent(context, VideoActivity::class.java).apply {
                putExtra("uri", uri)
                putExtra("duration", duration)
            })
        }

        fun startVideoSplitActivity(
            context: Context,
            videoUri: String,
            startTime: Long,
            endTime: Long
        ) {
            context.startActivity(Intent(context, VideoSplitActivity::class.java).apply {
                putExtra("videoUri", videoUri)
                putExtra("startTime", startTime)
                putExtra("endTime", endTime)
            })
        }
    }
}