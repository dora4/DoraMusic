package site.doramusic.app.ui.adapter

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import site.doramusic.app.R

class BettingDialog(
    context: Context,
    private val betTitle: String,
    private val optionName: String,
    private val odds: String,
    private val onConfirm: (Long) -> Unit
) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)

        val view = LayoutInflater.from(context)
            .inflate(R.layout.dialog_bet, null)

        setContentView(view)

        window?.apply {

            // 背景透明
            setBackgroundDrawableResource(android.R.color.transparent)

            // 宽度
            setLayout(
                (context.resources.displayMetrics.widthPixels * 0.88f).toInt(),
                WindowManager.LayoutParams.WRAP_CONTENT
            )
        }

        val title = view.findViewById<TextView>(R.id.tv_title)
        val content = view.findViewById<TextView>(R.id.tv_content)
        val oddsTv = view.findViewById<TextView>(R.id.tv_odds)
        val input = view.findViewById<EditText>(R.id.et_amount)
        val confirm = view.findViewById<TextView>(R.id.btn_confirm)

        title.text = betTitle
        content.text = "我的选择：$optionName"
        oddsTv.text = "赔率：$odds"

        confirm.setOnClickListener {

            val amount = input.text.toString()
                .toLongOrNull() ?: 0L

            if (amount > 0) {
                onConfirm(amount)
                dismiss()
            }
        }
    }
}