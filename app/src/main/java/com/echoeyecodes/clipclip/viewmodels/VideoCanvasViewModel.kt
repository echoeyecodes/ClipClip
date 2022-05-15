package com.echoeyecodes.clipclip.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.echoeyecodes.clipclip.models.VideoCanvasModel
import org.opencv.android.OpenCVLoader


class VideoCanvasViewModel(application: Application) :
    VideoFrameViewModel(application) {
    val videoDimensions: LiveData<List<VideoCanvasModel>>

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
}