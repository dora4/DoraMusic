package site.doramusic.app.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.just.agentweb.AgentWeb
import com.just.agentweb.WebIndicator
import dora.BaseActivity
import dora.util.IntentUtils
import dora.util.StatusBarUtils
import site.doramusic.app.R
import site.doramusic.app.base.conf.AppConfig.Companion.EXTRA_TITLE
import site.doramusic.app.base.conf.AppConfig.Companion.EXTRA_URL
import site.doramusic.app.databinding.ActivityBrowserBinding

class BrowserActivity : BaseActivity<ActivityBrowserBinding>() {

    private var title: String? = null
    private var url: String? = null
    private var agentWeb: AgentWeb? = null
    override fun getLayoutId(): Int {
        return R.layout.activity_browser
    }

    override fun onGetExtras(action: String?, bundle: Bundle?, intent: Intent) {
        title = IntentUtils.getStringExtra(intent, EXTRA_TITLE)
        url = IntentUtils.getStringExtra(intent, EXTRA_URL)
    }

    override fun onSetStatusBar() {
        StatusBarUtils.setStatusBarColorRes(this, R.color.colorPrimaryDark)
    }

    override fun initData(savedInstanceState: Bundle?, binding: ActivityBrowserBinding) {
        title?.let { binding.titlebar.title = it }
        val webIndicator = WebIndicator(this)
        webIndicator.setColor(ContextCompat.getColor(this, R.color.colorPrimary))
        agentWeb = AgentWeb.with(this)
            .setAgentWebParent(
                binding.rlBrowserWebPage,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
            .setCustomIndicator(webIndicator)
            .createAgentWeb()
            .ready()
            .go(url)
    }

    override fun onPointerCaptureChanged(hasCapture: Boolean) {}

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return agentWeb?.handleKeyEvent(keyCode, event) ?: super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        super.onDestroy()
        agentWeb?.destroy()
    }
}