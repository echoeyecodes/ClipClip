package com.echoeyecodes.clipclip.trimmer

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.net.toUri
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegKitConfig
import com.arthenica.ffmpegkit.ReturnCode
import com.echoeyecodes.clipclip.R
import com.echoeyecodes.clipclip.callbacks.VideoTrimCallback
import com.echoeyecodes.clipclip.models.VideoConfigModel
import com.echoeyecodes.clipclip.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class VideoTrimManager(private val context: Context) {
    private val callbacks = ArrayList<VideoTrimCallback>()
    var shouldTerminate = false

    fun addTrimCallback(callback: VideoTrimCallback) {
        this.callbacks.add(callback)
    }

    companion object {
        private var instance: VideoTrimManager? = null
        fun getInstance(context: Context) = instance ?: synchronized(this) {
            val newInstance = VideoTrimManager(context)
            AndroidUtilities.log((instance == null).toString())
            instance = newInstance
            return newInstance
        }
    }

    private fun resetTerminate() {
        shouldTerminate = false
    }

    suspend fun startTrim(videoUri: String, configModel: VideoConfigModel) {
        resetTerminate()
        val count = configModel.getSplitCount()
        val format = if (configModel.format == VideoFormat.MP3) {
            " -q:a 0 -map a "
        } else {
            val sizeValue = when (configModel.quality) {
                VideoQuality.LOW -> {
                    38
                }
                VideoQuality.MEDIUM -> {
                    28
                }
                else -> {
                    18
                }
            }
            "-vcodec libx264 -crf $sizeValue"
        }
        val uris = ArrayList<Uri>()
        for (i in 0 until count) {
            if (shouldTerminate) {
                break
            }
            callbacks.forEach { it.onTrimStarted(i + 1, count) }
            val start = (configModel.startTime.toSeconds() + (i * configModel.splitTime))

            val splitTime = if ((configModel.endTime.toSeconds() - start) < configModel.splitTime) {
                configModel.endTime.toSeconds() - start
            } else {
                configModel.splitTime
            }

            if (configModel.splitTime + start < configModel.endTime) {
                val commandString = format
                if (start >= configModel.endTime.toSeconds()) {
                    break
                }
                createFile(
                    context.getString(R.string.app_name).plus(i),
                    configModel.format.extension
                )?.let {
                    uris.add(it)
                    executeVideoEdit(
                        videoUri.toUri(),
                        it,
                        commandString,
                        "-ss ${start.formatTimeToDigits()}",
                        "-t ${splitTime.formatTimeToDigits()}"
                    )
                }
            }
        }
        callbacks.forEach { it.onTrimEnded(uris) }
    }

    private fun createFile(filename: String, extension: String): Uri? {
        val contentValues = ContentValues()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.put(
                MediaStore.MediaColumns.DISPLAY_NAME,
                filename.plus("_${System.currentTimeMillis()}")
            )
            return if (extension == ".mp3") {
                contentValues.put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_MUSIC.plus("/").plus(context.getString(R.string.app_name))
                )
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mpeg")
                context.contentResolver.insert(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                )
            } else {
                contentValues.put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_DCIM.plus("/").plus(context.getString(R.string.app_name))
                )
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
                context.contentResolver.insert(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                )
            }
        } else {
            createFileLegacy(filename, extension)
        }
    }

    private fun createFileLegacy(filename: String, extension: String): Uri {
        val file =
            File(Environment.getExternalStorageDirectory(), context.getString(R.string.app_name))
        if (!file.exists()) {
            file.mkdir()
        }
        return File(file, filename.plus(extension)).apply {
            if (exists()) {
                delete()
            }
        }.absolutePath.toUri()
    }

    private suspend fun executeVideoEdit(
        videoUri: Uri,
        path: Uri,
        commandString: String,
        startTime: String,
        splitTime: String
    ) {
        val iUri = FFmpegKitConfig.getSafParameterForRead(context, videoUri)
        val oUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            FFmpegKitConfig.getSafParameterForWrite(context, path)
        } else {
            path.toString()
        }
        AndroidUtilities.log(oUri.toString())

        try {
            val session = FFmpegKit.execute(" $startTime -i $iUri $splitTime $commandString $oUri")
            AndroidUtilities.log("FFMPEG process exited with state ${session.state} and return code ${session.returnCode}")
            val returnCode = session.returnCode
            if (ReturnCode.isSuccess(returnCode)) {
                withContext(Dispatchers.Main) {
                    AndroidUtilities.log("Success")
                    AndroidUtilities.showToastMessage(
                        context,
                        "Video trimmed successfully"
                    )
                }
                //operation successfull
            } else {
                shouldTerminate = true
                AndroidUtilities.log("Failed")
                //operation failed
            }
        } catch (exception: Exception) {
            AndroidUtilities.log("An unknown exception occurred")
        }
    }

}