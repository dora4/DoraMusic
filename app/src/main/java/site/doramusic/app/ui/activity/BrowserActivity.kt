package site.doramusic.app.ui.activity

import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import com.alibaba.android.arouter.facade.annotation.Route
import com.just.agentweb.AgentWeb
import com.just.agentweb.WebIndicator
import dora.skin.SkinManager
import dora.skin.base.BaseSkinBindingActivity
import dora.util.IntentUtils
import dora.util.StatusBarUtils
import dora.util.ViewUtils
import dora.widget.DoraTitleBar
import site.doramusic.app.R
import site.doramusic.app.base.conf.ARoutePath
import site.doramusic.app.base.conf.AppConfig.Companion.COLOR_THEME
import site.doramusic.app.base.conf.AppConfig.Companion.EXTRA_TITLE
import site.doramusic.app.base.conf.AppConfig.Companion.EXTRA_URL
import site.doramusic.app.databinding.ActivityBrowserBinding

/**
 * 浏览器页面，主要用于加载用户协议和隐私权政策等。
 */
@Route(path = ARoutePath.ACTIVITY_BROWSER)
class BrowserActivity : BaseSkinBindingActivity<ActivityBrowserBinding>() {

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
        val skinThemeColor = SkinManager.getLoader().getColor(COLOR_THEME)
        StatusBarUtils.setStatusBarColor(this, skinThemeColor)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        ViewUtils.showSystemBar(window)
    }

    override fun initData(savedInstanceState: Bundle?, binding: ActivityBrowserBinding) {
        val skinThemeColor = SkinManager.getLoader().getColor(COLOR_THEME)
        binding.titlebar.setBackgroundColor(skinThemeColor)
        title?.let { binding.titlebar.title = it }
        val webIndicator = WebIndicator(this)
        webIndicator.setColor(skinThemeColor)
        binding.titlebar.addMenuButton(R.drawable.ic_min)
        binding.titlebar.setOnIconClickListener(object : DoraTitleBar.OnIconClickListener {
            override fun onIconBackClick(icon: AppCompatImageView) {
            }

            override fun onIconMenuClick(position: Int, icon: AppCompatImageView) {
                // 画中画模式
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    enterPictureInPictureMode()
                }
            }
        })
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        if (isInPictureInPictureMode) {
            mBinding.titlebar.visibility = View.GONE
        } else {
            mBinding.titlebar.visibility = View.VISIBLE
        }
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