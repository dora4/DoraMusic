package site.doramusic.app.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import site.doramusic.app.R

class NicknameDialog(
    context: Context,
    private val currentNickname: String,
    private val onConfirm: (String) -> Unit
) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)

        val view = LayoutInflater.from(context)
            .inflate(R.layout.dialog_set_nickname, null)

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
        val input = view.findViewById<EditText>(R.id.et_nickname)
        val confirm = view.findViewById<TextView>(R.id.btn_confirm)

        title.text = context.getString(R.string.set_nickname)

        input.setText(currentNickname)
        input.setSelection(currentNickname.length)
        input.filters = arrayOf(InputFilter.LengthFilter(16))

        confirm.setOnClickListener {

            val nickname = input.text.toString()
                .trim()

            if (nickname.isEmpty()) {
                input.error = "昵称不能为空"
                return@setOnClickListener
            }

            onConfirm(nickname)
            dismiss()
        }
    }
}