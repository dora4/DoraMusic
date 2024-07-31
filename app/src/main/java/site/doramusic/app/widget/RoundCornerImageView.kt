package site.doramusic.app.widget

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.max

class RoundCornerImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null,
                                                     defStyleAttr: Int = 0) : AppCompatImageView(context, attrs, defStyleAttr) {
    // 圆角大小，默认为10
    private val borderRadius = 10

    private val paint: Paint = Paint()

    // 3x3 矩阵，主要用于缩小放大
    private val scaleMatrix: Matrix = Matrix()

    // 渲染图像，使用图像为绘制图形着色
    private var bitmapShader: BitmapShader? = null

    init {
        paint.isAntiAlias = true
    }

    override fun onDraw(canvas: Canvas) {
        if (drawable == null) {
            return
        }
        val bitmap = drawableToBitmap(drawable)
        bitmapShader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        var scale = 1.0f
        if (!(bitmap.width == width && bitmap.height == height)) {
            // 如果图片的宽或者高与view的宽高不匹配，计算出需要缩放的比例；缩放后的图片的宽高，一定要大于我们view的宽高；所以我们这里取大值；
            scale = max(width * 1.0f / bitmap.width,
                    height * 1.0f / bitmap.height)
        }
        // shader的变换矩阵，我们这里主要用于放大或者缩小
        scaleMatrix.setScale(scale, scale)
        // 设置变换矩阵
        bitmapShader!!.setLocalMatrix(scaleMatrix)
        // 设置shader
        paint.shader = bitmapShader
        canvas.drawRoundRect(RectF(0f, 0f, width.toFloat(), height.toFloat()),
                borderRadius.toFloat(), borderRadius.toFloat(), paint)
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }
        // 当设置不为图片，为颜色时，获取的drawable宽高会有问题，所有当为颜色时候获取控件的宽高
        val w = if (drawable.intrinsicWidth <= 0) width else drawable.intrinsicWidth
        val h = if (drawable.intrinsicHeight <= 0) height else drawable.intrinsicHeight
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, w, h)
        drawable.draw(canvas)
        return bitmap
    }
}
