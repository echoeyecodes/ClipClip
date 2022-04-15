package com.echoeyecodes.clipclip.viewmodels

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.lifecycle.*
import com.echoeyecodes.clipclip.utils.AndroidUtilities
import com.echoeyecodes.clipclip.utils.toSeconds
import com.echoeyecodes.clipclip.utils.withPrefix
import kotlin.math.max

class VideoActivityViewModelFactory(private val uri: String, private val context: Context) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VideoActivityViewModel::class.java)) {
            return VideoActivityViewModel(uri, context.applicationContext as Application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class VideoActivityViewModel(val uri: String, application: Application) :
    AndroidViewModel(application) {
    private val duration: Long
    var isPlaying = true
    private var startTime: Long = 0L
    private var endTime: Long = 100L
    private val timestampLiveData = MutableLiveData("00:00 - 00:00")
    private val timestampDifferenceLiveData = MutableLiveData("00:00")
    var currentPosition = 0L
    var trimProgress = Pair(0, 0)
    var splitTime = 1

    init {
        duration = getVideoDuration(Uri.parse(uri))
        endTime = duration
        setVideoTimestamps(0f, 1f)
    }

    /*
    Could have used a single content resolver query to retrieve video duration, but
    I can't seem to get the duration column if the video file is sent via a share
    intent due to FileProvider's internal mechanism. Since File Provider provides access to
    the display name of the file, the initial content resolver query retrieves the name, and
    passes it to the second content resolver query to query all video files and return the
    single file that matches the selection filter query based on the display name
     */
    @SuppressLint("Range", "Recycle")
    private fun getVideoDuration(uri: Uri): Long {
        val contentResolver = getApplication<Application>().contentResolver
        val cursor = contentResolver.query(uri, null, null, null, null)
        return if (cursor != null && cursor.moveToNext()) {
            val displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))

            val projections = arrayOf(MediaStore.Video.Media.DURATION)
            val innerCursor = contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projections,
                MediaStore.Video.Media.DISPLAY_NAME.plus(" LIKE ? "),
                arrayOf(displayName),
                null
            )
            if (innerCursor != null && innerCursor.moveToNext()) {
                innerCursor.getLong(innerCursor.getColumnIndex(projections[0]))
            } else {
                0
            }
        } else {
            0
        }
    }

    fun getMarkerPositions(): Pair<Float, Float> {
        val start = (startTime.toFloat() / duration.toFloat())
        val end = (endTime.toFloat() / duration.toFloat())

        return Pair(start, end)
    }

    fun getProgressMarkerPosition(value: Long): Float {
        return value.toFloat() / duration.toFloat()
    }

    /*
        Get the total duration for the current video trim cut
     */
    fun getTotalDurationByIndex(): Int {
        val index = (trimProgress.first - 1)
        val start = (startTime.toSeconds() + (index * splitTime))
        return if ((endTime.toSeconds() - start) < splitTime) {
            endTime.toSeconds() - start
        } else {
            splitTime
        }
    }

    fun getDuration(): Long {
        return duration
    }

    fun setVideoTimestamps(startX: Float, endX: Float) {
        val startTime = convertToTimestamp(startX)
        val endTime = convertToTimestamp(endX)

        this.startTime = startTime
        this.endTime = endTime

        timestampDifferenceLiveData.value = formatTimestamp(max(0, this.endTime - this.startTime))
        timestampLiveData.value = "${formatTimestamp(startTime)} - ${formatTimestamp(endTime)}"
    }

    fun convertToTimestamp(value: Float): Long {
        return ((value) * duration).toLong()
    }

    private fun formatTimestamp(value: Long): String {
        val totalSeconds = value / 1000
        val hours = "0".plus(totalSeconds / 60 / 60)
        val minutes = "0".plus((totalSeconds / 60 % 60))
        val seconds = "0".plus((totalSeconds % 60 % 60) % 60)
        val milliSeconds = "00".plus((value % 1000))
        return "${hours.withPrefix()}:${minutes.withPrefix()}:${seconds.withPrefix()}:${
            milliSeconds.withPrefix(
                3
            )
        }"
    }

    fun getTimestampLiveData(): LiveData<String> {
        return timestampLiveData
    }

    fun getTimestampDifferenceLiveData(): LiveData<String> {
        return timestampDifferenceLiveData
    }

    fun getTimeDifference(): Long {
        return endTime - startTime
    }

    fun getEndTime(): Long {
        return endTime
    }

    fun getStartTime(): Long {
        return startTime
    }

}