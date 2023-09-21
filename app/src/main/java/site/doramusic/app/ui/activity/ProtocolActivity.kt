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
        webView!!.destroy()
    }

    override fun onSetStatusBar() {
        super.onSetStatusBar()
        StatusBarUtils.setTransparencyStatusBar(this)
    }

    override fun initData(savedInstanceState: Bundle?, binding: ActivityProtocolBinding) {
        mBinding.statusbarPrivacyPolicy.layoutParams = LinearLayout
            .LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, StatusBarUtils.getStatusBarHeight())
        SkinManager.getLoader().setBackgroundColor(mBinding.statusbarPrivacyPolicy, "skin_theme_color")
        webView = WebView(applicationContext)
        val params = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        webView!!.setOnLongClickListener { true }
        webView!!.layoutParams = params
        webView!!.webViewClient = WebViewClient()
        // 动态添加WebView，解决在xml引用WebView持有Activity的Context对象，导致内存泄露
        mBinding.webViewContainer.addView(webView)
        mBinding.titlebarPrivacyPolicy.title = title.toString()
        if (title.equals("用户协议")) {
            webView!!.loadUrl("file:///android_asset/user_agreement.html")
        } else if (title.equals("隐私政策")) {
            webView!!.loadUrl("file:///android_asset/privacy_policy.html")
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_protocol
    }
}