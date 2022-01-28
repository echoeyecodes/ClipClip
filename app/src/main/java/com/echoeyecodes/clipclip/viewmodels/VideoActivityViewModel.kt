package com.echoeyecodes.clipclip.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.*
import com.echoeyecodes.clipclip.utils.toSeconds
import com.echoeyecodes.clipclip.utils.withPrefix
import kotlin.math.max

class VideoActivityViewModelFactory(private val duration: Long, private val context: Context) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VideoActivityViewModel::class.java)) {
            return VideoActivityViewModel(duration, context.applicationContext as Application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class VideoActivityViewModel(private val duration: Long, application: Application) :
    AndroidViewModel(application) {
    var isPlaying = true
    private var startTime: Long = 0L
    private var endTime: Long = 100L
    private val timestampLiveData = MutableLiveData("00:00 - 00:00")
    private val timestampDifferenceLiveData = MutableLiveData("00:00")
    var currentPosition = 0L
    var trimProgress = Pair(0, 0)
    var splitTime = 1

    init {
        setVideoTimestamps(0f, 100f)
    }

    fun getMarkerPositions(): Pair<Float, Float> {
        val start = (startTime.toFloat() / duration.toFloat())
        val end = (endTime.toFloat() / duration.toFloat())

        return Pair(start, end)
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

    fun setVideoTimestamps(startX: Float, endX: Float) {
        val startTime = convertToTimestamp(startX)
        val endTime = convertToTimestamp(endX)

        this.startTime = startTime
        this.endTime = endTime

        timestampDifferenceLiveData.value = formatTimestamp(max(0, this.endTime - this.startTime))
        timestampLiveData.value = "${formatTimestamp(startTime)} - ${formatTimestamp(endTime)}"
    }

    fun convertToTimestamp(value: Float): Long {
        return ((value / 100) * duration).toLong()
    }

    private fun formatTimestamp(value: Long): String {
        val totalSeconds = value / 1000
        val hours = "0".plus(totalSeconds / 60 / 60)
        val minutes = "0".plus((totalSeconds / 60 % 60))
        val seconds = "0".plus((totalSeconds % 60 % 60) % 60)
        return "${hours.withPrefix()}:${minutes.withPrefix()}:${seconds.withPrefix()}"
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