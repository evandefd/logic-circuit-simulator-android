package com.evandefd.logiccircuitsimulator.view.checkerboard

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import com.evandefd.logiccircuitsimulator.R
import com.evandefd.logiccircuitsimulator.view.millimeter2pxWithXdpi
import kotlin.math.roundToInt

class LogicCircuitBoardScaleView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attributeSet, defStyleAttr) {

    companion object {
        const val PRIORITY_SECONDARY = 0
        const val PRIORITY_TERTIARY = 1
    }

    private val linePaint = Paint()
    private val textPaint = Paint().apply {
        isAntiAlias = true
    }

    var textSize: Float = 16f
        set(value) {
            textPaint.textSize = value
            field = value
            textAlignment = TEXT_ALIGNMENT_CENTER
            invalidate()
        }
    var textColor: Int = Color.BLACK
        set(value) {
            textPaint.color = value
            field = value
            invalidate()
        }
    var lineColor: Int = Color.BLACK
        set(value) {
            linePaint.color = value
            field = value
            invalidate()
        }
    var lineStrokeWidth: Float = 5f
        set(value) {
            linePaint.strokeWidth = value
            field = value
            invalidate()
        }
    var lineTopPadding : Float = 8f
        set(value) {
            field = value
            invalidate()
        }

    var gravity: Int = Gravity.CENTER
    var intervalPriority = PRIORITY_SECONDARY

    var maxIntervalPixel: Float = 0f
        set(value) {
            field = value
            invalidate()
        }
    private var intervalMillimeter: Float = 0f
    private var intervalPixel: Float = 0f
    private var multiplier: Float = 0f

    fun updateValue(logicCircuitBoard: LogicCircuitBoard) {
        intervalMillimeter = logicCircuitBoard.getProperIntervalMillimeter(intervalPriority)
        intervalPixel = context.millimeter2pxWithXdpi(intervalMillimeter)
        multiplier = logicCircuitBoard.multiplier

        postInvalidate()
    }

    init {
        context.theme.obtainStyledAttributes(
            attributeSet,
            R.styleable.CheckerBoardScaleView,
            defStyleAttr,
            0
        ).apply {
            textSize = getDimension(R.styleable.CheckerBoardScaleView_android_textSize, 16f)
            textColor = getColor(R.styleable.CheckerBoardScaleView_android_textColor, Color.BLACK)
            lineColor = getColor(R.styleable.CheckerBoardScaleView_lineColor, Color.BLACK)
            lineStrokeWidth = getDimension(R.styleable.CheckerBoardScaleView_lineThickness, 8f)
            gravity = getInteger(R.styleable.CheckerBoardScaleView_android_gravity, Gravity.CENTER)
            lineTopPadding = getDimension(R.styleable.CheckerBoardScaleView_lineTopPadding, 4f)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val width: Int
        val height: Int

        val text = convertMmToString(intervalMillimeter)
        val bounds = Rect()
        textPaint.getTextBounds(text, 0, text.length, bounds)

        width = if (widthMode == MeasureSpec.EXACTLY)
            widthSize
        else {
            if (bounds.width() > maxIntervalPixel) bounds.width() else maxIntervalPixel.roundToInt()
        }

        height = if (heightMode == MeasureSpec.EXACTLY) {
            heightSize
        } else {
            (bounds.height() * 2 + lineTopPadding + lineStrokeWidth).roundToInt()
        }

        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas?) {
        if (canvas != null) {
            val x: Int
            val y: Int
            val startX: Float
            val startY: Float
            val stopX: Float
            val stopY: Float

            val lineLength = intervalPixel * multiplier

            val text = (if(lineLength > maxIntervalPixel) "< " else "") + convertMmToString(intervalMillimeter)
            val bounds = Rect()
            textPaint.getTextBounds(text, 0, text.length, bounds)

            when (Gravity.getAbsoluteGravity(
                gravity,
                layoutDirection
            ) and Gravity.HORIZONTAL_GRAVITY_MASK) {
                Gravity.CENTER_HORIZONTAL -> {
                    x = width / 2 - bounds.width() / 2 - bounds.left
                    startX = width / 2f - lineLength / 2f
                    stopX = width / 2f + lineLength / 2f
                }
                Gravity.LEFT, Gravity.START -> {
                    x = -bounds.left
                    startX = 0f
                    stopX = lineLength
                }
                Gravity.RIGHT, Gravity.END -> {
                    x = width - bounds.width() - bounds.left
                    startX = width - lineLength
                    stopX = width.toFloat()
                }
                else -> {
                    x = -bounds.left
                    startX = 0f
                    stopX = lineLength
                }
            }

            when (gravity and Gravity.VERTICAL_GRAVITY_MASK) {
                Gravity.TOP -> {
                    y = -bounds.top
                    startY = y + lineTopPadding + bounds.height().toFloat()
                    stopY = y + lineTopPadding + bounds.height().toFloat()
                }
                Gravity.CENTER_VERTICAL -> {
                    y = (height / 2 - bounds.height() / 2 - bounds.top - lineTopPadding - lineStrokeWidth).toInt()
                    startY = y  + bounds.height().toFloat()
                    stopY = y  + bounds.height().toFloat()
                }
                Gravity.BOTTOM -> {
                    y = (height - bounds.height() - bounds.top - lineTopPadding * 2).toInt()
                    startY = height.toFloat()
                    stopY = height.toFloat()
                }
                else -> {
                    y = -bounds.top
                    startY = y + lineTopPadding + bounds.height().toFloat()
                    stopY = y + lineTopPadding + bounds.height().toFloat()
                }
            }

            canvas.drawText(text, x.toFloat(), y.toFloat(), textPaint)

            canvas.drawLine(startX, startY, stopX, stopY, linePaint)
        }
    }

    private fun convertMmToString(mm: Float): String {
        val cm = mm.toDouble() / 10
        if (cm < 1.0) return "${mm.toDouble()}mm"
        val m = cm / 100
        if (m < 1.0) return "${cm}cm"
        val km = m / 1000
        if (km < 1.0) return "${m}m"
        return "${km}km"
    }
}