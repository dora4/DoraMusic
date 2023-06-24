package site.doramusic.app.ui.dialog

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView

import com.lwh.jackknife.dialog.BaseDialog
import dora.util.ScreenUtils

import site.doramusic.app.R

class ConfirmScanDialog(context: Context) : BaseDialog(context) {

    private var onConfirmScanListener: OnConfirmScanListener? = null

    interface OnConfirmScanListener {
        fun onScan()
    }

    fun setOnConfirmScanListener(l: OnConfirmScanListener) {
        this.onConfirmScanListener = l
    }

    override fun title(): String {
        return "系统消息"
    }

    override fun positiveLabel(): String {
        return "确认"
    }

    override fun negativeLabel(): String {
        return "取消"
    }

    private fun init() {
        val context = context
        val dialogLayout = LinearLayout(context)
        dialogLayout.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        dialogLayout.orientation = LinearLayout.VERTICAL
        dialogLayout.minimumWidth = dp2px(200f)
        dialogLayout.setBackgroundColor(Color.WHITE)
        val topLayout = LinearLayout(context)
        topLayout.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp2px(45f))
        topLayout.gravity = Gravity.CENTER_VERTICAL
        val titleTextView = TextView(context)
        titleTextView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        titleTextView.text = title
        titleTextView.setTextColor(-0xff3482)
        titleTextView.textSize = 18f
        titleTextView.ellipsize = TextUtils.TruncateAt.END
        titleTextView.setPadding(dp2px(10f), 0, dp2px(10f), 0)
        titleTextView.text = title()
        topLayout.addView(titleTextView)
        dialogLayout.addView(topLayout)
        val view = View(context)
        view.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp2px(0.2f))
        view.setBackgroundColor(-0x353536)
        if (isHasTitle) {
            topLayout.visibility = View.VISIBLE
            view.visibility = View.VISIBLE
        } else {
            topLayout.visibility = View.GONE
            view.visibility = View.GONE
        }
        val layoutContainer = RelativeLayout(context)
        layoutContainer.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        val messageTextView = TextView(context)
        messageTextView.text = message
        messageTextView.setTextColor(Color.GRAY)
        if (mView != null) {
            layoutContainer.addView(mView)
        } else {
            val params = RelativeLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            params.topMargin = dp2px(40f)
            params.bottomMargin = dp2px(40f)
            params.addRule(RelativeLayout.CENTER_IN_PARENT)
            layoutContainer.addView(messageTextView, params)
        }
        val bottomLayout = LinearLayout(context)
        bottomLayout.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp2px(40f))
        bottomLayout.orientation = LinearLayout.HORIZONTAL
        mPositiveButton = Button(context)
        mPositiveButton.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
        mPositiveButton.setBackgroundResource(com.lwh.jackknife.widget.R.drawable.jknf_base_dialog_bottom_button)
        mPositiveButton.setOnClickListener {
            if (onConfirmScanListener != null) {
                onConfirmScanListener!!.onScan()
            }
            dismiss()
        }
        mNegativeButton = Button(context)
        mNegativeButton.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
        mNegativeButton.setBackgroundResource(com.lwh.jackknife.widget.R.drawable.jknf_base_dialog_bottom_button)
        mNegativeButton.setOnClickListener { dismiss() }
        if (positiveLabel() != null && positiveLabel().length > 0) {
            mPositiveButton.text = positiveLabel()
            mPositiveButton.setTextColor(-0xff3482)
        } else {
            mPositiveButton.visibility = View.GONE
        }
        if (negativeLabel() != null && negativeLabel().length > 0) {
            mNegativeButton.text = negativeLabel()
        } else {
            mNegativeButton.visibility = View.GONE
        }
        val view2 = View(context)
        view2.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp2px(0.5f))
        view2.setBackgroundColor(-0x353536)
        dialogLayout.addView(view)
        val view3 = View(context)
        view3.layoutParams = LinearLayout.LayoutParams(dp2px(0.5f), LinearLayout.LayoutParams.MATCH_PARENT)
        view3.setBackgroundColor(-0x353536)
        dialogLayout.addView(layoutContainer)
        dialogLayout.addView(view2)
        bottomLayout.addView(mNegativeButton)
        bottomLayout.addView(view3)
        bottomLayout.addView(mPositiveButton)
        dialogLayout.addView(bottomLayout)
        val params = window!!.attributes
        if (width > 0)
            params.width = width
        if (height > 0)
            params.height = height
        if (x > 0)
            params.width = x
        if (y > 0)
            params.height = y
        if (isFullScreen) {
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = WindowManager.LayoutParams.MATCH_PARENT
        }
        if (isCancel) {
            setCanceledOnTouchOutside(true)
            setCancelable(true)
        } else {
            setCanceledOnTouchOutside(false)
            setCancelable(false)
        }
        window!!.attributes = params
        window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        setContentView(dialogLayout)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val view = onCreateView(this)
        setView(view)
        if (title() != null && title() != "") {
            title = title()
        }
        if (positiveLabel() != null && positiveLabel() != "") {
            positiveLabel = positiveLabel()
        }
        if (negativeLabel() != null && negativeLabel() != "") {
            negativeLabel = negativeLabel()
        }
        init()
    }

    override fun onCreateView(dialog: BaseDialog): View {
        return LayoutInflater.from(mContext).inflate(R.layout.dialog_confirm_scan, null)
    }
}
