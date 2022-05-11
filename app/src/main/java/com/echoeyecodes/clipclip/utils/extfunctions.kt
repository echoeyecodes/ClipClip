package com.echoeyecodes.clipclip.utils

import android.content.res.Resources
import android.util.TypedValue
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
fun Long.formatTimeToFFMPEGTimeDigits(): String {
    val totalSeconds = this / 1000
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

fun Long.toSeconds(): Long {
    return (this / 1000)
}

fun String.toVideoQuality(): VideoQuality {
    return when (this) {
        "very_low" -> VideoQuality.VERY_LOW
        "low" -> VideoQuality.LOW
        "medium" -> VideoQuality.MEDIUM
        "high" -> VideoQuality.HIGH
        else -> VideoQuality.NORMAL
    }
}

fun Pair<Double, Double>.getDimensions(size: Double): Pair<Double, Double> {
    val newDimension = if (this.first > this.second) {
        val value = (this.second / this.first) * size
        Pair(size, value)
    } else {
        val value = (this.first / this.second) * size
        Pair(value, size)
    }
    return newDimension
}