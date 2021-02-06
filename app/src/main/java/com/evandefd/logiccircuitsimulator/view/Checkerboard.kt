package com.evandefd.logiccircuitsimulator.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.annotation.ColorInt
import com.evandefd.logiccircuitsimulator.R
import java.util.*
import kotlin.math.ceil
import kotlin.math.pow


@SuppressLint("ClickableViewAccessibility")
class Checkerboard @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : SurfaceView(context, attributeSet, defStyleAttr), SurfaceHolder.Callback {

    companion object {
        private const val INTERVAL_DEFAULT_SIZE = 7
        private val defaultIntervals = Array(INTERVAL_DEFAULT_SIZE) {
            2.0.pow((INTERVAL_DEFAULT_SIZE / 2) - it).toFloat() * 10f
        }

        const val STRATEGY_MIN_MAX = 0
        const val STRATEGY_MIN_2 = 1
        const val STRATEGY_MAX_2 = 2
    }

    private val checkerboard = this
    var checkerBoardScaleView: CheckerBoardScaleView? = null
        set(value) {
            field = value
            checkerBoardScaleView?.maxIntervalPixel =
                millimeter2px(maxShowingIntervalMillimeter, context.resources.displayMetrics.xdpi)
        }
    var onCheckerBoardChangedListener: OnCheckerBoardChangedListener? = null

    private var oldX = 0f
    private var oldY = 0f
    private var activePointerId = MotionEvent.INVALID_POINTER_ID

    private val scaleGestureDetector =
        ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                drawCheckerBoardThread?.let {
                    it.multiplier *= detector.scaleFactor
                    if (it.multiplier > maxMultiplier) it.multiplier = maxMultiplier
                    if (it.multiplier < minMultiplier) it.multiplier = minMultiplier
                }
                return true
            }
        })

    var intervals = defaultIntervals
        set(value) {
            if (value.size < 2) {
                Log.e(this.javaClass.simpleName, "Intervals must have more than two items.")
            } else {
                field = value
            }
        }

    private var drawCheckerBoardThread: DrawCheckerBoardThread? = null
    private val xAxisPaint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 3f
    }
    private val yAxisPaint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 3f
    }
    private val secondaryXAxisPaint = Paint().apply {
        color = 0xFF888888.toInt()
        strokeWidth = 2f
    }
    private val secondaryYAxisPaint = Paint().apply {
        color = 0xFF888888.toInt()
        strokeWidth = 2f
    }
    private val tertiaryXAxisPaint = Paint().apply {
        color = 0xFFbbbbbb.toInt()
        strokeWidth = 1f
    }
    private val tertiaryYAxisPaint = Paint().apply {
        color = 0xFFbbbbbb.toInt()
        strokeWidth = 1f
    }

    @ColorInt
    var xAxisColor = Color.BLACK
        set(value) {
            xAxisPaint.color = value
            field = value
        }

    @ColorInt
    var yAxisColor = Color.BLACK
        set(value) {
            yAxisPaint.color = value
            field = value
        }

    @ColorInt
    var secondaryXAxisColor = 0xFF888888.toInt()
        set(value) {
            secondaryXAxisPaint.color = value
            field = value
        }

    @ColorInt
    var secondaryYAxisColor = 0xFF888888.toInt()
        set(value) {
            secondaryYAxisPaint.color = value
            field = value
        }

    @ColorInt
    var tertiaryXAxisColor = 0xFFbbbbbb.toInt()
        set(value) {
            tertiaryXAxisPaint.color = value
            field = value
        }

    @ColorInt
    var tertiaryYAxisColor = 0xFFbbbbbb.toInt()
        set(value) {
            tertiaryYAxisPaint.color = value
            field = value
        }

    var xAxisThickness = 3f
        set(value) {
            xAxisPaint.strokeWidth = value
            field = value
        }
    var yAxisThickness = 3f
        set(value) {
            yAxisPaint.strokeWidth = value
            field = value
        }
    var secondaryXAxisThickness = 2f
        set(value) {
            secondaryXAxisPaint.strokeWidth = value
            field = value
        }
    var secondaryYAxisThickness = 2f
        set(value) {
            secondaryYAxisPaint.strokeWidth = value
            field = value
        }
    var tertiaryXAxisThickness = 1f
        set(value) {
            tertiaryXAxisPaint.strokeWidth = value
            field = value
        }
    var tertiaryYAxisThickness = 1f
        set(value) {
            tertiaryYAxisPaint.strokeWidth = value
            field = value
        }

    @ColorInt
    var canvasBackgroundColor = Color.WHITE

    var maxShowingIntervalMillimeter = 20f
        set(value) {
            checkerBoardScaleView?.maxIntervalPixel =
                millimeter2px(value, context.resources.displayMetrics.xdpi)
            field = value
        }
    var minShowingIntervalMillimeter = 1f

    var showingIntervalStrategy: Int = STRATEGY_MAX_2

    var showSecondaryXAxis = true
    var showTertiaryXAxis = true
    var showSecondaryYAxis = true
    var showTertiaryYAxis = true
    var showXAxis = true
    var showYAxis = true

    var maxMultiplier = 75f
    var minMultiplier = 0.075f

    var multiplier: Float
        get() = drawCheckerBoardThread?.multiplier ?: 1f
        set(value) {
            drawCheckerBoardThread?.multiplier = value
        }

    private var _currentSecondaryInterval: Float? = null
    private var _currentTertiaryInterval: Float? = null

    val currentSecondaryInterval: Float? get() = _currentSecondaryInterval
    val currentTertiaryInterval: Float? get() = _currentTertiaryInterval

    init {
        holder.addCallback(this)

        context.theme.obtainStyledAttributes(
            attributeSet,
            R.styleable.Checkerboard,
            defStyleAttr,
            0
        ).apply {
            xAxisColor = getColor(R.styleable.Checkerboard_xAxisColor, Color.BLACK)
            yAxisColor = getColor(R.styleable.Checkerboard_yAxisColor, Color.BLACK)
            secondaryXAxisColor =
                getColor(R.styleable.Checkerboard_secondaryXAxisColor, 0xFF888888.toInt())
            secondaryYAxisColor =
                getColor(R.styleable.Checkerboard_secondaryYAxisColor, 0xFF888888.toInt())
            tertiaryXAxisColor =
                getColor(R.styleable.Checkerboard_tertiaryXAxisColor, 0xFFBBBBBB.toInt())
            tertiaryYAxisColor =
                getColor(R.styleable.Checkerboard_tertiaryXAxisColor, 0xFFBBBBBB.toInt())

            xAxisThickness = getDimension(R.styleable.Checkerboard_xAxisThickness, 3f)
            yAxisThickness = getDimension(R.styleable.Checkerboard_yAxisThickness, 3f)
            secondaryXAxisThickness =
                getDimension(R.styleable.Checkerboard_secondaryXAxisThickness, 2f)
            secondaryYAxisThickness =
                getDimension(R.styleable.Checkerboard_secondaryYAxisThickness, 2f)
            tertiaryXAxisThickness =
                getDimension(R.styleable.Checkerboard_tertiaryXAxisThickness, 1f)
            tertiaryYAxisThickness =
                getDimension(R.styleable.Checkerboard_tertiaryYAxisThickness, 1f)

            canvasBackgroundColor =
                getColor(R.styleable.Checkerboard_canvasBackgroundColor, Color.WHITE)

            showSecondaryXAxis = getBoolean(R.styleable.Checkerboard_showSecondaryXAxis, true)
            showTertiaryXAxis = getBoolean(R.styleable.Checkerboard_showTertiaryXAxis, true)
            showSecondaryYAxis = getBoolean(R.styleable.Checkerboard_showSecondaryYAxis, true)
            showTertiaryYAxis = getBoolean(R.styleable.Checkerboard_showTertiaryYAxis, true)
            showXAxis = getBoolean(R.styleable.Checkerboard_showXAxis, true)
            showYAxis = getBoolean(R.styleable.Checkerboard_showYAxis, true)

            recycle()
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                oldX = event.x
                oldY = event.y
                activePointerId = event.getPointerId(0)
            }
            MotionEvent.ACTION_MOVE -> {
                event.findPointerIndex(activePointerId).let { pointerIndex ->
                    if (pointerIndex != MotionEvent.INVALID_POINTER_ID) {
                        val (x, y) = event.getX(pointerIndex) to event.getY(pointerIndex)
                        drawCheckerBoardThread?.let {
                            it.offsetPixelX += x - oldX
                            it.offsetPixelY += y - oldY
                        }

                        oldX = x
                        oldY = y
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                activePointerId = MotionEvent.INVALID_POINTER_ID
            }
            MotionEvent.ACTION_POINTER_UP -> {
                event.actionIndex.also { pointerIndex ->
                    event.getPointerId(pointerIndex)
                        .takeIf { it == activePointerId }
                        ?.run {
                            // This was our active pointer going up. Choose a new
                            // active pointer and adjust accordingly.
                            val newPointerIndex = if (pointerIndex == 0) 1 else 0
                            oldX = event.getX(newPointerIndex)
                            oldY = event.getY(newPointerIndex)
                            activePointerId = event.getPointerId(newPointerIndex)
                        }
                }
            }
        }
        scaleGestureDetector.onTouchEvent(event)
        return true
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        if (drawCheckerBoardThread == null)
            with(context.resources.displayMetrics) {
                drawCheckerBoardThread = DrawCheckerBoardThread(xdpi, ydpi)
            }
        drawCheckerBoardThread?.start()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {

    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        drawCheckerBoardThread = null
    }

    inner class DrawCheckerBoardThread(
        private val xDensity: Float = 160f,
        private val yDensity: Float = 160f,
        var multiplier: Float = 1f,
        var offsetPixelX: Float = 0f,
        var offsetPixelY: Float = 0f
    ) : Thread() {
        var beforeMultiplier: Float? = null
        var beforeOffsetPixelX: Float? = null
        var beforeOffsetPixelY: Float? = null

        var drawingChecks = 0

        private var originPointX = (width / 2).toFloat()
        var originPointY = (height / 2).toFloat()

        override fun run() {
            while (drawCheckerBoardThread != null) {
                if (multiplier != beforeMultiplier ||
                    offsetPixelX != beforeOffsetPixelX ||
                    offsetPixelY != beforeOffsetPixelY
                ) {
                    drawingChecks = 0
                    beforeMultiplier = multiplier
                    beforeOffsetPixelX = offsetPixelX
                    beforeOffsetPixelY = offsetPixelY

                    originPointX = (width / 2 + offsetPixelX)
                    originPointY = (height / 2 + offsetPixelY)

                    val canvas =
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            holder.lockHardwareCanvas()
                        } else {
                            holder.lockCanvas()
                        }
                    canvas.drawColor(canvasBackgroundColor)

                    val newIntervals = intervals.filter {
                        it * multiplier in minShowingIntervalMillimeter..maxShowingIntervalMillimeter
                    }.toMutableList()

                    if (newIntervals.isEmpty()) {
                        if (intervals.last() * multiplier > maxShowingIntervalMillimeter) newIntervals.add(
                            intervals.last()
                        )
                        if (intervals.first() * multiplier < minShowingIntervalMillimeter) newIntervals.add(
                            intervals.first()
                        )
                    }

                    if (newIntervals.isNotEmpty()) {
                        when (showingIntervalStrategy) {
                            STRATEGY_MIN_MAX -> {
                                _currentSecondaryInterval = newIntervals.first()
                                _currentTertiaryInterval = newIntervals.last()

                            }
                            STRATEGY_MAX_2 -> {
                                _currentSecondaryInterval = newIntervals.first()
                                _currentTertiaryInterval = if (newIntervals.size > 1)
                                    newIntervals[1] else null
                            }
                            STRATEGY_MIN_2 -> {
                                _currentSecondaryInterval = if (newIntervals.size > 1)
                                    newIntervals[newIntervals.size - 2] else null
                                _currentTertiaryInterval = newIntervals.last()
                            }
                            else -> throw IllegalStateException("Wrong strategy!!")
                        }

                        _currentTertiaryInterval?.let {
                            drawCheck(
                                it,
                                canvas,
                                tertiaryXAxisPaint,
                                tertiaryYAxisPaint,
                                showTertiaryXAxis,
                                showTertiaryYAxis
                            )
                        }
                        _currentSecondaryInterval?.let {
                            drawCheck(
                                it,
                                canvas,
                                secondaryXAxisPaint,
                                secondaryYAxisPaint,
                                showSecondaryXAxis,
                                showSecondaryYAxis
                            )
                        }
                    }

                    drawAxis(canvas)

                    holder.unlockCanvasAndPost(canvas)
                    checkerBoardScaleView?.updateValue(checkerboard)
                    Handler(Looper.getMainLooper()).post(Runnable { // do UI work
                        onCheckerBoardChangedListener?.onCanvasChanged(
                            multiplier,
                            offsetPixelX,
                            offsetPixelY
                        )
                    })
                }
            }
        }

        private fun drawCheck(
            intervalMillimeter: Float,
            canvas: Canvas,
            paintX: Paint,
            paintY: Paint,
            showX: Boolean,
            showY: Boolean
        ) {
            val intervalXPixel = millimeter2px(intervalMillimeter, xDensity)
            val intervalYPixel = millimeter2px(intervalMillimeter, yDensity)
            if (showX) drawCheckRightX(intervalXPixel * multiplier, canvas, paintX)
            if (showX) drawCheckLeftX(intervalXPixel * multiplier, canvas, paintX)
            if (showY) drawCheckBottomY(intervalYPixel * multiplier, canvas, paintY)
            if (showY) drawCheckTopY(intervalYPixel * multiplier, canvas, paintY)
        }

        //Draw x = 0 graphs in the right position of y axis
        private fun drawCheckRightX(intervalPixel: Float, canvas: Canvas, paint: Paint) {
            var rightX = originPointX
            //x0 y0 x1 y1
            val pts = LinkedList<Float>()

            if (rightX < 0)
                rightX += getRoundInterval(intervalPixel, rightX * -1)

            while (rightX <= width) {
                pts.add(rightX)
                pts.add(0f)
                pts.add(rightX)
                pts.add(height.toFloat())

                rightX += intervalPixel
            }

            canvas.drawLines(pts.toFloatArray(), paint)
        }

        //Draw x = 0 graphs in the left position of y axis
        private fun drawCheckLeftX(intervalPixel: Float, canvas: Canvas, paint: Paint) {
            var leftX = originPointX
            //x0 y0 x1 y1
            val pts = LinkedList<Float>()

            if (leftX > width)
                leftX += getRoundInterval(intervalPixel, leftX)

            while (leftX >= 0) {
                pts.add(leftX)
                pts.add(0f)
                pts.add(leftX)
                pts.add(height.toFloat())

                leftX -= intervalPixel
            }

            canvas.drawLines(pts.toFloatArray(), paint)
        }

        //Draw y = 0 graphs in the bottom position of x axis
        private fun drawCheckBottomY(intervalPixel: Float, canvas: Canvas, paint: Paint) {
            var bottomY = originPointY
            //x0 y0 x1 y1
            val pts = LinkedList<Float>()

            if (bottomY < 0)
                bottomY += getRoundInterval(intervalPixel, bottomY * -1)

            while (bottomY <= height) {
                pts.add(0f)
                pts.add(bottomY)
                pts.add(width.toFloat())
                pts.add(bottomY)

                bottomY += intervalPixel
            }

            canvas.drawLines(pts.toFloatArray(), paint)
        }

        //Draw y = 0 graphs in the left position of x axis
        private fun drawCheckTopY(intervalPixel: Float, canvas: Canvas, paint: Paint) {
            var topY = originPointY
            //x0 y0 x1 y1
            val pts = LinkedList<Float>()

            if (topY > height)
                topY += getRoundInterval(intervalPixel, topY)

            while (topY >= 0) {
                pts.add(0f)
                pts.add(topY)
                pts.add(width.toFloat())
                pts.add(topY)

                topY -= intervalPixel
            }

            canvas.drawLines(pts.toFloatArray(), paint)
        }

        private fun drawAxis(canvas: Canvas) {
            if (originPointX >= 0 && originPointX <= width && showXAxis)
                canvas.drawLine(
                    originPointX,
                    0f,
                    originPointX,
                    height.toFloat(),
                    xAxisPaint
                )

            if (originPointY >= 0 && originPointY <= height && showYAxis)
                canvas.drawLine(
                    0f,
                    originPointY,
                    width.toFloat(),
                    originPointY,
                    yAxisPaint
                )
        }

        private fun getRoundInterval(intervalBase: Float, value: Float): Float {
            return intervalBase * ceil(value / intervalBase)
        }
    }

    interface OnCheckerBoardChangedListener {
        fun onCanvasChanged(multiplier: Float, offsetPixelX: Float, offsetPixelY: Float)
    }

    inline fun setOnCheckerBoardChangedListener(crossinline func: (multiplier: Float, offsetPixelX: Float, offsetPixelY: Float) -> (Unit)) {
        onCheckerBoardChangedListener = object : OnCheckerBoardChangedListener {
            override fun onCanvasChanged(
                multiplier: Float,
                offsetPixelX: Float,
                offsetPixelY: Float
            ) {
                func(multiplier, offsetPixelX, offsetPixelY)
            }
        }
    }

    fun millimeter2px(millimeter: Float, dpi: Float): Float {
        val mm2px = dpi / 25.4f * millimeter
        return if (mm2px < 1f) 1f else mm2px
    }

    fun getProperIntervalMillimeter(priority: Int = CheckerBoardScaleView.PRIORITY_SECONDARY): Float {
        return when (priority) {
            CheckerBoardScaleView.PRIORITY_SECONDARY -> {
                currentSecondaryInterval ?: currentTertiaryInterval ?: 0f
            }
            CheckerBoardScaleView.PRIORITY_TERTIARY -> {
                currentTertiaryInterval ?: currentSecondaryInterval ?: 0f
            }
            else -> throw IllegalStateException("Not a priority integer value.")
        }
    }

    fun getProperIntervalPixel(priority: Int = CheckerBoardScaleView.PRIORITY_SECONDARY): Float {
        return millimeter2px(
            getProperIntervalMillimeter(priority),
            context.resources.displayMetrics.xdpi
        )
    }
}