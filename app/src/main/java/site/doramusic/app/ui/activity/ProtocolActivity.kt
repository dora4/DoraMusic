package site.doramusic.app.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import com.alibaba.android.arouter.facade.annotation.Route
import dora.skin.SkinManager
import dora.util.StatusBarUtils
import site.doramusic.app.R
import site.doramusic.app.base.conf.ARoutePath
import site.doramusic.app.base.conf.AppConfig.Companion.COLOR_THEME
import site.doramusic.app.databinding.ActivityProtocolBinding

/**
 * 用户协议和隐私政策。
 */
@Route(path = ARoutePath.ACTIVITY_PROTOCOL)
class ProtocolActivity : BaseSkinActivity<ActivityProtocolBinding>() {

    private var webView: WebView? = null
    private var title: String? = null

    override fun onGetExtras(action: String?, bundle: Bundle?, intent: Intent) {
        title = intent.getStringExtra("title")
    }

    override fun onDestroy() {
        super.onDestroy()
        mBinding.webViewContainer.removeAllViews()
        webView?.destroy()
    }

    override fun onSetStatusBar() {
        super.onSetStatusBar()
        StatusBarUtils.setTransparencyStatusBar(this)
    }

    override fun initData(savedInstanceState: Bundle?, binding: ActivityProtocolBinding) {
        binding.statusbarPrivacyPolicy.layoutParams = LinearLayout
            .LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, StatusBarUtils.getStatusBarHeight())
        SkinManager.getLoader().setBackgroundColor(binding.statusbarPrivacyPolicy, COLOR_THEME)
        webView = WebView(applicationContext)
        val params = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        webView?.setOnLongClickListener { true }
        webView?.layoutParams = params
        webView?.webViewClient = WebViewClient()
        // 动态添加WebView，解决在xml引用WebView持有Activity的Context对象，导致内存泄露
        binding.webViewContainer.addView(webView)
        binding.titlebarPrivacyPolicy.title = title.toString()
        if (title.equals(getString(R.string.user_agreement_title))) {
            webView?.loadUrl("file:///android_asset/user_agreement.html")
        } else if (title.equals(getString(R.string.privacy_policy_title))) {
            webView?.loadUrl("file:///android_asset/privacy_policy.html")
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_protocol
    }
}