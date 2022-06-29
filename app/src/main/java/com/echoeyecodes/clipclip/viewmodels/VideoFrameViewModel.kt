package com.echoeyecodes.clipclip.viewmodels

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.echoeyecodes.clipclip.models.VideoCanvasModel
import com.echoeyecodes.clipclip.utils.DEFAULT_BLUR_FACTOR
import com.echoeyecodes.clipclip.utils.blurFrame
import io.alterac.blurkit.BlurKit
import kotlinx.coroutines.launch


/**
 * Blur factor could be a live data that calls on the blurFrame method via a livedata mediator
 * but that is totally unnecessary because the player handler in the VideoActivity checks for player
 * progress every 30milliseconds. The canvas fragment listens to this handler and updates the canvas from
 * every 30ms as well
 */

abstract class VideoFrameViewModel(videoCanvasModel: VideoCanvasModel?, application: Application) :
    AndroidViewModel(application) {
    val image = MutableLiveData<Bitmap?>()
    var blurFactor = DEFAULT_BLUR_FACTOR
    protected var selectedDimensionsLiveData =
        MutableLiveData(videoCanvasModel ?: VideoCanvasModel(0.0f, 0.0f))

    init {
        BlurKit.init(application)
    }

    fun blurFrame(bitmap: Bitmap) {
        viewModelScope.launch {
            val selectedDimension = selectedDimensionsLiveData.value ?: VideoCanvasModel(0.0f, 0.0f)
            val newBitmap = bitmap.blurFrame(selectedDimension, blurFactor)
            image.value = newBitmap
        }
    }

    fun getSelectedDimensionsLiveData(): LiveData<VideoCanvasModel> {
        return selectedDimensionsLiveData
    }

    fun getSelectedDimensions(): VideoCanvasModel {
        return selectedDimensionsLiveData.value ?: VideoCanvasModel(0.0f, 0.0f)
    }

    fun setSelectedDimension(dimension: VideoCanvasModel) {
        selectedDimensionsLiveData.value = dimension
    }

}