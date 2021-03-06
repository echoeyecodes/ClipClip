package com.echoeyecodes.clipclip.workmanager

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.echoeyecodes.clipclip.R
import com.echoeyecodes.clipclip.activities.VideoActivity
import com.echoeyecodes.clipclip.models.VideoBlurModel
import com.echoeyecodes.clipclip.models.VideoCanvasModel
import com.echoeyecodes.clipclip.models.VideoConfigModel
import com.echoeyecodes.clipclip.receivers.VideoTrimBroadcastReceiver
import com.echoeyecodes.clipclip.trimmer.VideoTrimManager
import com.echoeyecodes.clipclip.utils.Dimension
import com.echoeyecodes.clipclip.utils.VideoFormat
import com.echoeyecodes.clipclip.utils.toVideoEditConfigModel
import com.echoeyecodes.clipclip.utils.toVideoQuality
import kotlinx.coroutines.*
import java.lang.Exception

class VideoTrimWorkManager(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {
    private val videoTrimManager = VideoTrimManager.getInstance(context)

    companion object {
        const val NOTIFICATION_ID = 874
        const val NOTIFICATION_CHANNEL_ID = "TRIMMING_VIDEO"
        const val TAG = "VIDEO_TRIM"
    }

    private fun showNotification(intent: PendingIntent): Notification {
        val broadcastPendingIntent =
            Intent(applicationContext, VideoTrimBroadcastReceiver::class.java).apply {
                action = VideoTrimBroadcastReceiver.TERMINATE_TRIM_REQUEST_CODE
            }
        val broadcastIntent =
            PendingIntent.getBroadcast(applicationContext, 0, broadcastPendingIntent, 0)
        val builder =
            NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setContentTitle("Trimming Started")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(intent)
                .addAction(R.drawable.ic_baseline_cancel_24, "Cancel", broadcastIntent)
                .setContentText("Yupp! Doing a bit of work on your video")
        return builder.build()
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        val pendingIntent = Intent(applicationContext, VideoActivity::class.java).let {
            PendingIntent.getActivity(applicationContext, 0, it, 0)
        }
        return ForegroundInfo(NOTIFICATION_ID, showNotification(pendingIntent))
    }

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                val videoEditConfigModel = inputData.getString("config")!!.toVideoEditConfigModel()
                val canvasConfig = videoEditConfigModel.canvasConfig
                val trimConfig = videoEditConfigModel.trimConfig

                val videoUri = trimConfig.uri
                val startTime = trimConfig.startTime
                val endTime = trimConfig.endTime
                val splitTime = trimConfig.splitTime
                val _format = trimConfig.format
                val quality = trimConfig.quality.toVideoQuality()

                val format = if (_format == ".mp3") {
                    VideoFormat.MP3
                } else VideoFormat.MP4

                if (format == VideoFormat.MP3 || canvasConfig == null) {
                    videoTrimManager.startTrim(
                        videoUri,
                        VideoConfigModel(startTime, endTime, splitTime, format, quality),
                        null
                    )
                } else {
                    val targetWidth = canvasConfig.targetWidth
                    val targetHeight = canvasConfig.targetHeight
                    val videoWidth = canvasConfig.videoWidth
                    val videoHeight = canvasConfig.videoHeight
                    val blurFactor = canvasConfig.blurFactor

                    val blurModel = VideoBlurModel(
                        VideoCanvasModel(targetWidth, targetHeight),
                        Dimension(videoWidth, videoHeight),
                        blurFactor
                    )
                    videoTrimManager.startTrim(
                        videoUri,
                        VideoConfigModel(startTime, endTime, splitTime, format, quality),
                        blurModel
                    )
                }
                Result.success()
            } catch (exception: Exception) {
                Result.failure()
            }
        }
    }
}