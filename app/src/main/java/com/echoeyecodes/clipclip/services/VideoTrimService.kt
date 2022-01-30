package com.echoeyecodes.clipclip.services

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.arthenica.ffmpegkit.FFmpegKit
import com.echoeyecodes.clipclip.R
import com.echoeyecodes.clipclip.activities.VideoActivity
import com.echoeyecodes.clipclip.models.VideoConfigModel
import com.echoeyecodes.clipclip.receivers.VideoTrimBroadcastReceiver
import com.echoeyecodes.clipclip.trimmer.VideoTrimManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VideoTrimService : Service() {
    private val videoTrimManager by lazy { VideoTrimManager.getInstance(applicationContext) }

    companion object {
        const val NOTIFICATION_ID = 874
        const val NOTIFICATION_CHANNEL_ID = "TRIMMING_VIDEO"
    }

    override fun onCreate() {
        super.onCreate()
        val pendingIntent = Intent(applicationContext, VideoActivity::class.java).let {
            PendingIntent.getActivity(applicationContext, 0, it, 0)
        }
        startForeground(NOTIFICATION_ID, showNotification(pendingIntent))
    }

    override fun onDestroy() {
        videoTrimManager.shouldTerminate = true
        FFmpegKit.cancel()
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val configModel = intent.getSerializableExtra("videoConfig") as VideoConfigModel
        val videoUri = intent.getStringExtra("videoUri")!!

        CoroutineScope(Dispatchers.IO).launch {
            videoTrimManager.startTrim(videoUri, configModel)
            stopForeground(true)
        }
        return START_REDELIVER_INTENT
    }

    private fun showNotification(intent: PendingIntent): Notification {
        val broadcastPendingIntent =
            Intent(applicationContext, VideoTrimBroadcastReceiver::class.java).apply {
                action = VideoTrimBroadcastReceiver.TERMINATE_TRIM_REQUEST_CODE
            }
        val broadcastIntent =
            PendingIntent.getBroadcast(applicationContext, 0, broadcastPendingIntent, 0)
        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setContentTitle("Trimming Started")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(intent)
            .addAction(R.drawable.ic_baseline_cancel_24, "Cancel", broadcastIntent)
            .setContentText("Yupp! Doing a bit of work on your video")
        return builder.build()
    }

    override fun onBind(p0: Intent): IBinder? {
        return null
    }
}