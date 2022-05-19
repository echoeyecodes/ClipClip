package com.echoeyecodes.clipclip.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.*
import com.echoeyecodes.clipclip.models.VideoCanvasModel
import org.opencv.osgi.OpenCVNativeLoader

class VideoCanvasViewModelFactory(
    private val videoCanvasModel: VideoCanvasModel?,
    private val blurFactor: Int,
    private val context: Context
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VideoCanvasViewModel::class.java)) {
            return VideoCanvasViewModel(
                videoCanvasModel,
                blurFactor,
                context.applicationContext as Application
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


class VideoCanvasViewModel(
    videoCanvasModel: VideoCanvasModel?,
    bFactor: Int,
    application: Application
) :
    VideoFrameViewModel(videoCanvasModel, application) {
    val videoDimensions: LiveData<List<VideoCanvasModel>>

    init {
        OpenCVNativeLoader().init()
        blurFactor = bFactor
        videoDimensions = Transformations.map(selectedDimensionsLiveData) {
            initVideoCanvasDimensions(it)
        }
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
}