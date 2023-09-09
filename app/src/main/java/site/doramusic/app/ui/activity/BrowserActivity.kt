package site.doramusic.app.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.widget.LinearLayout
import com.just.agentweb.AgentWeb
import com.just.agentweb.WebIndicator
import dora.BaseActivity
import dora.util.IntentUtils
import dora.util.StatusBarUtils
import site.doramusic.app.R
import site.doramusic.app.databinding.ActivityBrowserBinding
import site.doramusic.app.util.SpmUtils
import site.doramusic.app.util.SpmUtils.screenEvent

class BrowserActivity : BaseActivity<ActivityBrowserBinding>() {
    private var title = "扫码结果"
    private var url: String? = null
    private var agentWeb: AgentWeb? = null
    override fun getLayoutId(): Int {
        return R.layout.activity_browser
    }

    override fun onGetExtras(action: String?, bundle: Bundle?, intent: Intent) {
        if (IntentUtils.hasExtra(intent, "title")) {
            title = IntentUtils.getStringExtra(intent, "title")
        }
        if (IntentUtils.hasExtra(intent, "url")) {
            url = IntentUtils.getStringExtra(intent, "url")
        }
    }

    override fun onSetStatusBar() {
        StatusBarUtils.setStatusBarColorRes(this, R.color.colorPrimaryDark)
    }

    override fun initData(savedInstanceState: Bundle?) {
        screenEvent(
            this,
            SpmUtils.SPM_ID_OPEN_SCREEN_BROWSER,
            SpmUtils.SPM_NAME_SCREEN,
            SpmUtils.SPM_TYPE_SCREEN_OPEN
        )
        mBinding!!.titlebar.title = title
        val webIndicator = WebIndicator(this)
        webIndicator.setColor(resources.getColor(R.color.colorPrimary))
        agentWeb = AgentWeb.with(this)
            .setAgentWebParent(
                mBinding!!.rlBrowserWebPage,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
            .setCustomIndicator(webIndicator)
            .createAgentWeb()
            .ready()
            .go(url)
        agentWeb!!.jsInterfaceHolder.addJavaObject(
            "android",
            AndroidInterface(agentWeb, this, object : WebJsInterfaceCallback {
                override fun nativeApi(params: String?) {}
            })
        )
    }

    override fun onPointerCaptureChanged(hasCapture: Boolean) {}
    interface WebJsInterfaceCallback {
        fun nativeApi(params: String?)
    }

    inner class AndroidInterface(
        private val agent: AgentWeb?,
        private val context: Context,
        private val interfaceCallback: WebJsInterfaceCallback?
    ) {
        private val TAG = "AndroidInterfaceWeb"

        //定义h5要调用的本地方法
        @JavascriptInterface
        fun Android_BuyVip(mtdName: String?, params: String?) {
            when (mtdName) {
                "nativeApi" -> interfaceCallback?.nativeApi(params)
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return agentWeb!!.handleKeyEvent(keyCode, event)
    }

    override fun onDestroy() {
        super.onDestroy()
        screenEvent(
            this,
            SpmUtils.SPM_ID_CLOSE_SCREEN_BROWSER,
            SpmUtils.SPM_NAME_SCREEN,
            SpmUtils.SPM_TYPE_SCREEN_CLOSE
        )
        agentWeb!!.destroy()
    }
}