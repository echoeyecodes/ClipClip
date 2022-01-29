package com.echoeyecodes.clipclip.utils

import android.content.Context
import android.content.Intent
import com.echoeyecodes.clipclip.activities.SelectVideoActivity
import com.echoeyecodes.clipclip.activities.VideoActivity

class ActivityUtil {

    companion object {
        fun startVideoActivity(context: Context, uri: String, duration: Long) {
            context.startActivity(Intent(context, VideoActivity::class.java).apply {
                putExtra("uri", uri)
                putExtra("duration", duration)
            })
        }
        fun startSelectVideoActivity(context: Context) {
            context.startActivity(Intent(context, SelectVideoActivity::class.java))
        }
    }
}