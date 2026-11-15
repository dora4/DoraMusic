package site.doramusic.app.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import com.alibaba.android.arouter.facade.annotation.Route
import dora.util.StatusBarUtils
import site.doramusic.app.R
import site.doramusic.app.conf.ARoutePath
import site.doramusic.app.conf.AppConfig.Companion.EXTRA_TITLE
import site.doramusic.app.databinding.ActivityProtocolBinding
import site.doramusic.app.util.PrefsManager
import site.doramusic.app.util.ThemeSelector

/**
 * 用户协议和隐私政策。
 */
@Route(path = ARoutePath.ACTIVITY_PROTOCOL)
class ProtocolActivity : BaseSkinActivity<ActivityProtocolBinding>() {

    private var webView: WebView? = null
    private var title: String? = null
    private lateinit var prefsManager: PrefsManager

    override fun onGetExtras(action: String?, bundle: Bundle?, intent: Intent) {
        title = intent.getStringExtra(EXTRA_TITLE)
    }

    override fun onDestroy() {
        super.onDestroy()
        mBinding.webViewContainer.removeAllViews()
        webView?.destroy()
    }

    override fun onSetStatusBar() {
        StatusBarUtils.setTransparencyStatusBar(this)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun initData(savedInstanceState: Bundle?, binding: ActivityProtocolBinding) {
        binding.statusbarPrivacyPolicy.layoutParams = LinearLayout
            .LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, StatusBarUtils.getStatusBarHeight())
        ThemeSelector.applyViewTheme(binding.statusbarPrivacyPolicy)
        ThemeSelector.applyViewTheme(binding.titlebarPrivacyPolicy)
        prefsManager = PrefsManager(this)
        webView = WebView(applicationContext)
        val params = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        webView?.setOnLongClickListener { true }
        webView?.layoutParams = params
        val webSettings = webView?.settings
        webSettings?.javaScriptEnabled = true
        webSettings?.domStorageEnabled = true
        webSettings?.allowFileAccess = true
        webSettings?.mediaPlaybackRequiresUserGesture = false
        // 禁止用户缩放
        webSettings?.setSupportZoom(false)
        webSettings?.builtInZoomControls = false
        webSettings?.displayZoomControls = false
        webView?.addJavascriptInterface(JsBridge(), "Android")
        webView?.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // 页面加载完成，把App保存的深色状态传给html
                val dark = prefsManager.getDarkMode()
                webView?.evaluateJavascript("javascript:setDarkMode($dark)", null)
            }
        }
        // 动态添加WebView，解决在xml引用WebView持有Activity的Context对象，导致内存泄露
        binding.webViewContainer.addView(webView)
        binding.titlebarPrivacyPolicy.title = title.toString()
        val dark = prefsManager.getDarkMode()
        binding.titlebarPrivacyPolicy.title = title.toString()
        val base = when (title) {
            getString(R.string.user_agreement_title) -> "file:///android_asset/user_agreement.html"
            getString(R.string.privacy_policy_title) -> "file:///android_asset/privacy_policy.html"
            else -> null
        }
        base?.let {
            val separator = if (it.contains("?")) "&" else "?"
            webView?.loadUrl("$it${separator}dark=$dark")
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_protocol
    }

    inner class JsBridge {

        @JavascriptInterface
        fun isDark(): Boolean {
            return prefsManager.getDarkMode()
        }
    }
}