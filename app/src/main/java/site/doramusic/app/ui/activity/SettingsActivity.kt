package site.doramusic.app.ui.activity

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.alibaba.android.arouter.facade.annotation.Route
import com.walletconnect.web3.modal.client.Modal
import com.walletconnect.web3.modal.client.Web3Modal
import dora.arouter.open
import dora.db.builder.WhereBuilder
import dora.db.dao.DaoFactory
import dora.firebase.SpmUtils.spmSelectContent
import dora.pgyer.PgyVersionUpdate
import dora.skin.SkinManager
import dora.pay.DoraFund
import dora.util.DeepLinkUtils
import dora.util.StatusBarUtils
import dora.widget.DoraBottomMenuDialog
import dora.widget.DoraLoadingDialog
import dora.widget.DoraToggleButton
import site.doramusic.app.R
import site.doramusic.app.base.conf.ARoutePath
import site.doramusic.app.base.conf.AppConfig
import site.doramusic.app.base.conf.AppConfig.Companion.COLOR_THEME
import site.doramusic.app.base.conf.AppConfig.Companion.COLUMN_PENDING
import site.doramusic.app.base.conf.AppConfig.Companion.DISCORD_GROUP_INVITE_CODE
import site.doramusic.app.base.conf.AppConfig.Companion.DORA_FUND_ACCESS_KEY
import site.doramusic.app.base.conf.AppConfig.Companion.DORA_FUND_SECRET_KEY
import site.doramusic.app.base.conf.AppConfig.Companion.EXTRA_TITLE
import site.doramusic.app.base.conf.AppConfig.Companion.PGYER_API_KEY
import site.doramusic.app.base.conf.AppConfig.Companion.PGYER_APP_KEY
import site.doramusic.app.databinding.ActivitySettingsBinding
import site.doramusic.app.media.MediaManager
import site.doramusic.app.model.Donation
import site.doramusic.app.util.PrefsManager

/**
 * 设置界面。
 */
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
        binding.tbSettingsAutoConnectVpn.checkedColor = skinThemeColor
        binding.tbSettingsShake.checkedColor = skinThemeColor
        binding.tbSettingsBassBoost.checkedColor = skinThemeColor
        binding.tbSettingsAutoPlay.isChecked = prefsManager.getColdLaunchAutoPlay()
        binding.tbSettingsAutoConnectVpn.isChecked = prefsManager.getColdLaunchAutoConnectVPN()
        binding.tbSettingsShake.isChecked = prefsManager.getShakeChangeMusic()
        binding.tbSettingsBassBoost.isChecked = prefsManager.getBassBoost()
        if (DaoFactory.getDao(Donation::class.java).count(
                WhereBuilder.create().addWhereEqualTo(COLUMN_PENDING, true)
        ) > 0) {
            // 有捐赠记录才显示此栏
            binding.rlSettingsDonation.visibility = View.VISIBLE
        }
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
        binding.tbSettingsAutoConnectVpn.setOnCheckedChangeListener(object : DoraToggleButton.OnCheckedChangeListener {
            override fun onCheckedChanged(view: DoraToggleButton?, isChecked: Boolean) {
                if (isChecked) {
                    spmSelectContent("打开自动连接VPN开关")
                } else {
                    spmSelectContent("关闭自动连接VPN开关")
                }
                binding.tbSettingsAutoConnectVpn.isChecked = isChecked
                prefsManager.saveColdLaunchAutoConnectVPN(isChecked)
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
            R.id.rl_settings_auto_connect_vpn -> {
                val isChecked = mBinding.tbSettingsAutoConnectVpn.isChecked
                mBinding.tbSettingsAutoConnectVpn.isChecked = !isChecked
                prefsManager.saveColdLaunchAutoConnectVPN(!isChecked)
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
            R.id.rl_settings_share -> {
                var shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.setType("text/plain")
                shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_content))
                shareIntent = Intent.createChooser(shareIntent,
                    getString(R.string.select_sharing_method))
                startActivity(shareIntent)
            }
            R.id.rl_settings_donate -> {
                if (Web3Modal.getAccount() == null) {
                    DoraFund.connectWallet(this)
                    return
                }
                val menus = arrayOf(
                    getString(R.string.donation_desc_1),
                    getString(R.string.donation_desc_2),
                    getString(R.string.donation_desc_3),
                    getString(R.string.disconnect_wallet)
                )
                val dialog = DoraBottomMenuDialog().show(this, menus)
                dialog.setOnMenuClickListener(object : DoraBottomMenuDialog.OnMenuClickListener {
                    override fun onMenuClick(position: Int, menu: String) {
                        dialog.dismiss()
                        if (position == 3) {
                            DoraFund.disconnectWallet()
                            return
                        }
                        val amount = when (position) {
                            0 -> {
                                0.1
                            }
                            1 -> {
                                1.0
                            }
                            2 -> {
                                10.0
                            }
                            else -> {
                                0.0
                            }
                        }
                        DoraFund.pay(this@SettingsActivity,
                            DORA_FUND_ACCESS_KEY,
                            DORA_FUND_SECRET_KEY,
                            getString(R.string.i_want_to_donate),
                            getString(R.string.donation_speech),
                            "0xcBa852Ef29a43a7542B88F60C999eD9cB66f6000",
                            amount,
                            null,
                            object : DoraFund.OrderListener {
                                override fun onPrintOrder(
                                    orderId: String,
                                    chain: Modal.Model.Chain,
                                    value: Double
                                ) {
                                    // 保存捐赠信息
                                    val donation = Donation(
                                        orderId = orderId,
                                        tokenAmount = value,
                                        tokenSymbol = "POL",
                                        timestamp = System.currentTimeMillis(),
                                    )
                                    DaoFactory.getDao(Donation::class.java).insert(donation)
                                }
                            })
                    }
                })
            }
            R.id.rl_settings_donation -> {
                open(ARoutePath.ACTIVITY_DONATION)
            }
            R.id.rl_settings_discord -> {
                DeepLinkUtils.openDiscordGroup(this@SettingsActivity, DISCORD_GROUP_INVITE_CODE)
            }
            R.id.rl_settings_check_update -> {
                PgyVersionUpdate.checkVersion(this, PGYER_API_KEY,
                    PGYER_APP_KEY, object : PgyVersionUpdate.UpdateListener {
                        override fun onError(msg: String) {
                        }

                        override fun onLatestVersion() {
                            showShortToast(getString(R.string.already_latest_version))
                        }

                        override fun onUpdate(
                            versionCode: Int,
                            versionName: String,
                            isForceUpdate: Boolean,
                            updateLog: String,
                            downloadUrl: String
                        ) {
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.data = Uri.parse(downloadUrl)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            try {
                                startActivity(intent)
                            } catch (ignore: ActivityNotFoundException) {
                            }
                        }
                    })
            }
            R.id.rl_settings_user_protocol -> {
                open(ARoutePath.ACTIVITY_PROTOCOL) {
                    withString(EXTRA_TITLE, getString(R.string.user_agreement_title))
                }
            }
            R.id.rl_settings_privacy_policy -> {
                open(ARoutePath.ACTIVITY_PROTOCOL) {
                    withString(EXTRA_TITLE, getString(R.string.privacy_policy_title))
                }
            }
        }
    }
}
