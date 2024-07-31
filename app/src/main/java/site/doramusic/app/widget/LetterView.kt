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

class LetterView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var paint: TextPaint? = null
    private var mTextSize = 0f
    private var mTextColor = 0
    private var mHoverTextColor = 0
    private val mBackgroundDrawable: Drawable
    var hoverBackgroundDrawable: Drawable? = null
        private set
    private var letters = arrayOf<String?>(
        "A", "B", "C", "D", "E", "F", "G", "H", "I",
        "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
        "W", "X", "Y", "Z", "#"
    )
    private var textAllCaps = false
    private var selected = -1
    private val metrics: DisplayMetrics
    private val locale: Locale
    private var onLetterChangeListener: OnLetterChangeListener? = null

    init {
        metrics = resources.displayMetrics
        locale = resources.configuration.locale
        mBackgroundDrawable = background
        initAttrs(context, attrs, defStyleAttr)
        initPaint()
    }

    private fun initAttrs(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.LetterView, defStyleAttr, 0)
        letters = parseLetters(a.getString(R.styleable.LetterView_letterview_letters))
        mTextSize = a.getDimension(
            R.styleable.LetterView_letterview_textSize,
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 15f, metrics)
        )
        mTextColor = a.getColor(R.styleable.LetterView_letterview_textColor, DEFAULT_TEXT_COLOR)
        mHoverTextColor = a
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
                for (i in 0 until letters.length) {
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
        paint!!.textSize = mTextSize
        paint!!.color = mTextColor
        paint!!.typeface = Typeface.DEFAULT
        paint!!.isAntiAlias = true
        paint!!.isDither = true
    }

    override fun onDraw(canvas: Canvas) {
        if (letters.size > 0) {
            val width = width
            val height = height
            val singleHeight = height / letters.size
            for (i in letters.indices) {
                if (i == selected) {
                    paint!!.typeface = Typeface.DEFAULT_BOLD
                    paint!!.color = mHoverTextColor
                } else {
                    paint!!.typeface = Typeface.DEFAULT
                    paint!!.color = mTextColor
                }
                val x = width / 2 - paint!!.measureText(letters[i]) / 2
                val y = (singleHeight * (i + 1)).toFloat()
                canvas.drawText(letters[i]!!, x, y, paint!!)
            }
        }
    }

    fun setLetters(letters: Array<String?>) {
        if (letters != this.letters) {
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
        get() = mTextSize
        set(size) {
            if (size != mTextSize) {
                mTextSize = size
                invalidateView()
            }
        }
    var textColor: Int
        get() = mTextColor
        set(color) {
            if (color != mTextColor) {
                mTextColor = color
                invalidateView()
            }
        }
    var hoverTextColor: Int
        get() = mHoverTextColor
        set(color) {
            if (color != mHoverTextColor) {
                mHoverTextColor = color
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

    fun setOnLetterChangeListener(l: OnLetterChangeListener?) {
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
                setBackgroundDrawable(mBackgroundDrawable)
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