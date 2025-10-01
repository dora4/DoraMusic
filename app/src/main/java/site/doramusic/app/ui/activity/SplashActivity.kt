package site.doramusic.app.ui.activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.alibaba.android.arouter.facade.annotation.Route
import dora.arouter.openWithFinish
import dora.crash.DoraCrash
import dora.util.IoUtils
import dora.util.PermissionHelper
import dora.util.StatusBarUtils
import site.doramusic.app.MusicApp
import site.doramusic.app.R
import site.doramusic.app.base.conf.ARoutePath
import site.doramusic.app.base.conf.AppConfig
import site.doramusic.app.base.conf.AppConfig.Companion.LOG_PATH
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

    private fun initAppFolder() {
        if (IoUtils.checkMediaMounted()) {
            IoUtils.createFolder(arrayOf(AppConfig.FOLDER_LOG, AppConfig.FOLDER_LRC))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        helper = PermissionHelper.with(this).prepare(PermissionHelper.Permission.WRITE_EXTERNAL_STORAGE)
        super.onCreate(savedInstanceState)
    }

    override fun initData(savedInstanceState: Bundle?, binding: ActivitySplashBinding) {
        if (!PermissionHelper.hasStoragePermission(this)) {
            helper.permissions(PermissionHelper.Permission.WRITE_EXTERNAL_STORAGE).request {
                if (it) {
                    initAppFolder()
                    DoraCrash.initCrash(
                        this@SplashActivity,
                        LOG_PATH
                    )
                }
            }
        } else {
            // 以后打开权限开关了才初始化这部分
            initAppFolder()
            DoraCrash.initCrash(
                this@SplashActivity,
                LOG_PATH
            )
        }
        splashLoading()
    }

    private fun splashLoading() {
        val handler = Handler(Looper.getMainLooper())
        handler.post(object : Runnable {
            override fun run() {
                if (MusicApp.isAppInitialized) {
                    // 初始化完成，进入主界面
                    openWithFinish(ARoutePath.ACTIVITY_MAIN)
                } else {
                    // 未完成，继续轮询
                    handler.postDelayed(this, 50) // 每50ms检查一次
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        MusicUtils.clearCache()
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_splash
    }
}
