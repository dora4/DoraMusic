package site.doramusic.app.ui.activity

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.alibaba.android.arouter.facade.annotation.Route
import dora.arouter.open
import dora.http.retrofit.RetrofitManager
import dora.skin.SkinManager
import dora.skin.base.BaseSkinActivity
import dora.util.StatusBarUtils
import dora.util.TextUtils
import dora.widget.DoraLoadingDialog
import dora.widget.DoraToggleButton
import site.doramusic.app.MusicApp
import site.doramusic.app.R
import site.doramusic.app.base.conf.ARoutePath
import site.doramusic.app.base.conf.AppConfig
import site.doramusic.app.databinding.ActivitySettingsBinding
import site.doramusic.app.http.DoraCallback
import site.doramusic.app.http.service.UserService
import site.doramusic.app.util.PreferencesManager

@Route(path = ARoutePath.ACTIVITY_SETTINGS)
class SettingsActivity : BaseSkinActivity<ActivitySettingsBinding>(), AppConfig, View.OnClickListener {

    internal lateinit var prefsManager: PreferencesManager
    internal var updateDialog: DoraLoadingDialog? = null

    override fun getLayoutId(): Int {
        return R.layout.activity_settings
    }

    override fun onSetStatusBar() {
        super.onSetStatusBar()
        StatusBarUtils.setTransparencyStatusBar(this)
    }
    override fun initData(savedInstanceState: Bundle?) {
        mBinding.statusbarSettings.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            StatusBarUtils.getStatusBarHeight()
        )
        SkinManager.getLoader().setBackgroundColor(mBinding.statusbarSettings, "skin_theme_color")
        mBinding.v = this
        updateDialog = DoraLoadingDialog(this)
        prefsManager = PreferencesManager(this)
//        if (UserManager.currentUser != null) {
//            mBinding.rlSettingsLogout.visibility = View.VISIBLE
//        }
        mBinding.tbSettingsTwo.isChecked = prefsManager.getColdLaunchAutoPlay()
        mBinding.tbSettingsThree.isChecked = prefsManager.getShakeChangeMusic()
        mBinding.tbSettingsFive.isChecked = prefsManager.getBassBoost()

        mBinding.tbSettingsTwo.setOnCheckedChangeListener(object : DoraToggleButton.OnCheckedChangeListener {
            override fun onCheckedChanged(view: DoraToggleButton?, isChecked: Boolean) {
                mBinding.tbSettingsTwo.isChecked = isChecked
                prefsManager.saveColdLaunchAutoPlay(isChecked)
            }
        })
        mBinding.tbSettingsThree.setOnCheckedChangeListener(object : DoraToggleButton.OnCheckedChangeListener {
            override fun onCheckedChanged(view: DoraToggleButton?, isChecked: Boolean) {
                mBinding.tbSettingsThree.isChecked = isChecked
                prefsManager.saveShakeChangeMusic(isChecked)
            }
        })
        mBinding.tbSettingsFive.setOnCheckedChangeListener(object : DoraToggleButton.OnCheckedChangeListener {
            override fun onCheckedChanged(view: DoraToggleButton?, isChecked: Boolean) {
                mBinding.tbSettingsFive.isChecked = isChecked
                prefsManager.saveBassBoost(isChecked)
                if (isChecked) {
                    MusicApp.instance!!.mediaManager!!.setBassBoost(1000)
                } else {
                    MusicApp.instance!!.mediaManager!!.setBassBoost(1)
                }
                mBinding.tbSettingsFive.isChecked = isChecked
                prefsManager.saveHelloDora(isChecked)
            }
        })
    }

    override fun onClick(view: View) {
        when(view.id) {
//            R.id.rl_settings_logout -> {
//                val service = RetrofitManager.getService(UserService::class.java)
//                val prefsManager = PreferencesManager(this)
//                if (TextUtils.isNotEmpty(prefsManager.getToken())) {
//                    val call = service.logout(prefsManager.getToken() ?: "")
//                    call.enqueue(object : DoraCallback<Long>() {
//
//                        override fun onSuccess(body: Long) {
//                            showShortToast("注销登录")
//                            UserManager.update(null)
//                            prefsManager.removeToken()
//                        }
//
//                        override fun onFailure(code: Int, msg: String) {
//                            showShortToast(msg)
//                        }
//                    })
//                    finish()
//                } else {
//                    showShortToast("注销登录")
//                    UserManager.update(null)
//                }
//            }
            R.id.rl_settings_two -> {
                val isChecked = mBinding.tbSettingsTwo.isChecked
                mBinding.tbSettingsTwo.isChecked = !isChecked
                prefsManager.saveColdLaunchAutoPlay(!isChecked)
            }
            R.id.rl_settings_three -> {
                val isChecked = mBinding.tbSettingsThree.isChecked
                mBinding.tbSettingsThree.isChecked = !isChecked
                prefsManager.saveShakeChangeMusic(!isChecked)
            }
            R.id.rl_settings_five -> {
                val isChecked = mBinding.tbSettingsFive.isChecked
                mBinding.tbSettingsFive.isChecked = !isChecked
                prefsManager.saveBassBoost(!isChecked)
                if (isChecked) {
                    MusicApp.instance!!.mediaManager!!.setBassBoost(1000)
                } else {
                    MusicApp.instance!!.mediaManager!!.setBassBoost(1)
                }
            }
            R.id.rl_settings_user_protocol -> {
                open(ARoutePath.ACTIVITY_PROTOCOL) {
                    withString("title", "用户协议")
                }
            }
            R.id.rl_settings_privacy_policy -> {
                open(ARoutePath.ACTIVITY_PROTOCOL) {
                    withString("title", "隐私政策")
                }
            }
        }
    }
}
