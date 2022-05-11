package com.echoeyecodes.clipclip.viewmodels

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.*
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

    init {
        OpenCVLoader.initDebug()
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
}