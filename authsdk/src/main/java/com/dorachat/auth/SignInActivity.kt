package com.dorachat.auth

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.alibaba.android.arouter.facade.annotation.Route
import com.dorachat.auth.databinding.ActivitySignInBindingImpl
import dora.BaseActivity
import dora.pay.DoraFund
import dora.util.LogUtils
import dora.util.StatusBarUtils
import dora.util.ToastUtils
import dora.util.ViewUtils

@Route(path = ARouterPath.ACTIVITY_SIGN_IN)
class SignInActivity : BaseActivity<ActivitySignInBindingImpl>() {

    override fun getLayoutId(): Int {
        return R.layout.activity_sign_in
    }

    override fun onSetStatusBar() {
        StatusBarUtils.setTransparencyStatusBar(this)
    }

    override fun initData(savedInstanceState: Bundle?, binding: ActivitySignInBindingImpl) {
        binding.tvConnectWallet.setOnClickListener {
            if (DoraFund.isWalletConnected()) {
                showLongToast(getString(R.string.wallet_connected))
                return@setOnClickListener
            }
            DoraFund.connectWallet(this)
        }
        val text = getString(R.string.don_t_have_an_account, getString(R.string.sign_up))
        val spannable = SpannableString(text)
        val start = text.indexOf(getString(R.string.sign_up))
        val end = start + getString(R.string.sign_up).length
        spannable.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                forwardDoraChat()
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
        binding.tvForgetPassword.setOnClickListener {
            forwardDoraChat()
        }
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
            val erc20 = DoraFund.getCurrentAddress()
            AuthManager.signIn(this, erc20, authWord, object : AuthManager.SignInListener {
                override fun onSuccess(user: DoraUser) {
                    showLongToast(getString(R.string.signed_in_successfully))
                    finish()
                }

                override fun onFailure(msg: String?) {
                    if (msg != null) {
                        showLongToast(
                            String.format(
                                getString(R.string.failed_to_sign_in_with_error),
                                msg
                            )
                        )
                    } else {
                        showLongToast(getString(R.string.failed_to_sign_in))
                    }
                }
            })
        }
    }

    private fun forwardDoraChat() {
        val url = "http://dorachat.com"
        ToastUtils.showLong(getString(R.string.forward_dora_chat, url))
        try {
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } catch (ignored: Exception) {
            if (DoraChatSDK.getConfig()?.enableLog == true) {
                LogUtils.e(ignored.toString())
            }
            ToastUtils.showShort(getString(R.string.unable_to_open_browser))
        }
    }
}