package dora.widget

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.AttributeSet
import android.view.animation.LinearInterpolator
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatImageView
import dora.util.DensityUtils
import dora.util.ImageUtils
import dora.util.ViewUtils

class DoraRotateCoverView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AppCompatImageView(context, attrs, defStyle) {

    private var rotateAnimator: ObjectAnimator? = null
    private var rotateDuration = 20000
    var borderColor = DEFAULT_BORDER_COLOR
    var borderWidth = DEFAULT_BORDER_WIDTH
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val coverPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderRect = RectF()
    private val coverRect = RectF()
    private var borderRadius = 0f
    private var coverRadius = 0f
    private var coverBitmap: Bitmap? = null
    private var bitmapShader: BitmapShader? = null
    var coverWidth = 0
        private set
    var coverHeight = 0
        private set

    private fun init() {
        coverPaint.color = Color.BLUE
        borderPaint.style = Paint.Style.STROKE
        borderPaint.color = borderColor
        borderPaint.strokeWidth = borderWidth.toFloat()
        borderRect[0f, 0f, width.toFloat()] = height.toFloat()
        borderRadius = DensityUtils.dp2px(200f).toFloat()
        if (coverBitmap != null) {
            val dp100 = DensityUtils.dp2px(100f)
            val dp200 = DensityUtils.dp2px(200f)
            val dp400 = DensityUtils.dp2px(400f)
            coverWidth = dp400
            coverHeight = dp400
            coverRect.set(borderRect)
            coverRadius = (coverRect.width() / 2).coerceAtMost(coverRect.height() / 2)
            coverBitmap = ImageUtils.decodeSampledBitmap(resources, site.doramusic.app.R.drawable.cover_rotating_bg,
                dp400, dp400)
//            if (coverRadius > 0f && coverWidth > coverRect.width() && coverHeight > coverRect.height()) {
//                coverBitmap = Bitmap.createScaledBitmap(
//                    coverBitmap!!,
//                    coverRadius.toInt() * 2,
//                    coverRadius.toInt() * 2,
//                    true
//                )
//            }
            bitmapShader = BitmapShader(coverBitmap!!, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
            coverPaint.shader = bitmapShader
        }
    }

    fun setRotateDuration(duration: Int) {
        if (duration >= 5) {
            rotateDuration = duration
        } else {
            throw IllegalArgumentException("so fast not supported.")
        }
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val measuredWidth = ViewUtils.applyWrapContentSize(widthMeasureSpec, DensityUtils.dp2px(200f))
        val measuredHeight = ViewUtils.applyWrapContentSize(widthMeasureSpec, DensityUtils.dp2px(200f))
        setMeasuredDimension(measuredWidth, measuredHeight)
    }
    override fun getScaleType(): ScaleType {
        return SCALE_TYPE
    }

    override fun setScaleType(scaleType: ScaleType) {
        require(scaleType == SCALE_TYPE) {
            String.format(
                "ScaleType %s not supported.",
                scaleType
            )
        }
    }

    override fun setAdjustViewBounds(adjustViewBounds: Boolean) {
        require(!adjustViewBounds) { "adjustViewBounds not supported." }
    }

    private fun getBitmapFromDrawable(drawable: Drawable?): Bitmap? {
        if (drawable == null) {
            return null
        }
        return if (drawable is BitmapDrawable) {
            drawable.bitmap
        } else try {
            val bitmap = if (drawable is ColorDrawable) {
                Bitmap.createBitmap(
                    COLOR_DRAWABLE_DIMENSION,
                    COLOR_DRAWABLE_DIMENSION,
                    BITMAP_CONFIG
                )
            } else {
                Bitmap.createBitmap(
                    drawable.intrinsicWidth,
                    drawable.intrinsicHeight,
                    BITMAP_CONFIG
                )
            }
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun setImageBitmap(bm: Bitmap) {
        super.setImageBitmap(bm)
        coverBitmap = bm
        init()
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        coverBitmap = getBitmapFromDrawable(drawable)
        init()
    }

    override fun setImageResource(@DrawableRes resId: Int) {
        super.setImageResource(resId)
        coverBitmap = getBitmapFromDrawable(drawable)
        init()
    }

    override fun setImageURI(uri: Uri?) {
        super.setImageURI(uri)
        coverBitmap = if (uri != null) getBitmapFromDrawable(drawable) else null
        init()
    }

    override fun onDraw(canvas: Canvas) {
        if (coverBitmap != null) {
            canvas.drawCircle(coverRect.centerX(), coverRect.centerY(), coverRadius, coverPaint)
        }
        if (borderRadius > 0) {
            canvas.drawCircle(borderRect.centerX(), borderRect.centerY(), borderRadius, borderPaint)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        init()
    }

    fun start(@DrawableRes drawableResId: Int, smart: Boolean) {
        if (smart) {
            if (rotateAnimator != null && !rotateAnimator!!.isRunning) {
                resume()
            } else {
                start(drawableResId)
            }
        } else {
            start(drawableResId)
        }
    }

    fun start(@DrawableRes drawableResId: Int) {
        setImageResource(drawableResId)
        rotateAnimator = ObjectAnimator.ofFloat(this, "rotation", 0f, 360f)
        rotateAnimator!!.interpolator = LinearInterpolator()
        rotateAnimator!!.duration = rotateDuration.toLong()
        rotateAnimator!!.repeatMode = ValueAnimator.RESTART
        rotateAnimator!!.repeatCount = ValueAnimator.INFINITE
        rotateAnimator!!.setupStartValues()
        rotateAnimator!!.start()
    }

    fun stop() {
        if (rotateAnimator != null && rotateAnimator!!.isRunning) {
            rotateAnimator!!.cancel()
            rotateAnimator = null
        }
    }

    fun pause() {
        if (rotateAnimator != null && rotateAnimator!!.isRunning) {
            rotateAnimator!!.pause()
        }
    }

    fun resume() {
        if (rotateAnimator != null && !rotateAnimator!!.isRunning) {
            rotateAnimator!!.resume()
        }
    }

    companion object {
        private val SCALE_TYPE = ScaleType.CENTER_CROP
        private val BITMAP_CONFIG = Bitmap.Config.ARGB_8888
        private const val COLOR_DRAWABLE_DIMENSION = 1
        private var DEFAULT_BORDER_WIDTH = DensityUtils.dp2px(2f)
        private const val DEFAULT_BORDER_COLOR = Color.BLACK
    }

    init {
        init()
    }
}