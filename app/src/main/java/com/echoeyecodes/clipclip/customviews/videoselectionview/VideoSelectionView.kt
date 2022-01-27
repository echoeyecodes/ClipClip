package com.echoeyecodes.clipclip.customviews.videoselectionview

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewGroup
import com.echoeyecodes.clipclip.utils.convertToDp

class VideoSelectionView(context: Context, attributeSet: AttributeSet) :
    ViewGroup(context, attributeSet), VideoSelectionMarkerCallback {
    private var startX = 0f
    private var endX = 0f
    private var selectionPaint = Paint().apply {
        color = Color.argb(70, 0, 255, 255)
    }
    val thumbWidth = 30.convertToDp()
    private var selectionRectF = RectF()
    var selectionCallback: VideoSelectionCallback? = null

    private var thumbX = 0f

    init {
        setWillNotDraw(false)
        val thumbOne = VideoSelectionMarkerView(context, attributeSet).apply {
            selectionMarkerCallback = this@VideoSelectionView
        }
        val thumbTwo = VideoSelectionMarkerView(context, attributeSet).apply {
            gravity = VideoSelectionGravity.RIGHT
            selectionMarkerCallback = this@VideoSelectionView
        }

        addView(thumbOne)
        addView(thumbTwo)
        post {
            setXCoordinates(0f, (width - thumbWidth).toFloat())
        }
    }

    fun setXCoordinates(startX: Float, endX: Float) {
        this.startX = startX
        this.endX = endX
        selectionRectF = RectF(startX, 0f, endX + thumbWidth, bottom.toFloat())
        invalidate()
    }

    private fun setStartX(startX: Float) {
        this.startX = startX
        selectionRectF = RectF(startX, 0f, endX + thumbWidth, bottom.toFloat())
        invalidate()
    }

    private fun setEndX(endX: Float) {
        this.endX = endX
        selectionRectF = RectF(startX, 0f, endX + thumbWidth, bottom.toFloat())
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawRect(selectionRectF, selectionPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val _x = event.rawX

        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                thumbX = startX - _x
                true
            }
            MotionEvent.ACTION_MOVE -> {
                val thumb1 = getChildAt(0) as VideoSelectionMarkerView
                val thumb2 = getChildAt(1) as VideoSelectionMarkerView

                val newStartX = _x + thumbX
                val newEndX = this.endX + (newStartX - this.startX)

                if (newStartX >= 0 && newEndX < (width - thumbWidth).toFloat()) {
                    onMarkerSelected(VideoSelectionGravity.LEFT)
                    thumb1.selectMarkerPosition(newStartX)
                    thumb2.selectMarkerPosition(newEndX)
                }
                true
            }
            MotionEvent.ACTION_UP -> {
                if (isBetweenCoordinates(_x)) {
                    onMarkerReleased(VideoSelectionGravity.LEFT)
                }
                true
            }
            else -> false
        }
    }

    private fun isBetweenCoordinates(value: Float): Boolean {
        return value in startX..endX
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val firstThumb = getChildAt(0) as VideoSelectionMarkerView
        val secondThumb = getChildAt(1) as VideoSelectionMarkerView

        firstThumb.layout(startX.toInt(), 0, (startX + thumbWidth).toInt(), height)
        secondThumb.layout(endX.toInt(), 0, (endX + thumbWidth).toInt(), height)
    }

    fun getBound(gravity: VideoSelectionGravity): Float {
        return if (gravity == VideoSelectionGravity.LEFT) {
            endX
        } else {
            startX
        }
    }

    override fun onMarkerMove(gravity: VideoSelectionGravity, valueX: Float) {
        if (gravity == VideoSelectionGravity.LEFT) {
            setStartX(valueX)
        } else {
            setEndX(valueX)
        }
        val range = determineSelectionRange(startX, endX)
        selectionCallback?.onSelectionMoved(range.first, range.second)
    }

    override fun onMarkerSelected(gravity: VideoSelectionGravity) {
        val range = determineSelectionRange(startX, endX)
        selectionCallback?.onSelectionStarted(gravity, range.first, range.second)
    }

    override fun onMarkerReleased(gravity: VideoSelectionGravity) {
        val range = determineSelectionRange(startX, endX)
        selectionCallback?.onSelectionEnded(gravity, range.first, range.second)
    }

    private fun determineSelectionRange(x1: Float, x2: Float): Pair<Float, Float> {
        val offset = thumbWidth
        val selectionWidth = width - offset

        val _x1 = (x1 / selectionWidth) * 100
        val _x2 = (x2 / selectionWidth) * 100
        return Pair(_x1, _x2)
    }

    fun updateMarkerPosition(gravity: VideoSelectionGravity, percentageValue: Float) {
        val child = if (gravity == VideoSelectionGravity.LEFT) {
            getChildAt(0) as VideoSelectionMarkerView
        } else {
            getChildAt(1) as VideoSelectionMarkerView
        }
        val value = percentageValue * (width - thumbWidth)
        child.selectMarkerPosition(value)
    }


    /**
     * Positions markers in specific positions via percentage values
     * ranges from 0.0 - 1.0
     * @param startX percentage value for the start position
     * @param endX percentage value for the end position
     */
    fun updateMarkerPosition(startX: Float, endX: Float) {
        post {
            val thumb1 = getChildAt(0) as VideoSelectionMarkerView
            val thumb2 = getChildAt(1) as VideoSelectionMarkerView
            val value1 = startX * (width - thumbWidth)
            val value2 = endX * (width - thumbWidth)

            thumb1.selectMarkerPosition(value1)
            thumb2.selectMarkerPosition(value2)
        }
    }
}