package com.echoeyecodes.clipclip.customviews.videoselectionview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.RelativeLayout
import kotlin.math.max
import kotlin.math.min

class VideoSelectionMarkerView(context: Context, attr: AttributeSet) :
    RelativeLayout(context, attr) {
    var startX = 0f
    var gravity = VideoSelectionGravity.LEFT
    var selectionMarkerCallback: VideoSelectionMarkerCallback? = null
    private var paint = Paint().apply {
        color = Color.rgb(255, 255, 255)
    }
    private var path = Path()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        path.reset()
        path.moveTo(0f, 0f)
        path.lineTo(width.toFloat(), 0f)
        path.lineTo(width.toFloat(), height.toFloat())
        path.lineTo(0f, height.toFloat())

        canvas.drawPath(path, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val _x = event.rawX

        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = x - _x
                selectionMarkerCallback?.onMarkerSelected(gravity)
                true
            }
            MotionEvent.ACTION_MOVE -> {
                selectMarkerPosition(_x + startX)
                true
            }
            MotionEvent.ACTION_UP -> {
                selectionMarkerCallback?.onMarkerReleased(gravity)
                true
            }
            else -> false
        }
    }

    fun selectMarkerPosition(value: Float) {
        val newPosition = determineThumbPosition(value)
        x = newPosition
        selectionMarkerCallback?.onMarkerMove(gravity, newPosition)
    }

    private fun determineThumbPosition(position: Float): Float {
        val parentView = (parent as VideoSelectionView)
        val maxWidth = (parentView.width - width).toFloat()
        return if (gravity == VideoSelectionGravity.RIGHT) {
            //allow to overlap first thumb to obtain at least a 0.0 percentage
            max(parentView.getBound(gravity), max(0f, min(position, maxWidth)))
        } else {
            min(parentView.getBound(gravity) - width, max(0f, min(position, maxWidth)))
        }
    }
}