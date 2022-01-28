package com.echoeyecodes.clipclip.activities

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.echoeyecodes.clipclip.databinding.ActivityWelcomeBinding
import com.echoeyecodes.clipclip.utils.AndroidUtilities
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.echoeyecodes.clipclip.R
import com.echoeyecodes.clipclip.adapters.VideoAdapter
import com.echoeyecodes.clipclip.callbacks.VideoAdapterCallback
import com.echoeyecodes.clipclip.models.VideoModel
import com.echoeyecodes.clipclip.utils.ActivityUtil
import com.echoeyecodes.clipclip.utils.CustomItemDecoration
import com.echoeyecodes.clipclip.viewmodels.VideoSelectionActivityViewModel


class WelcomeActivity : AppCompatActivity(), VideoAdapterCallback {
    private val binding by lazy { ActivityWelcomeBinding.inflate(layoutInflater) }
    private lateinit var recyclerView: RecyclerView
    private val requiredPermissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    private val viewModel by lazy { ViewModelProvider(this)[VideoSelectionActivityViewModel::class.java] }

    companion object {
        const val PERMISSIONS_REQUEST_CODE = 3
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        createNotificationChannel()
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

    override fun onVideoSelected(model: VideoModel) {
        val selectedUrl = model.getVideoUri()
        AndroidUtilities.log(selectedUrl.toString())
        ActivityUtil.startVideoActivity(this, selectedUrl.toString(), model.duration)
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

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.trim_notification_channel_title)
            val descriptionText = getString(R.string.trim_notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(
                getString(R.string.trim_notification_channel_id),
                name,
                importance
            ).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

}