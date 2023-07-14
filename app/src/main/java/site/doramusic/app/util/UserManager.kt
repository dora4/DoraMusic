package site.doramusic.app.util

import android.content.Context
import dora.http.retrofit.RetrofitManager
import dora.util.TextUtils
import site.doramusic.app.http.DoraCallback
import site.doramusic.app.http.DoraUser
import site.doramusic.app.http.service.UserService

object UserManager {

    val service = RetrofitManager.getService(UserService::class.java)

    var currentUser: DoraUser? = null
        private set

    fun update(doraUser: DoraUser?) {
        currentUser = doraUser
    }

    fun update(context: Context) {
        val prefsManager = PreferencesManager(context)
        if (TextUtils.isNotEmpty(prefsManager.getToken())) {
            val call = service.checkLogin(prefsManager.getToken() ?: "")
            call.enqueue(object : DoraCallback<DoraUser>() {
                override fun onSuccess(user: DoraUser) {
                    update(user)
                }

                override fun onFailure(code: Int, msg: String) {
                    val login = service.login(prefsManager.getUsername() ?: "", prefsManager.getPassword() ?: "")
                    login.enqueue(object : DoraCallback<DoraUser>() {
                        override fun onSuccess(user: DoraUser) {
                            update(user)
                        }

                        override fun onFailure(code: Int, msg: String) {
                            update(null)
                        }
                    })
                }
            })
        }
    }
}