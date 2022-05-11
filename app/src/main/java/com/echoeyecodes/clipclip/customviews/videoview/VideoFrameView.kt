package com.echoeyecodes.clipclip.customviews.videoview

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
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
        if (bitmap == null) {
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        } else {
            bitmap?.let {
                val startX = (_width - it.width) / 2f
                val startY = (_height - it.height) / 2f
                canvas.drawBitmap(it, startX, startY, paint)
            }
        }
    }

    private fun getParentView(): View {
        return parent as View
    }

    fun resetBitmap() {
        this.bitmap = null
        val newLayoutParams = layoutParams as LayoutParams
        val parentView = getParentView()
        newLayoutParams.width = parentView.width
        newLayoutParams.height = parentView.height
        layoutParams = newLayoutParams
        invalidate()
    }

    fun updateBitmap(bitmap: Bitmap) {
        post {

            val bitmapWidth = bitmap.width
            val bitmapHeight = bitmap.height

            val ratioBitmap = bitmapWidth / bitmapHeight.toFloat()
            val parentView = getParentView()
            val ratioMax = parentView.width / parentView.height.toFloat()

            var finalWidth = parentView.width
            var finalHeight = parentView.height

            if (ratioMax > ratioBitmap) {
                finalWidth = (finalHeight * ratioBitmap).toInt()
            } else {
                finalHeight = (finalWidth / ratioBitmap).toInt()
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