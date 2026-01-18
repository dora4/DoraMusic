package site.doramusic.app.ui.activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.alibaba.android.arouter.facade.annotation.Route
import com.dorachat.auth.AuthManager
import dora.arouter.openWithFinish
import dora.util.StatusBarUtils
import site.doramusic.app.MusicApp
import site.doramusic.app.R
import site.doramusic.app.conf.ARoutePath
import site.doramusic.app.databinding.ActivitySplashBinding
import site.doramusic.app.util.MusicUtils

/**
 * 启动页。
 */
@Route(path = ARoutePath.ACTIVITY_SPLASH)
class SplashActivity : BaseSkinActivity<ActivitySplashBinding>() {

    override fun onSetStatusBar() {
        StatusBarUtils.setTransparencyStatusBar(this)
    }

    override fun initData(savedInstanceState: Bundle?, binding: ActivitySplashBinding) {
        splashLoading()
    }

    private fun launchMain() {
        AuthManager.checkToken {
            openWithFinish(ARoutePath.ACTIVITY_MAIN)
        }
    }

    private fun splashLoading() {
        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                if (MusicApp.isAppInitialized) {
                    launchMain()
                } else {
                    // 还没初始化完成，50ms后再次检查
                    handler.postDelayed(this, 50)
                }
            }
        }
        // 立即开始轮询
        handler.post(runnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        MusicUtils.clearCache()
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_splash
    }
}
