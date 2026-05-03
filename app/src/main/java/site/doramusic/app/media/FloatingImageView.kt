package site.doramusic.app.media

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.hypot

class FloatingImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : AppCompatImageView(context, attrs) {

    private var downX = 0f
    private var downY = 0f
    private var isDragging = false
    private var touchSlop: Int = 10

    var onClick: ((View) -> Unit)? = null

    override fun onFinishInflate() {
        touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.rawX
                downY = event.rawY
                isDragging = false
                parent.requestDisallowInterceptTouchEvent(true)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = event.rawX - downX
                val dy = event.rawY - downY
                if (!isDragging && hypot(dx, dy) > touchSlop) {
                    isDragging = true
                    parent.requestDisallowInterceptTouchEvent(false)
                    return false
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (!isDragging) {
                    performClick()
                    onClick?.invoke(this)
                }
                return true
            }
            MotionEvent.ACTION_CANCEL -> {
                isDragging = false
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}