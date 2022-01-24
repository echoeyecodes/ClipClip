package com.echoeyecodes.clipclip.activities

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.arthenica.ffmpegkit.*
import com.echoeyecodes.clipclip.databinding.ActivityWelcomeBinding
import com.echoeyecodes.clipclip.utils.AndroidUtilities
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
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
    var selectedUrl: Uri? = null

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
        val iUri = FFmpegKitConfig.getSafParameterForRead(this, selectedUrl!!)
        val uri = FFmpegKitConfig.getSafParameterForWrite(this, path)

        try {
            FFmpegKit.executeAsync(
                "-i $iUri -c:v mpeg4 $uri",
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
            }
        } catch (exception: Exception) {
            AndroidUtilities.log("An unknown exception occurred")
        }
    }

    override fun onVideoSelected(model: VideoModel) {
        selectedUrl = model.getVideoUri()
        ActivityUtil.startVideoActivity(this, selectedUrl!!.toString(), model.duration)
//        allowAccessToFolder()
    }

    private fun createFile() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "video/mp4"
            putExtra(Intent.EXTRA_TITLE, "video.mp4")
        }
        startActivityForResult(intent, CREATE_FILE)
    }

    private fun allowAccessToFolder() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        startActivityForResult(intent, ACCESS_FOLDER)
    }

    private fun createFileFromDocument(documentUri: Uri): Uri? {
        val documentFile = DocumentFile.fromTreeUri(this, documentUri) ?: return null
        val file = documentFile.createFile("video/mp4", "jasdsad.mp4")
        return file?.uri
    }

    private fun getLegacyFilePath(filename: String): File {
        val file =
            File(Environment.getExternalStorageDirectory(), getString(R.string.app_name)).apply {
                if (!exists()) {
                    mkdir()
                }
            }
        return File(file, filename).apply {
            if (exists()) {
                delete()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, result: Intent?) {
        super.onActivityResult(requestCode, resultCode, result)

        if (requestCode == CREATE_FILE && resultCode == RESULT_OK) {
            if (result != null) {
                executeVideoEdit(result.data!!)
            }
        } else if (requestCode == ACCESS_FOLDER && resultCode == RESULT_OK) {
            result?.data?.let {
                val uri = createFileFromDocument(it)
                executeVideoEdit(uri!!)
            }
        }
    }

}