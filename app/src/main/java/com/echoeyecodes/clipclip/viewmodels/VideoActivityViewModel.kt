package com.echoeyecodes.clipclip.viewmodels

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.lifecycle.*
import com.echoeyecodes.clipclip.FileUtils
import com.echoeyecodes.clipclip.utils.AndroidUtilities
import com.echoeyecodes.clipclip.utils.getScreenSize
import com.echoeyecodes.clipclip.utils.toSeconds
import com.echoeyecodes.clipclip.utils.withPrefix
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import org.opencv.videoio.VideoCapture
//import org.bytedeco.javacv.AndroidFrameConverter
//import org.bytedeco.javacv.FFmpegFrameGrabber
//import org.bytedeco.javacv.Frame
//import org.bytedeco.javacv.OpenCVFrameConverter
//import org.bytedeco.opencv.global.opencv_videoio.*
//import org.bytedeco.opencv.opencv_core.GpuMat
//import org.bytedeco.opencv.opencv_core.Mat
//import org.bytedeco.opencv.opencv_core.UMat
//import org.bytedeco.opencv.opencv_videoio.VideoCapture
import org.opencv.videoio.Videoio
import org.opencv.videoio.Videoio.CAP_PROP_FPS
import org.opencv.videoio.Videoio.CAP_PROP_FRAME_COUNT
//import org.bytedeco.javacv.AndroidFrameConverter
//import org.bytedeco.javacv.FFmpegFrameGrabber
//import org.bytedeco.javacv.OpenCVFrameConverter
//import org.bytedeco.opencv.opencv_core.Mat
//import org.bytedeco.opencv.opencv_videoio.VideoCapture
//import org.bytedeco.opencv.opencv_videostab.VideoFileSource
import kotlin.math.max
import kotlin.math.min

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
    var splitTime = 1L
    val image = MutableLiveData<Bitmap?>()

    init {
        OpenCVLoader.initDebug()
        duration = getVideoDuration(Uri.parse(uri))
        endTime = duration
        setVideoTimestamps(0f, 1f)
    }

    fun blurFrame(bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.IO) {
            val mat = Mat()
            Utils.bitmapToMat(bitmap, mat)
            val dimension = Pair(1.0, 1.0)
            val minimumSize = min(mat.cols(), mat.rows())

            val newDimension = if (dimension.first > dimension.second) {
                val value = (dimension.second / dimension.first) * minimumSize
                Pair(minimumSize, value)
            } else {
                val value = (dimension.first / dimension.second) * minimumSize
                Pair(value, minimumSize)
            }
            val height = newDimension.second.toInt()
            val width = newDimension.first.toInt()

            val rowMid = mat.rows() / 2
            val colMid = mat.cols() / 2

            val rowStart = rowMid - (height / 2)
            val rowEnd = rowMid + (height / 2)
            val colStart = colMid - (width / 2)
            val colEnd = colMid + (width / 2)
            val submat = mat.submat(rowStart, rowEnd, colStart, colEnd)
            Imgproc.blur(mat, mat, Size(16.0, 16.0))

            val newBitmap =
                Bitmap.createBitmap(submat.width(), submat.height(), Bitmap.Config.ARGB_8888)
            Utils.matToBitmap(submat, newBitmap)
            withContext(Dispatchers.Main) {
                image.value = (newBitmap)
            }
        }
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
    fun getTotalDurationByIndex(): Long {
        val index = (trimProgress.first - 1)
        val start = (startTime + (index * splitTime))
        return if ((endTime - start) < splitTime) {
            endTime - start
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