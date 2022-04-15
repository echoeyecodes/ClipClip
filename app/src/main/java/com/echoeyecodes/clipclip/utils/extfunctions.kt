package com.echoeyecodes.clipclip.utils

import android.content.res.Resources
import android.util.TypedValue
import com.echoeyecodes.clipclip.models.VideoConfigModel
import com.google.gson.Gson
import kotlin.math.max


fun Int.convertToDp(): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    )
        .toInt()
}

fun Float.convertToDp(): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this,
        Resources.getSystem().displayMetrics
    )
        .toInt()
}


private fun delimitTime(value: Int, suffix: String): String {
    return if (value <= 1) {
        "$value $suffix"
    } else {
        "$value ${suffix}s"
    }
}

fun String.withPrefix(size: Int = 2): String {
    return this.substring(
        max(
            this.length - size,
            0
        )
    )
}

//Only use with FFMPEG TRIM
fun Int.formatTimeToDigits(): String {
    val totalSeconds = this
    val hours = "0".plus(totalSeconds / 60 / 60)
    val minutes = "0".plus((totalSeconds / 60 % 60))
    val seconds = "0".plus((totalSeconds % 60 % 60) % 60)
    val milliSeconds = "00".plus((this % 1000))
    return "${hours.withPrefix()}:${minutes.withPrefix()}:${seconds.withPrefix()}.${
        milliSeconds.withPrefix(
            3
        )
    }"
}

fun Long.formatTimeToDigits(): String {
    val totalSeconds = this / 1000
    val hours = "0".plus(totalSeconds / 60 / 60)
    val minutes = "0".plus((totalSeconds / 60 % 60))
    val seconds = "0".plus((totalSeconds % 60 % 60) % 60)
    val milliSeconds = "00".plus((this % 1000))
    return "${hours.withPrefix()}:${minutes.withPrefix()}:${seconds.withPrefix()}:${
        milliSeconds.withPrefix(
            3
        )
    }"
}

fun Long.formatVideoTime(): String {
    val totalSeconds = this / 1000
    val hours = "0".plus(totalSeconds / 60 / 60)
    val minutes = "0".plus((totalSeconds / 60 % 60))
    val seconds = "0".plus((totalSeconds % 60 % 60) % 60)
    return "${hours.withPrefix()}:${minutes.withPrefix()}:${seconds.withPrefix()}"
}

fun String.formatDigitsToLong(): Long {
    val timeArray = this.split(":")
    val hours = timeArray[0].toInt()
    val minutes = timeArray[1].toInt()
    val seconds = timeArray[2].toInt()
    val milliSeconds = timeArray[3].toInt()

    return (((hours * 60 * 60) + (minutes * 60) + seconds) * 1000L) + milliSeconds
}

fun Long.toSeconds(): Int {
    return (this.toFloat() / 1000).toInt()
}

fun String.toVideoConfigModel(): VideoConfigModel {
    val gson = Gson()
    return gson.fromJson(this, VideoConfigModel::class.java)
}

fun VideoConfigModel.serialize(): String {
    val gson = Gson()
    return gson.toJson(this)
}