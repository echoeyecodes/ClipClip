package com.echoeyecodes.clipclip.customviews.videoview

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.widget.FrameLayout
import com.echoeyecodes.clipclip.utils.AndroidUtilities

class VideoFrameView(context: Context, attributeSet: AttributeSet) :
    FrameLayout(context, attributeSet) {
    private var bitmap: Bitmap? = null
    private var _width = 0
    private var _height = 0
    private val paint = Paint().apply {
        isFilterBitmap = false
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        bitmap?.let {
//            canvas.drawBitmap(it, 0f, (_height.toFloat() - it.height) / 2, Paint())
//            canvas.drawBitmap(it, srcRect, destRect,null)
            val startX = (_width - it.width) / 2f
            val startY = (_height - it.height) / 2f
            canvas.drawBitmap(it, startX, startY, paint)
        }
    }

    fun updateBitmap(bitmap: Bitmap) {
        post {

            val bitmapWidth = bitmap.width
            val bitmapHeight = bitmap.height

            val ratioBitmap = bitmapWidth / bitmapHeight.toFloat()
            val ratioMax = width / height.toFloat()

            var finalWidth = width
            var finalHeight = height

            if (ratioMax > ratioBitmap) {
                finalWidth = (height * ratioBitmap).toInt()
            } else {
                finalHeight = (width / ratioBitmap).toInt()
            }

            this.bitmap = createScaledBitmap(bitmap, finalWidth, finalHeight)
            val newLayoutParams = layoutParams as LayoutParams
            newLayoutParams.width = finalWidth
            newLayoutParams.height = finalHeight
            layoutParams = newLayoutParams
            invalidate()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        this._width = w
        this._height = h
    }

    private fun createScaledBitmap(bitmap: Bitmap, finalWidth: Int, finalHeight: Int): Bitmap {

//        return bitmap
        return Bitmap.createScaledBitmap(bitmap, finalWidth, finalHeight, true)
    }
}