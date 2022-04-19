package com.echoeyecodes.clipclip.customviews.videoselectionview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.echoeyecodes.clipclip.R
import java.lang.Math.abs
import kotlin.math.max
import kotlin.math.min


class VideoSelectionView(context: Context, attributeSet: AttributeSet) :
    View(context, attributeSet) {

    enum class TouchPoint {
        LEFT,
        RIGHT,
        CENTER
    }

    private var startX = 0f
    private var endX = 0f
    private var _width = 0
    private var _height = 0

    private var touchPoint: TouchPoint? = null
    private val selectionPaint = Paint().apply {
        strokeWidth = 6f
        color = Color.WHITE
        style = Paint.Style.STROKE
        isAntiAlias = true
    }
    private val thumbPaint = Paint().apply {
        color = Color.WHITE
        pathEffect = DashPathEffect(floatArrayOf(5f, 5f, 5f, 5f), 0f)
    }
    private val progressPaint = Paint().apply {
        color = Color.WHITE
    }
    private val paint = Paint().apply {
        color = ResourcesCompat.getColor(resources, R.color.shot_overlay, null)
    }
    private var thumbStart = 0f
    private var thumbEnd = 0f
    private var progressPosition = 0f
    var selectionCallback: VideoSelectionCallback? = null
    private var h = 0f
    private var w = 0f
    private var strokeOffset = 2f

    companion object {
        const val SIZE = 80f
    }

    override fun onDraw(canvas: Canvas) {
        //view background
        canvas.drawRect(0f, 0f, w, h, paint)

        //round rect between start of thumb and end of thumb
        canvas.drawRoundRect(
            thumbStart + strokeOffset, strokeOffset, thumbEnd - strokeOffset, h - strokeOffset,
            16f,
            16f,
            selectionPaint
        )
        canvas.save()

        //start thumb
        canvas.drawLine(thumbStart + SIZE, 0f, thumbStart + SIZE, h, thumbPaint)

        //end thumb
        canvas.drawLine((thumbEnd - SIZE), 0f, (thumbEnd - SIZE), h, thumbPaint)

        //progress line
        canvas.drawLine(progressPosition, 0f, progressPosition, h, progressPaint)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        this.thumbEnd = w.toFloat()
        this.h = h.toFloat()
        this.w = w.toFloat()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val _x = event.rawX

        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = thumbStart + (x - event.rawX)
                endX = (width - thumbEnd) - (x - event.rawX)

                _width = width
                _height = height

                if (isLeft(event.x)) {
                    touchPoint = TouchPoint.LEFT
                } else if (isRight(event.x)) {
                    touchPoint = TouchPoint.RIGHT
                } else {
                    touchPoint = TouchPoint.CENTER
                }
                selectionCallback?.onSelectionStarted(
                    getThumbStart(),
                    getThumbEnd()
                )
                true
            }
            MotionEvent.ACTION_MOVE -> {
                val positionX = (_x + startX)
                val positionX1 = _x + (_width - abs(endX))

                when (touchPoint) {
                    TouchPoint.LEFT -> {
                        resizeLeft(positionX)
                    }
                    TouchPoint.RIGHT -> {
                        resizeRight(positionX1)
                    }
                    TouchPoint.CENTER -> {
                        moveView(positionX)
                    }
                }
                true
            }
            MotionEvent.ACTION_UP -> {
                touchPoint = null
                selectionCallback?.onSelectionEnded(
                    getThumbStart(),
                    getThumbEnd()
                )
                true
            }
            else -> false
        }
    }

    /**
     * returns true if the touch is between the start thumb
     * position plus width of the thumb
     */
    private fun isLeft(positionX: Float): Boolean {
        return (positionX in thumbStart..thumbStart + SIZE)
    }

    /**
     * returns true if the touch is between the end thumb
     * position minus width of the thumb
     */
    private fun isRight(positionX: Float): Boolean {
        return (positionX in thumbEnd - SIZE..thumbEnd)
    }

    private fun moveView(positionX: Float) {
        val start = getMinMaxLeft(positionX)
        val end = getMinMaxRight(thumbEnd - (thumbStart - start))
        thumbStart -= (thumbEnd - end)
        thumbEnd = end
        executeCallback()
        invalidate()
    }

    /**
     * Changes the left position of the selection view
     */
    private fun resizeLeft(positionX: Float) {
        thumbStart = getMinMaxLeft(positionX)
        executeCallback()
        invalidate()
    }

    /**
     * Changes the right position of the selection view
     */
    private fun resizeRight(positionX: Float) {
        thumbEnd = getMinMaxRight(positionX)
        executeCallback()
        invalidate()
    }

    private fun executeCallback() {
        val start = getThumbStart()
        val end = getThumbEnd()
        selectionCallback?.onSelectionMoved(start, end)
    }

    /**
     * Returns the percentage value position of the start of the thumb
     * relative to the draggable region
     */
    private fun getThumbStart(): Float {
        return (thumbStart) / getActualWidth()
    }

    /**
     * Returns the percentage value position of the end of the thumb
     * relative to the draggable region
     */
    private fun getThumbEnd(): Float {
        return (thumbEnd - (SIZE * 2)) / getActualWidth()
    }

    /**
     * Returns the width of the draggable region
     */
    private fun getActualWidth(): Float {
        return width - (SIZE * 2)
    }

    /**
     * Updates the positions of the start and end thumb
     * @param start Percentage value for the start of the thumb. Ranges between (0.0 -1.0)
     * @param end Percentage value for the end of the thumb. Ranges between (0.0 -1.0)
     */
    fun updateMarkers(start: Float, end: Float) {
        selectionCallback?.onSelectionStarted(getThumbStart(), getThumbEnd())
        post {
            this.thumbStart = getMinMaxLeft(start * getActualWidth())
            this.thumbEnd = getMinMaxRight((end * getActualWidth()) + (SIZE * 2))
            executeCallback()
            invalidate()
            selectionCallback?.onSelectionEnded(getThumbStart(), getThumbEnd())
        }
    }

    /**
     * updates the progress stick position in the selection view
     * @param value between 0.0-1.0 representing the percentage offset from the start
     * of the selection view to the end
     */
    fun updateProgressMarkerPosition(value: Float) {
        val progress = SIZE + (value * getActualWidth())
        progressPosition = progress
        invalidate()
    }

    /**
     * Returns the maximum permissible value of the left thumb
     * i.e ranges between 0 and the start of the
     * right thumb
     */
    private fun getMinMaxLeft(positionX: Float): Float {
        return max(0f, min(positionX, thumbEnd - (SIZE * 2)))
    }

    /**
     * Returns the maximum permissible value of the right thumb
     * i.e not exceeding the width of the screen and the end of the
     * left thumb
     */
    private fun getMinMaxRight(positionX: Float): Float {
        return max(thumbStart + (SIZE * 2), min(positionX, width.toFloat()))
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = 0 + paddingLeft + paddingRight
        val desiredHeight = 0 + paddingTop + paddingBottom

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val width: Int = when (widthMode) {
            MeasureSpec.EXACTLY -> {
                widthSize
            }
            MeasureSpec.AT_MOST -> {
                min(desiredWidth, widthSize)
            }
            else -> {
                desiredWidth
            }
        }

        val height: Int = when (heightMode) {
            MeasureSpec.EXACTLY -> {
                heightSize
            }
            MeasureSpec.AT_MOST -> {
                min(desiredHeight, heightSize)
            }
            else -> {
                desiredHeight
            }
        }

        setMeasuredDimension(width, height)
    }
}