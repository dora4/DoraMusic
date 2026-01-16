package site.doramusic.app.ui.activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.alibaba.android.arouter.facade.annotation.Route
import dora.arouter.openWithFinish
import dora.http.DoraHttp.flowRequest
import dora.http.DoraHttp.net
import dora.http.retrofit.RetrofitManager
import dora.util.StatusBarUtils
import dora.util.TextUtils
import site.doramusic.app.MusicApp
import site.doramusic.app.R
import site.doramusic.app.auth.AuthService
import site.doramusic.app.auth.DoraUser
import site.doramusic.app.auth.ReqToken
import site.doramusic.app.auth.TokenStore
import site.doramusic.app.auth.UserManager
import site.doramusic.app.conf.ARoutePath
import site.doramusic.app.databinding.ActivitySplashBinding
import site.doramusic.app.http.ApiCode
import site.doramusic.app.http.SecureRequestBuilder
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
        net {
            val token = TokenStore.accessToken().orEmpty()
            if (TextUtils.isNotEmpty(token)) {
                // token中带有分区等信息，直接发起token校验
                val req = ReqToken(token)
                val body = SecureRequestBuilder.build(req, SecureRequestBuilder.SecureMode.ENC)
                    ?: return@net
                flowRequest(requestBlock = {
                    RetrofitManager.getService(AuthService::class.java)
                        .checkToken(body.toRequestBody())
                }, successBlock = {
                    if (it.code == ApiCode.SUCCESS) {
                        // 请求成功
                        it.data?.let {
                            UserManager.ins?.setCurrentUser(DoraUser(it.erc20, it.latestSignIn))
                        }
                    }
                    openWithFinish(ARoutePath.ACTIVITY_MAIN)
                }, failureBlock = {
                    openWithFinish(ARoutePath.ACTIVITY_MAIN)
                })
            } else {
                // 未登录过或已注销登录，token为null时走这里
                openWithFinish(ARoutePath.ACTIVITY_MAIN)
            }
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
