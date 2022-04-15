package com.echoeyecodes.clipclip.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.echoeyecodes.clipclip.utils.AndroidUtilities

class VideoTimestampViewModel() : ViewModel() {
    private val isValid = MutableLiveData(false)
    private val pattern = "^[0-9][0-9]:[0-9][0-9]:[0-9][0-9]:[0-9][0-9][0-9]\$"

    fun getValidStatus(): LiveData<Boolean> {
        return isValid
    }

    fun validate(text: String) {
        isValid.value = text.matches(Regex(pattern))
    }
}