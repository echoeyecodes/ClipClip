package com.echoeyecodes.clipclip.viewmodels

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.*
import com.echoeyecodes.clipclip.models.VideoCanvasModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import kotlin.math.ceil
import kotlin.math.min


class VideoCanvasViewModel(application: Application) :
    AndroidViewModel(application) {
    val image = MutableLiveData<Bitmap?>()
    val videoDimensions: LiveData<List<VideoCanvasModel>>
    private var selectedDimensionsLiveData = MutableLiveData(VideoCanvasModel(0.0f, 0.0f))

    init {
        videoDimensions = Transformations.map(selectedDimensionsLiveData) {
            initVideoCanvasDimensions(it)
        }
        OpenCVLoader.initDebug()
    }

    private fun initVideoCanvasDimensions(selectedDimension: VideoCanvasModel): List<VideoCanvasModel> {
        return listOf(
            VideoCanvasModel(0.0f, 0.0f),
            VideoCanvasModel(1.0f, 1.0f),
            VideoCanvasModel(4.0f, 5.0f),
            VideoCanvasModel(16.0f, 9.0f),
            VideoCanvasModel(9.0f, 16.0f),
            VideoCanvasModel(3.0f, 4.0f),
            VideoCanvasModel(4.0f, 3.0f),
            VideoCanvasModel(2.0f, 3.0f),
            VideoCanvasModel(3.0f, 2.0f),
            VideoCanvasModel(2.0f, 1.0f),
            VideoCanvasModel(1.0f, 2.0f)
        ).map {
            it.isSelected = it == selectedDimension
            it
        }
    }

    fun setSelectedDimension(dimension: VideoCanvasModel) {
        selectedDimensionsLiveData.value = dimension
    }

    fun blurFrame(bitmap: Bitmap) {
        val selectedDimension = selectedDimensionsLiveData.value ?: VideoCanvasModel(0.0f, 0.0f)
        if (selectedDimension.width == 0.0f && selectedDimension.height == 0.0f) {
            image.value = null
        } else
            viewModelScope.launch(Dispatchers.IO) {
                val mat = Mat()
                Utils.bitmapToMat(bitmap, mat)
                val dimension =
                    Pair(selectedDimension.width, selectedDimension.height)
                val rows = mat.rows().toFloat()
                val cols = mat.cols().toFloat()
                val minimumSize = min(cols, rows)

                val newDimension = if (dimension.first > dimension.second) {
                    val value = (dimension.second / dimension.first) * minimumSize
                    Pair(minimumSize, value)
                } else {
                    val value = (dimension.first / dimension.second) * minimumSize
                    Pair(value, minimumSize)
                }
                val height = ceil(newDimension.second).toInt()
                val width = ceil(newDimension.first).toInt()

                val rowMid = rows / 2
                val colMid = cols / 2

                val rowStart = (rowMid - (height / 2)).toInt()
                val rowEnd = (rowMid + (height / 2)).toInt()
                val colStart = (colMid - (width / 2)).toInt()
                val colEnd = (colMid + (width / 2)).toInt()

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
}