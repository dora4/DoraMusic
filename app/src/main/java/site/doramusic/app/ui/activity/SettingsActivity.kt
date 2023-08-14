package site.doramusic.app.ui.activity

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.alibaba.android.arouter.facade.annotation.Route
import com.google.firebase.analytics.FirebaseAnalytics
import dora.arouter.open
import dora.skin.SkinManager
import dora.skin.base.BaseSkinActivity
import dora.util.StatusBarUtils
import dora.widget.DoraLoadingDialog
import dora.widget.DoraToggleButton
import site.doramusic.app.MusicApp
import site.doramusic.app.R
import site.doramusic.app.base.conf.ARoutePath
import site.doramusic.app.base.conf.AppConfig
import site.doramusic.app.databinding.ActivitySettingsBinding
import site.doramusic.app.util.PreferencesManager
import site.doramusic.app.util.SpmUtils.SPM_ID_CLOSE_SCREEN_SETTINGS
import site.doramusic.app.util.SpmUtils.SPM_ID_OPEN_SCREEN_SETTINGS
import site.doramusic.app.util.SpmUtils.SPM_ID_TOGGLE_BUTTON_CLOSE_AUTO_PLAY
import site.doramusic.app.util.SpmUtils.SPM_ID_TOGGLE_BUTTON_CLOSE_BASS_BOOST
import site.doramusic.app.util.SpmUtils.SPM_ID_TOGGLE_BUTTON_CLOSE_SHAKE
import site.doramusic.app.util.SpmUtils.SPM_ID_TOGGLE_BUTTON_OPEN_AUTO_PLAY
import site.doramusic.app.util.SpmUtils.SPM_ID_TOGGLE_BUTTON_OPEN_BASS_BOOST
import site.doramusic.app.util.SpmUtils.SPM_ID_TOGGLE_BUTTON_OPEN_SHAKE
import site.doramusic.app.util.SpmUtils.SPM_NAME_SCREEN
import site.doramusic.app.util.SpmUtils.SPM_NAME_TOGGLE_BUTTON
import site.doramusic.app.util.SpmUtils.SPM_TYPE_SCREEN_CLOSE
import site.doramusic.app.util.SpmUtils.SPM_TYPE_SCREEN_OPEN
import site.doramusic.app.util.SpmUtils.SPM_TYPE_TOGGLE_BUTTON_CLOSE
import site.doramusic.app.util.SpmUtils.SPM_TYPE_TOGGLE_BUTTON_OPEN
import site.doramusic.app.util.SpmUtils.spm
import site.doramusic.app.util.SpmUtils.spmScreen

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

    override fun onDestroy() {
        super.onDestroy()
        spmScreen(SPM_ID_CLOSE_SCREEN_SETTINGS, SPM_NAME_SCREEN, SPM_TYPE_SCREEN_CLOSE)
    }
    override fun initData(savedInstanceState: Bundle?) {
        spmScreen(SPM_ID_OPEN_SCREEN_SETTINGS, SPM_NAME_SCREEN, SPM_TYPE_SCREEN_OPEN)
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
                if (isChecked) {
                    spm(SPM_ID_TOGGLE_BUTTON_OPEN_AUTO_PLAY, SPM_NAME_TOGGLE_BUTTON, SPM_TYPE_TOGGLE_BUTTON_OPEN)
                } else {
                    spm(SPM_ID_TOGGLE_BUTTON_CLOSE_AUTO_PLAY, SPM_NAME_TOGGLE_BUTTON, SPM_TYPE_TOGGLE_BUTTON_CLOSE)
                }
                mBinding.tbSettingsTwo.isChecked = isChecked
                prefsManager.saveColdLaunchAutoPlay(isChecked)
            }
        })
        mBinding.tbSettingsThree.setOnCheckedChangeListener(object : DoraToggleButton.OnCheckedChangeListener {
            override fun onCheckedChanged(view: DoraToggleButton?, isChecked: Boolean) {
                if (isChecked) {
                    spm(SPM_ID_TOGGLE_BUTTON_OPEN_SHAKE, SPM_NAME_TOGGLE_BUTTON, SPM_TYPE_TOGGLE_BUTTON_OPEN)
                } else {
                    spm(SPM_ID_TOGGLE_BUTTON_CLOSE_SHAKE, SPM_NAME_TOGGLE_BUTTON, SPM_TYPE_TOGGLE_BUTTON_CLOSE)
                }
                mBinding.tbSettingsThree.isChecked = isChecked
                prefsManager.saveShakeChangeMusic(isChecked)
            }
        })
        mBinding.tbSettingsFive.setOnCheckedChangeListener(object : DoraToggleButton.OnCheckedChangeListener {
            override fun onCheckedChanged(view: DoraToggleButton?, isChecked: Boolean) {
                if (isChecked) {
                    spm(SPM_ID_TOGGLE_BUTTON_OPEN_BASS_BOOST, SPM_NAME_TOGGLE_BUTTON, SPM_TYPE_TOGGLE_BUTTON_OPEN)
                } else {
                    spm(SPM_ID_TOGGLE_BUTTON_CLOSE_BASS_BOOST, SPM_NAME_TOGGLE_BUTTON, SPM_TYPE_TOGGLE_BUTTON_CLOSE)
                }
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
