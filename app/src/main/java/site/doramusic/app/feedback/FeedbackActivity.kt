package site.doramusic.app.feedback

import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatImageView

import dora.firebase.SpmUtils
import dora.http.DoraHttp.api
import dora.http.DoraHttp.net
import dora.skin.SkinManager
import dora.skin.base.BaseSkinBindingActivity
import dora.util.DensityUtils
import dora.util.LogUtils
import dora.util.NetUtils
import dora.util.StatusBarUtils
import dora.util.TextUtils
import dora.util.ViewUtils
import dora.widget.DoraRadioGroup
import dora.widget.DoraTitleBar
import dora.widget.panel.MenuPanelItemRoot
import dora.widget.panel.menu.InputMenuPanelItem
import site.doramusic.app.R
import site.doramusic.app.base.conf.AppConfig.Companion.COLOR_THEME
import site.doramusic.app.base.conf.AppConfig.Companion.PRODUCT_NAME
import site.doramusic.app.databinding.ActivityFeedbackBinding
import site.doramusic.app.http.SecureRequestBuilder

class FeedbackActivity : BaseSkinBindingActivity<ActivityFeedbackBinding>() {

    private var feedbackType = 0

    override fun getLayoutId(): Int {
        return R.layout.activity_feedback
    }

    override fun onSetStatusBar() {
        val skinThemeColor = SkinManager.getLoader().getColor(COLOR_THEME)
        StatusBarUtils.setStatusBarColor(this, skinThemeColor)
    }


    override fun initData(savedInstanceState: Bundle?, binding: ActivityFeedbackBinding) {
        val skinThemeColor = SkinManager.getLoader().getColor(COLOR_THEME)
        binding.titlebar.setBackgroundColor(skinThemeColor)
        binding.titlebar.addMenuButton(R.drawable.ic_save)
            .setOnIconClickListener(object : DoraTitleBar.OnIconClickListener {

                override fun onIconBackClick(icon: AppCompatImageView) {}

                @RequiresApi(Build.VERSION_CODES.O)
                override fun onIconMenuClick(position: Int, icon: AppCompatImageView) {
                    if (!NetUtils.checkNetworkAvailable()) {
                        showLongToast(getString(R.string.no_internet_connection))
                        return
                    }
                    SpmUtils.selectContent(this@FeedbackActivity, "提交反馈信息")
                    val etInput = binding.menuPanel.getViewByPosition(
                        0,
                        InputMenuPanelItem.ID_EDIT_TEXT_INPUT
                    ) as EditText
                    val content = ViewUtils.getText(etInput)
                    if (TextUtils.isEmpty(content)) {
                        showShortToast(getString(R.string.please_input_content))
                        return
                    }
                    showShortToast(getString(R.string.submitting_please_wait))
                    net {
                        val req = ReqFeedback(productName = PRODUCT_NAME,
                            feedbackType = feedbackType, feedbackContent = content)
                        val body = SecureRequestBuilder.build(req, SecureRequestBuilder.SecureMode.ENC)
                        if (body == null) {
                            showShortToast(getString(R.string.failed_to_feedback))
                            return@net
                        }
                        try {
                            val ok = api(FeedbackService::class) { commitFeedback(body.toRequestBody()) }?.data as Boolean
                            if (ok) {
                                showLongToast(getString(R.string.feedback_successfully))
                                finish()
                            } else {
                                showLongToast(getString(R.string.failed_to_feedback))
                            }
                        } catch (e: Exception) {
                            LogUtils.e(e.toString())
                        }
                    }
                }
            })
        binding.radioGroup.check(R.id.rb_suggestion)
        binding.radioGroup.setOnCheckedChangeListener(object : DoraRadioGroup.OnCheckedChangeListener {
            override fun onCheckedChanged(
                group: DoraRadioGroup?,
                checkedId: Int
            ) {
                when (checkedId) {
                    R.id.rb_suggestion -> feedbackType = 0
                    R.id.rb_question -> feedbackType = 1
                }
            }
        })
        binding.menuPanel.addMenu(
            InputMenuPanelItem(
                0,
                getString(R.string.feedback_title),
                MenuPanelItemRoot.Span(DensityUtils.DP10),
                getString(R.string.input_feedback_content),
                "",
                null
            )
        )
        val etInput = binding.menuPanel.getViewByPosition(0, InputMenuPanelItem.ID_EDIT_TEXT_INPUT) as EditText
        etInput.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            DensityUtils.dp2px(270f)
        )
        etInput.gravity = Gravity.TOP
        etInput.isSingleLine = false
    }
}