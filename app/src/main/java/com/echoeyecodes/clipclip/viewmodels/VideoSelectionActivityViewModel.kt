package com.echoeyecodes.clipclip.viewmodels

import android.annotation.SuppressLint
import android.app.Application
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.echoeyecodes.clipclip.models.VideoModel

class VideoSelectionActivityViewModel(application: Application) : AndroidViewModel(application) {
    private val videoData = MutableLiveData<List<VideoModel>>(ArrayList())


    fun getVideosLiveData(): LiveData<List<VideoModel>> {
        return videoData
    }

    @SuppressLint("Range")
    fun fetchVideosFromFileSystem() {
        val contentResolver = getApplication<Application>().applicationContext.contentResolver
        val uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DURATION
        )

        val cursor = contentResolver.query(
            uri,
            projection,
            null,
            null,
            MediaStore.MediaColumns.DATE_ADDED + " DESC"
        )

        if (cursor != null) {
            val _videoList = ArrayList<VideoModel>()
            while (cursor.moveToNext()) {
                val id = cursor.getLong(cursor.getColumnIndex(projection[0]))
                val title = cursor.getString(cursor.getColumnIndex(projection[2]))
                val duration = cursor.getLong(cursor.getColumnIndex(projection[4]))
                val path = cursor.getString(cursor.getColumnIndex(projection[3]))

                _videoList.add(VideoModel(id, title, path, duration))
            }
            cursor.close()
            videoData.value = _videoList
        }
    }
}