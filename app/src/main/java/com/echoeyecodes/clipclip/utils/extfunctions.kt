package com.echoeyecodes.clipclip.utils

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.provider.OpenableColumns
import android.util.TypedValue
import androidx.core.view.doOnLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.URL
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.regex.Pattern
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

fun String.withPrefix(): String {
    return this.substring(
        max(
            this.length - 2,
            0
        )
    )
}

fun Int.formatTimeToDigits(): String {
    val totalSeconds = this
    val hours = "0".plus(totalSeconds / 60 / 60)
    val minutes = "0".plus((totalSeconds / 60 % 60))
    val seconds = "0".plus((totalSeconds % 60 % 60) % 60)
    return "${hours.withPrefix()}:${minutes.withPrefix()}:${seconds.withPrefix()}"
}

fun Long.formatTimeToDigits(): String {
    val totalSeconds = this/1000
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

    val total = ((hours * 60 * 60) + (minutes * 60) + seconds) * 1000L
    return total
}

fun Long.toSeconds(): Int {
    return (this.toFloat() / 1000).toInt()
}