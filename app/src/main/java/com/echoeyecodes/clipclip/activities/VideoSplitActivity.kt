package com.echoeyecodes.clipclip.activities

import android.content.ContentValues
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.arthenica.ffmpegkit.*
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.echoeyecodes.clipclip.R
import com.echoeyecodes.clipclip.databinding.ActivityVideoSplitBinding
import com.echoeyecodes.clipclip.models.VideoConfigModel
import com.echoeyecodes.clipclip.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.ceil
import kotlin.math.min


class VideoSplitActivity : AppCompatActivity() {
    private val binding by lazy { ActivityVideoSplitBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val configModel =
            intent.getSerializableExtra("videoConfig") as VideoConfigModel? ?: return finish()

        FFmpegKitConfig.enableStatisticsCallback {
            //            val progress = (time / videoduration) * 100
            AndroidUtilities.log(it.time.toString())
        }
        lifecycleScope.launch(Dispatchers.IO) {
            test(configModel)
        }
    }

    private fun test(configModel: VideoConfigModel) {
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

        for (i in 0 until count) {

            val videoUri = intent.getStringExtra("videoUri") ?: return finish()
            val start = (configModel.startTime.toSeconds() + (i * configModel.splitTime))

            val splitTime = if ((configModel.endTime.toSeconds() - start) < configModel.splitTime) {
                configModel.endTime.toSeconds() - start
            } else {
                configModel.splitTime
            }

            if (configModel.splitTime + start < configModel.endTime) {
                val commandString =
                    " -t ${splitTime.formatTimeToDigits()} -ss ${start.formatTimeToDigits()} $format"
                if (start >= configModel.endTime.toSeconds()) {
                    break
                }
                createFile(
                    getString(R.string.app_name).plus(i),
                    configModel.format.extension
                )?.let {
                    executeVideoEdit(videoUri.toUri(), it, commandString)
                }
            }
        }
        finish()
    }

    private fun executeVideoEdit(videoUri: Uri, path: Uri, commandString: String) {
        val iUri = FFmpegKitConfig.getSafParameterForRead(this, videoUri)
        val oUri = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            FFmpegKitConfig.getSafParameterForWrite(this, path)
        } else {
            path.toString()
        }
        AndroidUtilities.log(oUri.toString())

        try {
            val session =
                FFmpegKit.execute("-i $iUri $commandString $oUri")
            AndroidUtilities.log("FFMPEG process exited with state ${session.state} and return code ${session.returnCode}")
            val returnCode = session.returnCode
            if (ReturnCode.isSuccess(returnCode)) {
                runOnUiThread {
                    AndroidUtilities.log("Success")
                    AndroidUtilities.showToastMessage(this, "Video trimmed successfully")
                }
                //operation successfull
            } else {
                AndroidUtilities.log("Failed")
                //operation failed
            }
        } catch (exception: Exception) {
            AndroidUtilities.log("An unknown exception occurred")
        }
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
                    Environment.DIRECTORY_MUSIC.plus("/").plus(getString(R.string.app_name))
                )
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mpeg")
                contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues)
            } else {
                contentValues.put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_DCIM.plus("/").plus(getString(R.string.app_name))
                )
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
                contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)
            }
        } else {
            createFileLegacy(filename, extension)
        }
    }

    private fun createFileLegacy(filename: String, extension: String): Uri {
        val file = File(Environment.getExternalStorageDirectory(), getString(R.string.app_name))
        if (!file.exists()) {
            file.mkdir()
        }
        return File(file, filename.plus(extension)).apply {
            if (exists()) {
                delete()
            }
        }.absolutePath.toUri()
    }
}