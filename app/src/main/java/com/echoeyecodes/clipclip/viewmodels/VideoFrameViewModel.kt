package com.echoeyecodes.clipclip.viewmodels

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.echoeyecodes.clipclip.models.VideoCanvasModel
import com.echoeyecodes.clipclip.utils.blurFrame
import kotlinx.coroutines.launch

abstract class VideoFrameViewModel(videoCanvasModel: VideoCanvasModel?, application: Application) :
    AndroidViewModel(application) {
    val image = MutableLiveData<Bitmap?>()
    protected var selectedDimensionsLiveData =
        MutableLiveData(videoCanvasModel ?: VideoCanvasModel(0.0f, 0.0f))

    fun blurFrame(bitmap: Bitmap) {
        viewModelScope.launch {
            val selectedDimension = selectedDimensionsLiveData.value ?: VideoCanvasModel(0.0f, 0.0f)
            val newBitmap = bitmap.blurFrame(selectedDimension)
            image.value = newBitmap
        }
    }

    fun getSelectedDimensionsLiveData(): LiveData<VideoCanvasModel> {
        return selectedDimensionsLiveData
    }

    fun setSelectedDimension(dimension: VideoCanvasModel) {
        selectedDimensionsLiveData.value = dimension
    }
}