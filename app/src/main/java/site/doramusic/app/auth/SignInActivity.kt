package site.doramusic.app.auth

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import androidx.core.content.ContextCompat
import dora.BaseActivity
import dora.firebase.SpmUtils.spmLogin
import dora.http.DoraHttp.net
import dora.http.DoraHttp.result
import dora.pay.DoraFund
import dora.util.RxBus
import dora.util.StatusBarUtils
import dora.util.TextUtils
import dora.util.ViewUtils
import dora.widget.DoraLoadingDialog
import site.doramusic.app.R
import site.doramusic.app.conf.AppConfig
import site.doramusic.app.databinding.ActivitySignInBinding
import site.doramusic.app.auth.SignInEvent
import site.doramusic.app.http.SecureRequestBuilder

class SignInActivity : BaseActivity<ActivitySignInBinding>() {

    override fun getLayoutId(): Int {
        return R.layout.activity_sign_in
    }

    override fun onSetStatusBar() {
        super.onSetStatusBar()
        StatusBarUtils.setTransparencyStatusBar(this)
    }

    override fun initData(savedInstanceState: Bundle?, binding: ActivitySignInBinding) {
        binding.tvSignInForget.setOnClickListener {
            showLongToast("请前往 http://dorachat.com 下载Dora Chat")
        }
        binding.tvConnectWallet.setOnClickListener {
            if (DoraFund.isWalletConnected()) {
                showLongToast(getString(R.string.wallet_connected))
                return@setOnClickListener
            }
            DoraFund.connectWallet(this)
        }
        val text = "Don't have an account? Sign up"
        val spannable = SpannableString(text)
        val start = text.indexOf("Sign up")
        val end = start + "Sign up".length
        spannable.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                showLongToast("请前往 http://dorachat.com 下载Dora Chat")
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false   // 不要下划线
                ds.color = ContextCompat.getColor(this@SignInActivity, R.color.colorPrimary) // 高亮颜色
            }
        }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.tvSignUp.text = spannable
        binding.tvSignUp.movementMethod = LinkMovementMethod.getInstance()
        binding.tvSignUp.highlightColor = Color.TRANSPARENT
        binding.btnSignIn.setOnClickListener {
            if (!DoraFund.isWalletConnected()) {
                showLongToast(getString(R.string.using_wallet_authorization_first))
                return@setOnClickListener
            }
            val authWord = ViewUtils.getText(binding.etPassword)
            if (authWord.length < 6) {
                showLongToast(getString(R.string.at_least_6_characters_long))
                return@setOnClickListener
            }
            val dialog = DoraLoadingDialog(this).show("正在登录") {
                setCanceledOnTouchOutside(false)
                messageTextSize(15f)
            }
            net {
                val erc20 = DoraFund.getCurrentAddress()
                val req = ReqSignIn(erc20, authWord, AppConfig.PRODUCT_NAME)
                val body = SecureRequestBuilder.build(req, SecureRequestBuilder.SecureMode.ENC)
                    ?: return@net
                val user = result(AuthService::class) {
                    signIn(body.toRequestBody())
                }?.data
                if (user == null) {
                    showLongToast("登录失败")
                    dialog.dismissWithAnimation()
                    return@net
                }
                val token = user.accessToken
                if (TextUtils.isEmpty(token)) {
                    showLongToast("登录失败")
                    dialog.dismissWithAnimation()
                    return@net
                }
                spmLogin("Dora Chat官方帐号")
                UserManager.ins?.setCurrentUser(DoraUser(user.erc20, user.latestSignIn))
                TokenStore.save(user.accessToken, user.refreshToken)
                RxBus.getInstance().post(SignInEvent(user.erc20))
                showLongToast("登录成功")
                finish()
            }
        }
    }
}