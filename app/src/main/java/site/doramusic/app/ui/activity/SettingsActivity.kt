package site.doramusic.app.ui.activity

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.alibaba.android.arouter.facade.annotation.Route
import com.walletconnect.web3.modal.client.Web3Modal
import dora.arouter.open
import dora.firebase.SpmUtils.spmSelectContent
import dora.skin.SkinManager
import dora.trade.DoraTrade
import dora.util.StatusBarUtils
import dora.widget.DoraBottomMenuDialog
import dora.widget.DoraLoadingDialog
import dora.widget.DoraSingleButtonDialog
import dora.widget.DoraToggleButton
import site.doramusic.app.R
import site.doramusic.app.base.conf.ARoutePath
import site.doramusic.app.base.conf.AppConfig
import site.doramusic.app.base.conf.AppConfig.Companion.COLOR_THEME
import site.doramusic.app.databinding.ActivitySettingsBinding
import site.doramusic.app.media.MediaManager
import site.doramusic.app.util.PrefsManager

@Route(path = ARoutePath.ACTIVITY_SETTINGS)
class SettingsActivity : BaseSkinActivity<ActivitySettingsBinding>(), AppConfig, View.OnClickListener {

    internal lateinit var prefsManager: PrefsManager
    private var updateDialog: DoraLoadingDialog? = null

    override fun getLayoutId(): Int {
        return R.layout.activity_settings
    }

    override fun onSetStatusBar() {
        super.onSetStatusBar()
        StatusBarUtils.setTransparencyStatusBar(this)
    }

    override fun initData(savedInstanceState: Bundle?, binding: ActivitySettingsBinding) {
        binding.statusbarSettings.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            StatusBarUtils.getStatusBarHeight()
        )
        SkinManager.getLoader().setBackgroundColor(binding.statusbarSettings, COLOR_THEME)
        binding.v = this
        updateDialog = DoraLoadingDialog(this)
        prefsManager = PrefsManager(this)
        val skinThemeColor = SkinManager.getLoader().getColor(COLOR_THEME)
        binding.tbSettingsAutoPlay.checkedColor = skinThemeColor
        binding.tbSettingsShake.checkedColor = skinThemeColor
        binding.tbSettingsBassBoost.checkedColor = skinThemeColor
        binding.tbSettingsAutoPlay.isChecked = prefsManager.getColdLaunchAutoPlay()
        binding.tbSettingsShake.isChecked = prefsManager.getShakeChangeMusic()
        binding.tbSettingsBassBoost.isChecked = prefsManager.getBassBoost()

        binding.tbSettingsAutoPlay.setOnCheckedChangeListener(object : DoraToggleButton.OnCheckedChangeListener {
            override fun onCheckedChanged(view: DoraToggleButton?, isChecked: Boolean) {
                if (isChecked) {
                    spmSelectContent("打开自动播放开关")
                } else {
                    spmSelectContent("关闭自动播放开关")
                }
                binding.tbSettingsAutoPlay.isChecked = isChecked
                prefsManager.saveColdLaunchAutoPlay(isChecked)
            }
        })
        binding.tbSettingsShake.setOnCheckedChangeListener(object : DoraToggleButton.OnCheckedChangeListener {
            override fun onCheckedChanged(view: DoraToggleButton?, isChecked: Boolean) {
                if (isChecked) {
                    spmSelectContent("打开摇一摇切歌开关")
                } else {
                    spmSelectContent("关闭摇一摇切歌开关")
                }
                binding.tbSettingsShake.isChecked = isChecked
                prefsManager.saveShakeChangeMusic(isChecked)
            }
        })
        binding.tbSettingsBassBoost.setOnCheckedChangeListener(object : DoraToggleButton.OnCheckedChangeListener {
            override fun onCheckedChanged(view: DoraToggleButton?, isChecked: Boolean) {
                if (isChecked) {
                    spmSelectContent("打开重低音开关")
                } else {
                    spmSelectContent("关闭重低音开关")
                }
                binding.tbSettingsBassBoost.isChecked = isChecked
                prefsManager.saveBassBoost(isChecked)
                if (isChecked) {
                    MediaManager.setBassBoost(1000)
                } else {
                    MediaManager.setBassBoost(1)
                }
                binding.tbSettingsBassBoost.isChecked = isChecked
            }
        })
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.rl_settings_auto_play -> {
                val isChecked = mBinding.tbSettingsAutoPlay.isChecked
                mBinding.tbSettingsAutoPlay.isChecked = !isChecked
                prefsManager.saveColdLaunchAutoPlay(!isChecked)
            }
            R.id.rl_settings_shake -> {
                val isChecked = mBinding.tbSettingsShake.isChecked
                mBinding.tbSettingsShake.isChecked = !isChecked
                prefsManager.saveShakeChangeMusic(!isChecked)
            }
            R.id.rl_settings_bass_boost -> {
                val isChecked = mBinding.tbSettingsBassBoost.isChecked
                mBinding.tbSettingsBassBoost.isChecked = !isChecked
                prefsManager.saveBassBoost(!isChecked)
                if (isChecked) {
                    MediaManager.setBassBoost(1000)
                } else {
                    MediaManager.setBassBoost(1)
                }
            }
            R.id.rl_settings_donate -> {
                if (Web3Modal.getAccount() == null) {
                    DoraTrade.connectWallet(this)
                    return
                }
                val menus = arrayOf(getString(R.string.donation_desc_1),
                    getString(R.string.donation_desc_2), getString(R.string.donation_desc_3))
                val dialog = DoraBottomMenuDialog().show(this, menus)
                dialog.setOnMenuClickListener(object : DoraBottomMenuDialog.OnMenuClickListener {
                    override fun onMenuClick(position: Int, menu: String) {
                        dialog.dismiss()
                        val amount = when (position) {
                            0 -> {
                                0.1
                            }
                            1 -> {
                                1.0
                            }
                            else -> {
                                10.0
                            }
                        }
                        DoraTrade.donate(this@SettingsActivity,
                            "vs42INhGWDnq",
                            "RrZqzf1Vh8StMqyHhpfCu6TPOQMoCRYw",
                            getString(R.string.i_want_to_donate),
                            getString(R.string.donation_speech),
                            "0xcBa852Ef29a43a7542B88F60C999eD9cB66f6000",
                            amount)
                    }
                })
            }
            R.id.rl_settings_user_protocol -> {
                open(ARoutePath.ACTIVITY_PROTOCOL) {
                    withString("title", getString(R.string.user_agreement_title))
                }
            }
            R.id.rl_settings_privacy_policy -> {
                open(ARoutePath.ACTIVITY_PROTOCOL) {
                    withString("title", getString(R.string.privacy_policy_title))
                }
            }
        }
    }
}
