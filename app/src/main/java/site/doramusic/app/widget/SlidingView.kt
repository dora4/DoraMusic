package site.doramusic.app.widget

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.SlidingDrawer

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
        fun onClick(view: View?)
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