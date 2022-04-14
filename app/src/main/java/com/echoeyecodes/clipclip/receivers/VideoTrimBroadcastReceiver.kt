package com.echoeyecodes.clipclip.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.arthenica.ffmpegkit.FFmpegKit

class VideoTrimBroadcastReceiver : BroadcastReceiver() {

    companion object {
        const val TERMINATE_TRIM_REQUEST_CODE = "TERMINATE_TRIM_REQUEST_CODE"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == TERMINATE_TRIM_REQUEST_CODE) {
            FFmpegKit.cancel()
        }
    }
}