package com.echoeyecodes.clipclip.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.echoeyecodes.clipclip.models.VideoConfigModel
import com.echoeyecodes.clipclip.trimmer.VideoTrimManager
import com.echoeyecodes.clipclip.utils.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VideoTrimService : Service() {
    private val videoTrimManager by lazy { VideoTrimManager.getInstance(applicationContext) }

    override fun onDestroy() {
        super.onDestroy()
        AndroidUtilities.log("Service stopped")
    }


    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val configModel = intent.getSerializableExtra("videoConfig") as VideoConfigModel
        val videoUri = intent.getStringExtra("videoUri")!!

        CoroutineScope(Dispatchers.IO).launch {
            videoTrimManager.startTrim(videoUri, configModel)
            stopSelf()
        }
        return START_REDELIVER_INTENT
    }

    override fun onBind(p0: Intent): IBinder? {
        return null
    }
}