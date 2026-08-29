package site.doramusic.app.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.SlidingDrawer
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toDrawable

/**
 * 播放控制界面滑动容器。
 */
class SlidingView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null,
                                            defStyleAttr: Int = 0) : SlidingDrawer(context, attrs, defStyleAttr) {

    private var handleId = 0
    private var touchableIds: IntArray? = null
    private var onSlidingViewClickListener: OnSlidingViewClickListener? = null

    fun setOnSlidingViewClickListener(listener: OnSlidingViewClickListener) {
        onSlidingViewClickListener = listener
    }

    interface OnSlidingViewClickListener {
        fun onClick(view: View)
    }

    /**
     * 设置 handle + content 整体 Bitmap 背景。
     *
     * Bitmap 按整体区域进行 CENTER_CROP，
     * 再分别裁剪给 handle 和 content。
     */
    fun setHandleContentBackground(bitmap: Bitmap) {
        if (handle.width <= 0 ||
            handle.height <= 0 ||
            content.width <= 0 ||
            content.height <= 0
        ) {
            post {
                setHandleContentBackground(bitmap)
            }
            return
        }
        val handleWidth = handle.width
        val handleHeight = handle.height
        val contentWidth = content.width
        val contentHeight = content.height
        val totalWidth = maxOf(
            handleWidth,
            contentWidth
        )
        val totalHeight = handleHeight + contentHeight
        val fullBitmap = createBitmap(totalWidth, totalHeight)
        val canvas = Canvas(fullBitmap)
        val scale = maxOf(
            totalWidth.toFloat() / bitmap.width.toFloat(),
            totalHeight.toFloat() / bitmap.height.toFloat()
        )
        val scaledWidth = bitmap.width * scale
        val scaledHeight = bitmap.height * scale
        val left = (totalWidth - scaledWidth) / 2f
        val top = (totalHeight - scaledHeight) / 2f
        val matrix = Matrix()
        matrix.setScale(scale, scale)
        matrix.postTranslate(left, top)
        canvas.drawBitmap(
            bitmap,
            matrix,
            null
        )
        val handleBitmap = Bitmap.createBitmap(
            fullBitmap,
            0,
            0,
            handleWidth,
            handleHeight
        )
        val contentBitmap = Bitmap.createBitmap(
            fullBitmap,
            0,
            handleHeight,
            contentWidth,
            contentHeight
        )
        handle.background = handleBitmap.toDrawable(resources)
        content.background = contentBitmap.toDrawable(resources)
        if (!fullBitmap.isRecycled) {
            fullBitmap.recycle()
        }
    }

    /**
     * 设置 handle + content 整体 Drawable 背景。
     */
    fun setHandleContentBackground(drawable: Drawable) {
        val bitmap = drawableToBitmap(drawable)
        setHandleContentBackground(bitmap)
    }

    /**
     * 设置 handle + content 整体资源背景。
     */
    fun setHandleContentBackground(resId: Int) {
        val drawable =
            ContextCompat.getDrawable(
                context,
                resId
            ) ?: return
        setHandleContentBackground(drawable)
    }

    /**
     * Drawable 转 Bitmap。
     */
    private fun drawableToBitmap(
        drawable: Drawable
    ): Bitmap {
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }
        val width =
            if (drawable.intrinsicWidth > 0) {
                drawable.intrinsicWidth
            } else {
                1
            }
        val height =
            if (drawable.intrinsicHeight > 0) {
                drawable.intrinsicHeight
            } else {
                1
            }
        val bitmap = createBitmap(width, height)
        val canvas = Canvas(bitmap)
        drawable.setBounds(
            0,
            0,
            width,
            height
        )
        drawable.draw(canvas)
        return bitmap
    }

    private fun getRectOnScreen(view: View): Rect {
        val rect = Rect()
        val location = IntArray(2)
        var parent = view
        if (view.parent is View) {
            parent = view.parent as View
        }
        parent.getLocationOnScreen(location)
        view.getHitRect(rect)
        rect.offset(location[0], location[1])
        return rect
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        val location = IntArray(2)
        var x = event.x.toInt()
        var y = event.y.toInt()
        getLocationOnScreen(location)
        x += location[0]
        y += location[1]
        if (touchableIds != null) {
            for (id in touchableIds!!) {
                val view = findViewById<View>(id)
                if (view.isShown) {
                    val rect = getRectOnScreen(view)
                    if (rect.contains(x, y)) {
                        if (event.action == MotionEvent.ACTION_DOWN) {
                            onSlidingViewClickListener?.onClick(view)
                        }
                        return true
                    }
                }
            }
        }
        // 抽屉行为控件
        if (event.action == MotionEvent.ACTION_DOWN && handleId != 0) {
            val view = findViewById<View>(handleId)
            val rect = getRectOnScreen(view)
            return if (rect.contains(x, y)) { // 点击抽屉控件时交由系统处理
                super.dispatchTouchEvent(event)
            } else {
                false
            }
        }
        return super.dispatchTouchEvent(event)
    }
}