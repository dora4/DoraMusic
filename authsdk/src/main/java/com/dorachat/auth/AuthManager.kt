package com.dorachat.auth

import android.content.Context
import androidx.core.content.ContextCompat
import dora.http.DoraHttp
import dora.http.retrofit.RetrofitManager
import dora.util.LogUtils
import dora.util.RxBus
import dora.util.TextUtils
import dora.widget.DoraLoadingDialog

object AuthManager {

    private lateinit var partitionId: String

    fun init(context: Context, partitionId: String) {
        this.partitionId = partitionId
        TokenStore.init(context)
    }

    interface SignInListener {
        fun onSuccess(user: DoraUser)
        fun onFailure(msg: String? = null)
    }

    fun signIn(context: Context, erc20: String, authWord: String, listener: SignInListener? = null) {
        DoraHttp.net {
            val req = ReqSignIn(erc20, authWord, partitionId)
            val body =
                SecureRequestBuilder.build(req, SecureRequestBuilder.SecureMode.ENC) ?: return@net
            var dialog: DoraLoadingDialog? = null
            DoraHttp.flowRequest(requestBlock = {
                RetrofitManager.getService(AuthService::class.java).signIn(body.toRequestBody())
            }, successBlock = {
                if (it.code == ApiCode.SUCCESS) {
                    val user = it.data
                    if (user != null) {
                        UserManager.ins?.setCurrentUser(DoraUser(user.erc20, user.latestSignIn))
                        TokenStore.save(user.accessToken, user.refreshToken)
                        RxBus.getInstance().post(SignInEvent(user.erc20))
                        listener?.onSuccess(user)
                    } else {
                        listener?.onFailure()
                    }
                } else {
                    if (DoraChatSDK.getConfig()?.enableLog == true) {
                        LogUtils.e(it.msg)
                    }
                    listener?.onFailure(it.msg)
                }
            }, failureBlock = {
                if (DoraChatSDK.getConfig()?.enableLog == true) {
                    LogUtils.e(it)
                }
                listener?.onFailure(it)
            }, loadingBlock = {
                if (it) {
                    dialog = DoraLoadingDialog(context)
                    dialog.show(ContextCompat.getString(context, R.string.now_sign_in)) {
                        setCanceledOnTouchOutside(false)
                        messageTextSize(15f)
                    }
                } else {
                    dialog?.dismissWithAnimation()
                }
            })
        }
    }

    fun signOut(token: String) {
        DoraHttp.net {
            val req = ReqToken(token)
            val body =
                SecureRequestBuilder.build(req, SecureRequestBuilder.SecureMode.ENC) ?: return@net
            DoraHttp.result(AuthService::class) { signOut(body.toRequestBody()) }
            RxBus.getInstance().post(SignOutEvent())
            TokenStore.clear()
        }
    }

    fun checkToken(callback: () -> Unit) {
        DoraHttp.net {
            val token = TokenStore.accessToken().orEmpty()
            if (TextUtils.isNotEmpty(token)) {
                val req = ReqToken(token)
                val body = SecureRequestBuilder.build(req, SecureRequestBuilder.SecureMode.ENC)
                    ?: return@net
                DoraHttp.flowRequest(requestBlock = {
                    RetrofitManager.getService(AuthService::class.java)
                        .checkToken(body.toRequestBody())
                }, successBlock = {
                    if (it.code == ApiCode.SUCCESS) {
                        it.data?.let {
                            UserManager.ins?.setCurrentUser(
                                DoraUser(
                                    it.erc20,
                                    it.latestSignIn
                                )
                            )
                        }
                    }
                    callback()
                }, failureBlock = {
                    if (DoraChatSDK.getConfig()?.enableLog == true) {
                        LogUtils.e(it)
                    }
                    callback()
                })
            } else {
                // 第一次token都没有走这里
                callback()
            }
        }
    }

    /**
     * 获取访问Token。
     */
    fun getAccessToken(): String? = TokenStore.accessToken()

    /**
     * 获取刷新Token。
     */
    fun getRefreshToken(): String? = TokenStore.refreshToken()
}