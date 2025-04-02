package site.doramusic.app.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Looper
import android.text.TextPaint
import android.text.TextUtils
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import site.doramusic.app.R
import java.util.Locale

/**
 * 字母导航控件。
 */
class LetterView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var paint: TextPaint? = null
    private var letterTextSize = 0f
    private var letterTextColor = 0
    private var letterHoverTextColor = 0
    private val backgroundDrawable: Drawable = background
    private var hoverBackgroundDrawable: Drawable? = null
    private var letters = arrayOf<String?>(
        "A", "B", "C", "D", "E", "F", "G", "H", "I",
        "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
        "W", "X", "Y", "Z", "#"
    )
    private var textAllCaps = false
    private var selected = -1
    private val metrics: DisplayMetrics = resources.displayMetrics
    private val locale: Locale = resources.configuration.locale
    private var onLetterChangeListener: OnLetterChangeListener? = null

    init {
        initAttrs(context, attrs, defStyleAttr)
        initPaint()
    }

    private fun initAttrs(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.LetterView, defStyleAttr, 0)
        letters = parseLetters(a.getString(R.styleable.LetterView_letterview_letters))
        letterTextSize = a.getDimension(
            R.styleable.LetterView_letterview_textSize,
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 15f, metrics)
        )
        letterTextColor = a.getColor(R.styleable.LetterView_letterview_textColor, DEFAULT_TEXT_COLOR)
        letterHoverTextColor = a
            .getColor(R.styleable.LetterView_letterview_hoverTextColor, DEFAULT_HOVER_TEXT_COLOR)
        hoverBackgroundDrawable =
            a.getDrawable(R.styleable.LetterView_letterview_hoverBackgroundDrawable)
        textAllCaps = a.getBoolean(R.styleable.LetterView_letterview_textAllCaps, false)
        a.recycle()
    }

    private fun parseLetters(letters: String?): Array<String?> {
        val values: Array<String?>
        if (letters == null) {
            values = this.letters
        } else {
            val length = letters.length
            values = arrayOfNulls(length)
            if (!TextUtils.isEmpty(letters)) {
                for (i in letters.indices) {
                    var letter = letters[i].toString()
                    if (textAllCaps) {
                        letter = letter.uppercase(locale)
                    }
                    values[i] = letter
                }
            }
        }
        return values
    }

    private fun initPaint() {
        paint = TextPaint()
        paint!!.textSize = letterTextSize
        paint!!.color = letterTextColor
        paint!!.typeface = Typeface.DEFAULT
        paint!!.isAntiAlias = true
        paint!!.isDither = true
    }

    override fun onDraw(canvas: Canvas) {
        if (letters.isNotEmpty()) {
            val width = width
            val height = height
            val singleHeight = height / letters.size
            for (i in letters.indices) {
                if (i == selected) {
                    paint!!.typeface = Typeface.DEFAULT_BOLD
                    paint!!.color = letterHoverTextColor
                } else {
                    paint!!.typeface = Typeface.DEFAULT
                    paint!!.color = letterTextColor
                }
                val x = width / 2 - paint!!.measureText(letters[i]) / 2
                val y = (singleHeight * (i + 1)).toFloat()
                canvas.drawText(letters[i]!!, x, y, paint!!)
            }
        }
    }

    fun setLetters(letters: Array<String?>) {
        if (!letters.contentEquals(this.letters)) {
            this.letters = letters
            invalidateView()
        }
    }

    fun setTextColorResource(resId: Int) {
        textColor = resources.getColor(resId)
    }

    fun setHoverTextColorResource(resId: Int) {
        hoverTextColor = resources.getColor(resId)
    }

    fun setHoverBackgroundDrawable(drawable: Drawable) {
        if (drawable.constantState != hoverBackgroundDrawable!!.constantState) {
            hoverBackgroundDrawable = drawable
            invalidateView()
        }
    }

    var textSize: Float
        get() = letterTextSize
        set(size) {
            if (size != letterTextSize) {
                letterTextSize = size
                invalidateView()
            }
        }
    var textColor: Int
        get() = letterTextColor
        set(color) {
            if (color != letterTextColor) {
                letterTextColor = color
                invalidateView()
            }
        }
    var hoverTextColor: Int
        get() = letterHoverTextColor
        set(color) {
            if (color != letterHoverTextColor) {
                letterHoverTextColor = color
                invalidateView()
            }
        }
    var isTextAllCaps: Boolean
        get() = textAllCaps
        set(caps) {
            if (caps != textAllCaps) {
                textAllCaps = caps
                invalidateView()
            }
        }

    fun setOnLetterChangeListener(l: OnLetterChangeListener) {
        onLetterChangeListener = l
    }

    interface OnLetterChangeListener {
        fun onChanged(letter: String)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val measuredWidth = measureWidth(widthMeasureSpec)
        setMeasuredDimension(measuredWidth, MeasureSpec.getSize(heightMeasureSpec))
    }

    private fun measureWidth(widthMeasureSpec: Int): Int {
        return if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.AT_MOST) {
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                25f,
                metrics
            ).toInt()
        } else MeasureSpec.getSize(widthMeasureSpec)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.action
        val height = height
        val y = event.y
        val index = (y * letters.size / height).toInt()
        when (action) {
            MotionEvent.ACTION_UP -> {
                setBackgroundDrawable(backgroundDrawable)
                selected = -1
                invalidateView()
            }

            else -> {
                setBackgroundDrawable(hoverBackgroundDrawable)
                if (index < letters.size && index >= 0) {
                    selected = index
                    invalidateView()
                    if (onLetterChangeListener != null) {
                        letters[index]?.let { onLetterChangeListener!!.onChanged(it) }
                    }
                }
            }
        }
        return true
    }

    /**
     * The refresh view operation of the automatic processing thread.
     */
    private fun invalidateView() {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            invalidate()
        } else {
            postInvalidate()
        }
    }

    companion object{
        private const val DEFAULT_TEXT_COLOR = -0x1000000
        private const val DEFAULT_HOVER_TEXT_COLOR = -0x1000000
    }
}