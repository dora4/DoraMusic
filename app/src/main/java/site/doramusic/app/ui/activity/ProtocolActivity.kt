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
import java.util.Locale

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

    /**
     * 语言后缀：命中支持的语言返回 "-xx"，未命中返回 ""（英文兜底）。
     * 中文区分简繁：zh-CN / zh-SG 走 "-zh"，zh-TW / zh-HK / zh-MO 走 "-zhtw"。
     */
    private fun localeSuffix(): String {
        val locale = Locale.getDefault()
        val lang = locale.language
        if (lang.equals("zh", ignoreCase = true)) {
            val region = locale.country.uppercase(Locale.US)
            return if (region == "TW" || region == "HK" || region == "MO") "-zhtw" else "-zh"
        }
        return when {
            lang.startsWith("de", true) -> "-de"
            lang.startsWith("fr", true) -> "-fr"
            lang.startsWith("ja", true) -> "-ja"
            lang.startsWith("ko", true) -> "-ko"
            lang.startsWith("ru", true) -> "-ru"
            lang.startsWith("es", true) -> "-es"
            lang.startsWith("pt", true) -> "-pt"
            lang.startsWith("id", true) -> "-id"
            lang.startsWith("ms", true) -> "-ms"
            lang.startsWith("th", true) -> "-th"
            lang.startsWith("vi", true) -> "-vi"
            else -> ""
        }
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
        val suffix = localeSuffix()
        val base = when (title) {
            getString(R.string.user_agreement_title) ->
                "file:///android_asset/user_agreement$suffix.html"
            getString(R.string.privacy_policy_title) ->
                "file:///android_asset/privacy_policy$suffix.html"
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