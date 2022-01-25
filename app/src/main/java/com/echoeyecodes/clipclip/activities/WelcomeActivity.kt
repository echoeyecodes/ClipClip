package com.echoeyecodes.clipclip.activities

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.arthenica.ffmpegkit.*
import com.echoeyecodes.clipclip.databinding.ActivityWelcomeBinding
import com.echoeyecodes.clipclip.utils.AndroidUtilities
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.echoeyecodes.clipclip.R
import com.echoeyecodes.clipclip.adapters.VideoAdapter
import com.echoeyecodes.clipclip.callbacks.VideoAdapterCallback
import com.echoeyecodes.clipclip.model.VideoModel
import com.echoeyecodes.clipclip.utils.ActivityUtil
import com.echoeyecodes.clipclip.utils.CustomItemDecoration
import com.echoeyecodes.clipclip.viewmodels.VideoSelectionActivityViewModel
import java.io.File


class WelcomeActivity : AppCompatActivity(), VideoAdapterCallback {
    private val binding by lazy { ActivityWelcomeBinding.inflate(layoutInflater) }
    private lateinit var recyclerView: RecyclerView
    private val requiredPermissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    private val viewModel by lazy { ViewModelProvider(this)[VideoSelectionActivityViewModel::class.java] }

    companion object {
        const val CREATE_FILE = 1
        const val ACCESS_FOLDER = 2
        const val PERMISSIONS_REQUEST_CODE = 3
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        recyclerView = binding.recyclerView

        val adapter = VideoAdapter(this)
        val layoutManager = GridLayoutManager(this, 2)
        val itemDecoration = CustomItemDecoration(2)

        recyclerView.layoutManager = layoutManager
        recyclerView.addItemDecoration(itemDecoration)
        recyclerView.adapter = adapter

        viewModel.getVideosLiveData().observe(this, {
            adapter.submitList(it)
        })
//        executeVideoEdit()
        checkDataAccessPermissions()
    }

    private fun checkSelfPermission(permissions: Array<String>): Boolean {
        return permissions.all {
            ContextCompat.checkSelfPermission(
                this,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun checkDataAccessPermissions() {
        if (!checkSelfPermission(requiredPermissions)) {
            ActivityCompat.requestPermissions(this, requiredPermissions, PERMISSIONS_REQUEST_CODE)
        } else {
            viewModel.fetchVideosFromFileSystem()
        }
    }

    private fun executeVideoEdit(path: Uri) {
        val iUri = FFmpegKitConfig.getSafParameterForRead(this, Uri.parse("selectedUrl!!"))
        val oUri = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            FFmpegKitConfig.getSafParameterForWrite(this, path)
        } else {
            path
        }
        AndroidUtilities.log(oUri.toString())

        try {
            FFmpegKit.executeAsync(
                "-i $iUri -c:v mpeg4 $oUri",
                { session ->
                    AndroidUtilities.log("FFMPEG process exited with state ${session.state} and return code ${session.returnCode}")
                    val returnCode = session.returnCode
                    if (ReturnCode.isSuccess(returnCode)) {
                        AndroidUtilities.log("Success")
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

    override fun onVideoSelected(model: VideoModel) {
        val selectedUrl = model.getVideoUri()
        AndroidUtilities.log(selectedUrl.toString())
        ActivityUtil.startVideoActivity(this, selectedUrl.toString(), model.duration)
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val granted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        if (granted) {
            viewModel.fetchVideosFromFileSystem()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, result: Intent?) {
        super.onActivityResult(requestCode, resultCode, result)

        if (requestCode == CREATE_FILE && resultCode == RESULT_OK) {
            if (result != null) {
                executeVideoEdit(result.data!!)
            }
        }
    }

}