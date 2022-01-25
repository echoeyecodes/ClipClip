package com.echoeyecodes.clipclip.activities

import android.content.ContentValues
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.arthenica.ffmpegkit.*
import com.echoeyecodes.clipclip.utils.AndroidUtilities
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.net.toUri
import com.echoeyecodes.clipclip.R
import com.echoeyecodes.clipclip.databinding.ActivityVideoSplitBinding
import java.io.File


class VideoSplitActivity : AppCompatActivity() {
    private val binding by lazy { ActivityVideoSplitBinding.inflate(layoutInflater) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val videoUri = intent.getStringExtra("videoUri") ?: return finish()
        createFile("sommm")?.let {
            executeVideoEdit(videoUri.toUri(), it)
        }
    }

    private fun executeVideoEdit(videoUri: Uri, path: Uri) {
        val iUri = FFmpegKitConfig.getSafParameterForRead(this, videoUri)
        val oUri = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            FFmpegKitConfig.getSafParameterForWrite(this, path)
        } else {
            path.toString()
        }
        AndroidUtilities.log(oUri.toString())

        try {
            FFmpegKit.executeAsync(
                "-i $iUri -t 00:10 -ss 00:30 -qscale 0 ${oUri.plus(".mp4")}",
                { session ->
                    AndroidUtilities.log("FFMPEG process exited with state ${session.state} and return code ${session.returnCode}")
                    val returnCode = session.returnCode
                    if (ReturnCode.isSuccess(returnCode)) {
                        runOnUiThread {
                            AndroidUtilities.log("Success")
                            AndroidUtilities.showToastMessage(this, "Video trimmed successfully")
                            finish()
                        }
                        //operation successfull
                    } else {
                        AndroidUtilities.log("Failed")
                        //operation failed
                    }
                }, {
//                AndroidUtilities.log(it.message.toString())
                }) {
                //executes repeatedly
//            val progress = (time / videoduration) * 100
                AndroidUtilities.log(it.time.toString())
            }
        } catch (exception: Exception) {
            AndroidUtilities.log("An unknown exception occurred")
        }
    }

    private fun createFile(filename: String): Uri? {
        val contentValues = ContentValues()
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "")
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                Environment.DIRECTORY_DCIM.plus("/").plus(getString(R.string.app_name))
            )
            contentValues.put(
                MediaStore.MediaColumns.DISPLAY_NAME,
                filename.plus("_${System.currentTimeMillis()}")
            )
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)
        } else {
            createFileLegacy(filename)
        }
    }

    private fun createFileLegacy(filename: String): Uri {
        val file = File(Environment.getExternalStorageDirectory(), getString(R.string.app_name))
        if (!file.exists()) {
            file.mkdir()
        }
        return File(file, filename.plus(".mp4")).apply {
            if (exists()) {
                delete()
            }
        }.absolutePath.toUri()
    }
}