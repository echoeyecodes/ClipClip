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
import kotlin.math.min


class VideoCanvasViewModel(application: Application) :
    AndroidViewModel(application) {
    val image = MutableLiveData<Bitmap?>()
    val videoDimensions: LiveData<List<VideoCanvasModel>>
    private var selectedDimensionsLiveData = MutableLiveData(VideoCanvasModel(0.0, 0.0))

    init {
        videoDimensions = Transformations.map(selectedDimensionsLiveData) {
            initVideoCanvasDimensions(it)
        }
        OpenCVLoader.initDebug()
    }

    private fun initVideoCanvasDimensions(selectedDimension: VideoCanvasModel): List<VideoCanvasModel> {
        return listOf(
            VideoCanvasModel(0.0, 0.0),
            VideoCanvasModel(1.0, 1.0),
            VideoCanvasModel(4.0, 5.0),
            VideoCanvasModel(16.0, 9.0),
            VideoCanvasModel(9.0, 16.0),
            VideoCanvasModel(3.0, 4.0),
            VideoCanvasModel(4.0, 3.0),
            VideoCanvasModel(2.0, 3.0),
            VideoCanvasModel(3.0, 2.0),
            VideoCanvasModel(2.0, 1.0),
            VideoCanvasModel(1.0, 2.0)
        ).map {
            it.isSelected = it == selectedDimension
            it
        }
    }

    fun setSelectedDimension(dimension: VideoCanvasModel) {
        selectedDimensionsLiveData.value = dimension
    }

    fun blurFrame(bitmap: Bitmap) {
        val selectedDimension = selectedDimensionsLiveData.value ?: VideoCanvasModel(0.0, 0.0)
        if (selectedDimension.width == 0.0 && selectedDimension.height == 0.0) {
            image.value = null
        } else
            viewModelScope.launch(Dispatchers.IO) {
                val mat = Mat()
                Utils.bitmapToMat(bitmap, mat)
                val dimension = Pair(selectedDimension.width, selectedDimension.height)
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
}