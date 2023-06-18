package site.doramusic.app.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import site.doramusic.app.R

class TitleBar @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null,
                                         defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {

    var titleView: TextView? = null
        private set
    private var backView: TextView? = null
    private var menuView: TextView? = null
    private var title: String? = null
    private var back: String? = null
    private var menu: String? = null
    private var backIcon: Drawable? = null
    private var menuIcon: Drawable? = null
    private var menuBg: Drawable? = null
    private var noBack: Boolean = false

    init {
        init(attrs, defStyleAttr)
    }

    private fun init(attrs: AttributeSet?, defStyleAttr: Int) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.TitleBar, defStyleAttr, 0)
        val dp6 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6f, resources.displayMetrics).toInt()
        val dp8 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, resources.displayMetrics).toInt()
        val dp10 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10f, resources.displayMetrics).toInt()
        title = a.getString(R.styleable.TitleBar_tb_title)
        back = a.getString(R.styleable.TitleBar_tb_backName)
        menu = a.getString(R.styleable.TitleBar_tb_menuName)
        noBack = a.getBoolean(R.styleable.TitleBar_tb_noBack, false)
        if (a.hasValue(R.styleable.TitleBar_tb_backIcon)) {
            backIcon = a.getDrawable(R.styleable.TitleBar_tb_backIcon)
        }
        if (a.hasValue(R.styleable.TitleBar_tb_menuIcon)) {
            menuIcon = a.getDrawable(R.styleable.TitleBar_tb_menuIcon)
        }
        if (a.hasValue(R.styleable.TitleBar_tb_menuBg)) {
            menuBg = a.getDrawable(R.styleable.TitleBar_tb_menuBg)
        }
        a.recycle()
        if (backIcon == null) {
            backIcon = ContextCompat.getDrawable(context, com.lwh.jackknife.widget.R.drawable.jknf_title_bar_back)
        }
        View.inflate(context, com.lwh.jackknife.widget.R.layout.jknf_title_bar, this)
        titleView = findViewById(com.lwh.jackknife.widget.R.id.tv_titlebar_title)
        backView = findViewById(com.lwh.jackknife.widget.R.id.tv_titlebar_back)
        menuView = findViewById(com.lwh.jackknife.widget.R.id.tv_titlebar_menu)
        backView!!.visibility = if (noBack) View.GONE else View.VISIBLE
        titleView!!.text = title
        backView!!.text = back
        menuView!!.text = menu
        if (menuIcon != null) {
            menuView!!.setCompoundDrawables(menuIcon, null, null, null)
            menuView!!.compoundDrawablePadding = 10
        }
        if (menuBg != null) {
            menuView!!.background = menuBg
            menuView!!.setPadding(dp8, dp6, dp8, dp6)
            val params = menuView!!.layoutParams as RelativeLayout.LayoutParams
            params.rightMargin = dp10
            menuView!!.layoutParams = params
        }
        backView!!.setCompoundDrawables(backIcon, null, null, null)
        backView!!.compoundDrawablePadding = 10
    }

    fun setTitle(title: String) {
        titleView!!.text = title
    }

    fun setBack(back: String) {
        backView!!.text = back
    }

    fun setMenu(menu: String) {
        menuView!!.text = menu
    }

    fun setOnBackListener(listener: View.OnClickListener) {
        backView!!.setOnClickListener(listener)
    }

    fun setOnMenuListener(listener: View.OnClickListener) {
        menuView!!.setOnClickListener(listener)
    }

    fun showMenu(show: Boolean) {
        menuView!!.visibility = if (show) View.VISIBLE else View.GONE
    }
}
