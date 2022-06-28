package com.echoeyecodes.clipclip.utils

import android.content.Context
import android.net.Uri
import android.os.Build
import com.arthenica.ffmpegkit.FFmpegKitConfig
import com.echoeyecodes.clipclip.models.VideoBlurModel
import com.echoeyecodes.clipclip.models.VideoCanvasModel

class FFMPEGCommand private constructor(val command: String) {

    class Builder {
        private var inputUri: String? = null
        private var format: VideoFormat = VideoFormat.MP4
        private var outputUri: String? = null
        private var startTime: Long = 0
        private var splitTime: Long = 0
        private var quality: VideoQuality = VideoQuality.HIGH
        private var blurConfig: VideoBlurModel? = null

        fun inputUri(
            context: Context,
            inputUri: Uri,
            outputUri: Uri,
            startTime: Long,
            splitTime: Long
        ): Builder {
            this.startTime = startTime
            this.splitTime = splitTime
            this.inputUri = FFmpegKitConfig.getSafParameterForRead(context, inputUri)
            this.outputUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                FFmpegKitConfig.getSafParameterForWrite(context, outputUri)
            } else {
                outputUri.toString()
            }
            return this
        }

        fun format(fmt: VideoFormat): Builder {
            this.format = fmt
            return this
        }

        fun setQuality(qty: VideoQuality): Builder {
            this.quality = qty
            return this
        }

        fun setBlurConfig(
            blurConfig: VideoBlurModel?
        ): Builder {
            this.blurConfig = blurConfig

            return this
        }

        private fun getTrimTime(): String {
            return "-ss ${startTime.formatTimeToFFMPEGTimeDigits()} -t ${splitTime.formatTimeToFFMPEGTimeDigits()}"
        }


        private fun getQualityFilter(): String {
            return when (quality) {
                VideoQuality.VERY_LOW -> {
                    "scale=trunc(iw/10)*2:trunc(ih/10)*2"
                }
                VideoQuality.LOW -> {
                    "scale=trunc(iw/8)*2:trunc(ih/8)*2"
                }
                VideoQuality.MEDIUM -> {
                    "scale=trunc(iw/6)*2:trunc(ih/6)*2"
                }
                VideoQuality.HIGH -> {
                    "scale=trunc(iw/4)*2:trunc(ih/4)*2"
                }
                else -> {
                    "scale=iw:ih"
                }
            }
        }

        private fun getBlurFilter(): String {
            return if (blurConfig != null) {
                val targetDimension = Dimension(
                    blurConfig!!.videoCanvasModel.width,
                    blurConfig!!.videoCanvasModel.height
                )
                val videoDimension = blurConfig!!.dimension
                val blurFactor = blurConfig!!.blurFactor

                val desRatio = targetDimension.width / targetDimension.height
                val srcRatio = videoDimension.width / videoDimension.height

                val canvasCrop = if (desRatio > srcRatio) {
                    "[copy]scale=ih*${targetDimension.width}/${targetDimension.height}:-1,crop=h=iw*${targetDimension.height}/${targetDimension.width}"
                } else {
                    "[copy]scale=-1:iw/(${targetDimension.width}/${targetDimension.height}),crop=w=ih/(${targetDimension.height}/${targetDimension.width})"
                }
                "split=2[original][copy];$canvasCrop,gblur=sigma=$blurFactor[blurred];[blurred][original]overlay=(main_w-overlay_w)/2:(main_h-overlay_h)/2"
            } else ""
        }

        private fun parseFilters(): String {
            return if (format == VideoFormat.MP3) {
                return "-q:a 0 -map a"
            } else {
                if (blurConfig == null) {
                    "-vf \"${getQualityFilter()}\""
                } else "-vf \"${getQualityFilter()}, ${getBlurFilter()}\""
            }
        }

        fun build(): FFMPEGCommand {
            if (inputUri == null || outputUri == null) {
                throw Exception("Input/Output Uri must be initialized")
            }

            val command = "${getTrimTime()} -i $inputUri ${parseFilters()} $outputUri"
//            val command = "$trim -i $inputUri $format -vf \"$scaleFilter $blurFilter\" $outputUri"
            AndroidUtilities.log(command)
            return FFMPEGCommand(command)
        }
    }

}