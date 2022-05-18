package com.echoeyecodes.clipclip.utils

import android.content.Context
import android.net.Uri
import android.os.Build
import com.arthenica.ffmpegkit.FFmpegKitConfig
import com.echoeyecodes.clipclip.models.VideoCanvasModel

class FFMPEGCommand private constructor(val command: String) {

    class Builder {
        private var inputUri: String? = null
        private var format: String = ""
        private var outputUri: String? = null
        private var trim: String = ""
        private var scaleFilter: String = ""
        private var blurFilter = ""

        fun init(context: Context, inputUri: Uri, outputUri: Uri): Builder {
            inputUri(context, inputUri)
            outputUri(context, outputUri)
            return this
        }

        fun inputUri(context: Context, uri: Uri): Builder {
            this.inputUri = FFmpegKitConfig.getSafParameterForRead(context, uri)
            return this
        }

        fun outputUri(context: Context, uri: Uri): Builder {
            this.outputUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                FFmpegKitConfig.getSafParameterForWrite(context, uri)
            } else {
                uri.toString()
            }
            return this
        }

        fun format(fmt: VideoFormat): Builder {
            val format = if (fmt == VideoFormat.MP3) {
                "-q:a 0 -map a"
            } else {
                "-vcodec libx264"
            }
            this.format = format
            return this
        }

        fun setCanvas(
            canvasModel: VideoCanvasModel,
            videoDimension: Dimension,
            blurFactor: Int
        ): Builder {
            val desRatio = canvasModel.width / canvasModel.height
            val srcRatio = videoDimension.width / videoDimension.height
            val canvasCrop = if (desRatio > srcRatio) {
                "[copy]scale=ih*${canvasModel.width}/${canvasModel.height}:-1,crop=h=iw*${canvasModel.height}/${canvasModel.width}"
            } else {
                "[copy]scale=-1:iw/(${canvasModel.width}/${canvasModel.height}),crop=w=ih/(${canvasModel.height}/${canvasModel.width})"
            }
            this.blurFilter =
                ", split=2[original][copy];$canvasCrop,gblur=sigma=$blurFactor[blurred];[blurred][original]overlay=(main_w-overlay_w)/2:(main_h-overlay_h)/2"
            return this
        }

        fun quality(qty: VideoQuality): Builder {
            val scale = when (qty) {
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
                    ""
                }
            }
            this.scaleFilter = scale
            return this
        }

        fun trim(startTime: Long, splitTime: Long): Builder {
            this.trim =
                "-ss ${startTime.formatTimeToFFMPEGTimeDigits()} -t ${splitTime.formatTimeToFFMPEGTimeDigits()}"
            return this
        }

        fun build(): FFMPEGCommand {
            if (inputUri == null || outputUri == null) {
                throw Exception("Input/Output Uri must be initialized")
            }
            val command = "$trim -i $inputUri $format -vf \"$scaleFilter $blurFilter\" $outputUri"
            AndroidUtilities.log(command)
            return FFMPEGCommand(command)
        }
    }

}