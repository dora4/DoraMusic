package site.doramusic.app.widget

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import kotlin.jvm.JvmOverloads

class MarqueeTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AppCompatTextView(
    context, attrs, defStyle
) {
    init {
        init()
    }

    private fun init() {
        setSingleLine()
        ellipsize = TextUtils.TruncateAt.MARQUEE
    }

    override fun isFocused(): Boolean {
        return true
    }
}