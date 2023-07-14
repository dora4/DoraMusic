package site.doramusic.app.aop

import android.app.Activity
import com.alibaba.android.arouter.launcher.ARouter
import dora.http.retrofit.RetrofitManager
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect

import site.doramusic.app.base.conf.ARoutePath
import site.doramusic.app.http.DoraCallback
import site.doramusic.app.http.DoraUser
import site.doramusic.app.http.service.UserService
import site.doramusic.app.util.PreferencesManager

/**
 * 执行操作前如果没有登录，则跳登录界面。
 */
@Aspect
class CheckLoginAspect {

    @Around("execution(@site.doramusic.app.annotation.CheckLogin * *(..))")
    @Throws(Throwable::class)
    fun aroundLoginPoint(joinPoint: ProceedingJoinPoint) {
        val activity = joinPoint.target as Activity
        val service = RetrofitManager.getService(UserService::class.java)
        val prefsManager = PreferencesManager(activity)
        val call = service.checkLogin(prefsManager.getToken() ?: "")
        call.enqueue(object : DoraCallback<DoraUser>() {
            override fun onSuccess(body: DoraUser) {
                joinPoint.proceed()
            }

            override fun onFailure(code: Int, msg: String) {
                ARouter.getInstance().build(ARoutePath.ACTIVITY_LOGIN).navigation()
            }
        })
    }
}