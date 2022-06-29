package com.echoeyecodes.clipclip.utils

import android.content.res.Resources
import android.graphics.Bitmap
import android.util.TypedValue
import com.echoeyecodes.clipclip.models.VideoCanvasModel
import com.echoeyecodes.clipclip.models.VideoEditConfigModel
import com.google.gson.Gson
import io.alterac.blurkit.BlurKit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

fun Pair<Float, Float>.getDimensions(size: Float): Pair<Float, Float> {
    val newDimension = if (this.first > this.second) {
        val value = (this.second / this.first) * size
        Pair(size, value)
    } else {
        val value = (this.first / this.second) * size
        Pair(value, size)
    }
    return newDimension
}

fun convertToAspectRatio(
    src: Pair<Float, Float>,
    des: Pair<Float, Float>
): Pair<Float, Float> {
    val srcWidth = src.first
    val srcHeight = src.second

    val desWidth = des.first
    val desHeight = des.second

    val srcRatio = srcWidth / srcHeight
    val desRatio = desWidth / desHeight

    var finalWidth = desWidth
    var finalHeight = desHeight

    if (desRatio > srcRatio) {
        finalWidth = finalHeight * srcRatio
    } else {
        finalHeight = finalWidth / srcRatio
    }
    return Pair(finalWidth, finalHeight)
}


suspend fun Bitmap.blurFrame(selectedDimension: VideoCanvasModel, blurFactor: Int): Bitmap? {
    return withContext(Dispatchers.IO) {
        if (selectedDimension.width == 0.0f && selectedDimension.height == 0.0f) {
            null
        } else {
            val bitmap = this@blurFrame

            val dimension = Pair(selectedDimension.width, selectedDimension.height)
            val rows = bitmap.height.toFloat()
            val cols = bitmap.width.toFloat()

            val newDimension =
                convertToAspectRatio(Pair(dimension.first, dimension.second), Pair(cols, rows))
            val height = newDimension.second
            val width = newDimension.first

            val rowMid = height / 2
            val colMid = width / 2

            val rowStart = (rowMid - (height / 2)).toInt()
            val rowEnd = (rowMid + (height / 2)).toInt()
            val colStart = (colMid - (width / 2)).toInt()
            val colEnd = (colMid + (width / 2)).toInt()

            val croppedBitmap = Bitmap.createBitmap(bitmap, colStart, rowStart, colEnd, rowEnd)
            val newBitmap = BlurKit.getInstance().blur(croppedBitmap, blurFactor)
            newBitmap
        }
    }
}

fun VideoEditConfigModel.serialize(): String {
    val gson = Gson()
    return gson.toJson(this)
}

fun String.toVideoEditConfigModel(): VideoEditConfigModel {
    val gson = Gson()
    return gson.fromJson(this, VideoEditConfigModel::class.java)
}