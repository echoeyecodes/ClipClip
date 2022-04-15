package com.echoeyecodes.clipclip.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class VideoTimestampViewModel() : ViewModel() {
    private val isValid = MutableLiveData(false)
    private val pattern = "^[0-9][0-9]:[0-9][0-9]:[0-9][0-9]:[0-9][0-9][0-9]\$"
    private var startTime = ""
    private var endTime = ""

    fun getValidStatus(): LiveData<Boolean> {
        return isValid
    }

    private fun validate() {
        isValid.value = startTime.matches(Regex(pattern)) && endTime.matches(Regex(pattern))
    }

    fun setStartTime(startTime: String) {
        this.startTime = startTime
        validate()
    }

    fun setEndTime(endTime: String) {
        this.endTime = endTime
        validate()
    }
}