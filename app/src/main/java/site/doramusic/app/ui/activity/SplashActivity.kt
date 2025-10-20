package site.doramusic.app.ui.activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.alibaba.android.arouter.facade.annotation.Route
import dora.arouter.openWithFinish
import dora.util.PermissionHelper
import dora.util.StatusBarUtils
import site.doramusic.app.R
import site.doramusic.app.base.conf.ARoutePath
import site.doramusic.app.databinding.ActivitySplashBinding
import site.doramusic.app.util.MusicUtils

/**
 * 启动页。
 */
@Route(path = ARoutePath.ACTIVITY_SPLASH)
class SplashActivity : BaseSkinActivity<ActivitySplashBinding>() {

    private lateinit var helper: PermissionHelper

    override fun onSetStatusBar() {
        super.onSetStatusBar()
        StatusBarUtils.setTransparencyStatusBar(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        helper = PermissionHelper.with(this).prepare(PermissionHelper.Permission.WRITE_EXTERNAL_STORAGE)
        super.onCreate(savedInstanceState)
    }

    override fun initData(savedInstanceState: Bundle?, binding: ActivitySplashBinding) {
        splashLoading()
    }

    private fun splashLoading() {
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({ // 初始化完成，进入主界面
            openWithFinish(ARoutePath.ACTIVITY_MAIN)
        }, 300)
    }

    override fun onDestroy() {
        super.onDestroy()
        MusicUtils.clearCache()
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_splash
    }
}
