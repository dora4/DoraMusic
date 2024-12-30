package site.doramusic.app.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.text.TextPaint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import dora.skin.SkinManager
import site.doramusic.app.R
import site.doramusic.app.base.conf.AppConfig.Companion.COLOR_THEME
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * 均衡器控件。
 */
class EqualizerView @JvmOverloads constructor(context: Context, attrs:
    AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {

    private lateinit var paint: Paint
    private lateinit var nodePaint: Paint
    private lateinit var nodeConnectPaint: Paint
    private lateinit var freqPaint: TextPaint
    private lateinit var points: Array<PointF?>
    private var state = STATE_NONE
    private var decibels: IntArray = intArrayOf()
    private lateinit var freqs: IntArray
    private var radius = 0f
    private var step = 0f
    private var bandsNum = 0
    private var touchable = false
    private var lastY = 0
    private var index = 0
    private var onUpdateDecibelListener: OnUpdateDecibelListener? = null

    private fun initAttrs(context: Context, attrs: AttributeSet?) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.EqualizerView)
        bandsNum = a.getInt(R.styleable.EqualizerView_ev_bandsNum, 5)
        if (bandsNum < 1) {
            bandsNum = 1
        }
        decibels = IntArray(bandsNum)
        freqs = IntArray(bandsNum)
        points = arrayOfNulls(bandsNum + 2)
        a.recycle()
    }

    companion object {
        private const val STATE_NONE = 0
        private const val STATE_TOUCH_DOWN = 1
        private const val STATE_TOUCH_MOVE = 2
        private const val STATE_TOUCH_UP = 3
    }

    fun setFreqs(freqs: IntArray) {
        this.freqs = freqs
        invalidate()
    }

    fun setDecibels(decibels: IntArray) {
        this.decibels = decibels
        invalidate()
    }

    fun resetState() {
        state = STATE_NONE
        index = 0
    }

    fun setTouchable(touchable: Boolean) {
        this.touchable = touchable
    }

    interface OnUpdateDecibelListener {
        fun onUpdateDecibel(decibels: IntArray)
    }

    fun setOnUpdateDecibelListener(l: OnUpdateDecibelListener) {
        onUpdateDecibelListener = l
    }

    fun setBandsNum(bandsNum: Int) {
        this.bandsNum = bandsNum
        invalidate()
    }

    private fun initPaints() {
        val skinThemeColor = SkinManager.getLoader().getColor(COLOR_THEME)
        paint = Paint()
        paint.isAntiAlias = true
        nodePaint = Paint()
        nodePaint.isAntiAlias = true
        nodePaint.color = skinThemeColor // 圆圈的颜色
        nodePaint.strokeWidth = 6f
        nodePaint.style = Paint.Style.STROKE
        nodeConnectPaint = Paint()
        nodeConnectPaint.isAntiAlias = true
        nodeConnectPaint.strokeWidth = 50f
        nodeConnectPaint.style = Paint.Style.FILL
        nodeConnectPaint.color = skinThemeColor // 圆圈填充的颜色和连线的颜色
        freqPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
        freqPaint.isFakeBoldText = true
        freqPaint.textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10f, context.resources.displayMetrics)
        freqPaint.color = ContextCompat.getColor(context, dora.widget.colors.R.color.white_smoke)
    }

    private fun measureView(measureSpec: Int, defaultSize: Int): Int {
        var measureSize: Int
        val mode = MeasureSpec.getMode(measureSpec)
        val size = MeasureSpec.getSize(measureSpec)
        if (mode == MeasureSpec.EXACTLY) {
            measureSize = size
        } else {
            measureSize = defaultSize
            if (mode == MeasureSpec.AT_MOST) {
                measureSize = min(measureSize, defaultSize)
            }
        }
        return measureSize
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(measureView(widthMeasureSpec, 400),
                measureView(heightMeasureSpec, 200))
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        step = measuredHeight / 26.toFloat() // -12到12共26份
        val stepSize = measuredWidth / (bandsNum + 1)
        points[0] = PointF((-50).toFloat(), step * 13)
        points[bandsNum + 1] = PointF((measuredWidth + 50).toFloat(), step * 13)
        if (state == STATE_NONE) {
            for (i in 1..bandsNum) {
                val cx = stepSize * i.toFloat()
                val cy = step * (decibels[i - 1] + 13)
                points[i] = PointF(cx, cy)
            }
            refreshView(canvas, stepSize)
        } else {
            refreshView(canvas, stepSize)
        }
    }

    private fun refreshView(canvas: Canvas, stepSize: Int) {
        val skinThemeColor = SkinManager.getLoader().getColor(COLOR_THEME)
        val fontMetrics = freqPaint.fontMetrics
        for (i in 1..bandsNum) {
            val cx = stepSize * i.toFloat()
            val cy = points[i]!!.y
            radius = if (i == index && state != STATE_TOUCH_UP) {
                50f
            } else {
                40f
            }
            canvas.drawCircle(cx, cy, radius, nodePaint) // 绘制大圆
            canvas.drawCircle(cx, cy, radius - 6, nodeConnectPaint) // 绘制小圆
            paint.color = ContextCompat.getColor(context, dora.widget.colors.R.color.light_gray) //下面的线的颜色
            paint.strokeWidth = 6f
            canvas.drawLine(cx, cy + radius + 3, stepSize * i.toFloat(), measuredHeight.toFloat(),
                paint
            )
            paint.color = skinThemeColor // 上面的线的颜色
            canvas.drawLine(cx, cy - radius - 3, stepSize * i.toFloat(), 0f, paint)
            val text = if (freqs.isNotEmpty()) formatHz(freqs[i - 1]) else formatHz(0)
            val textWidth = freqPaint.measureText(text)
            val x =
                (i - 1) * (measuredWidth / (bandsNum + 1)) + (measuredWidth / (bandsNum + 1) - textWidth) / 2 + measuredWidth / (bandsNum + 1) / 2
            val baselineY = cy + (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom
            canvas.drawText(text, x, baselineY, freqPaint)
        }
    }

    private fun formatHz(freq: Int): String {
        return if (freq > 1000) {
            (freq / 1000).toString() + "k"
        } else {
            freq.toString() + ""
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (touchable) {
            val action = event.action
            val x = event.x.toInt()
            val y = event.y.toInt()
            when (action) {
                MotionEvent.ACTION_DOWN -> {
                    index = findNodeIndex(x.toFloat(), y.toFloat())
                    if (index != 0) {
                        state = STATE_TOUCH_DOWN
                        invalidate()
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaY = y - lastY.toFloat()
                    if (index != 0) {
                        state = STATE_TOUCH_MOVE
                        points[index]!!.y += deltaY
                        if (y <= 40) {
                            points[index]!!.y = 40f
                        }
                        if (y >= measuredHeight - 40) {
                            points[index]!!.y = measuredHeight - 40.toFloat()
                        }
                        decibels[index - 1] = getDecibel(points[index]!!.y)
                        invalidate()
                    }
                }
                MotionEvent.ACTION_UP -> if (index != 0) {
                    state = STATE_TOUCH_UP
                    if (decibels[index - 1] != 0 && decibels[index - 1] != -12 && decibels[index - 1] != 12) {
                        val lastY = step * (decibels[index - 1] + 13)
                        points[index]!!.y = lastY
                        decibels[index - 1] = decibels[index - 1]
                    } else if (decibels[index - 1] == 0) {
                        points[index]!!.y = step * 13
                    }
                    invalidate()
                    onUpdateDecibelListener?.onUpdateDecibel(decibels)
                }
                else -> {
                }
            }
            lastY = y
        }
        return true
    }

    /**
     * 查出当前正在操作的是哪个结点。
     *
     * @param x
     * @param y
     * @return
     */
    private fun findNodeIndex(x: Float, y: Float): Int {
        var result = 0
        for (i in 1 until points.size) {
            if (points[i]!!.x - radius * 1.5 < x && points[i]!!.x + radius * 1.5 > x
                && points[i]!!.y - radius * 1.5 < y && points[i]!!.y + radius * 1.5 > y) {
                result = i
                break
            }
        }
        return result
    }

    /**
     * 将坐标转换为-12到12之间的数字。
     *
     * @param y
     * @return
     */
    private fun getDecibel(y: Float): Int {
        return when (y) {
            height - 40.toFloat() -> {
                12
            }
            40f -> {
                -12
            }
            else -> {
                ((y - 40) / step - 12).roundToInt()
            }
        }
    }

    init {
        initAttrs(context, attrs)
        initPaints()
    }
}