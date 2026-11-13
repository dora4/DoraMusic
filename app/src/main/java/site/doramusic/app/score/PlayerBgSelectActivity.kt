package site.doramusic.app.score

import android.content.res.Configuration
import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import dora.BaseActivity
import dora.util.SPUtils
import dora.util.StatusBarUtils
import dora.widget.DoraDoubleButtonDialog
import dora.widget.DoraTitleBar
import site.doramusic.app.R
import site.doramusic.app.conf.ARoutePath
import site.doramusic.app.conf.AppConfig
import site.doramusic.app.databinding.ActivityPlayerBgSelectBinding
import site.doramusic.app.track.EventType
import site.doramusic.app.track.TrackAnalysis
import site.doramusic.app.util.MusicTimer
import site.doramusic.app.util.ThemeSelector

@Route(path = ARoutePath.ACTIVITY_PLAYER_BG_SELECT)
class PlayerBgSelectActivity :
    BaseActivity<ActivityPlayerBgSelectBinding>(), AppConfig {

    // 切换开关
    private val useViewPager2 = false
    // private val useViewPager2 = true

    private lateinit var galleryBgFragment: BackgroundListFragment
    private lateinit var mallBgFragment: BackgroundListFragment
    private lateinit var systemBgFragment: BackgroundListFragment
    private lateinit var tabStrategy: TabRenderStrategy
    private var selectedBackground: String = PlayerBgManager.DEFAULT_BACKGROUND

    override fun getLayoutId(): Int = R.layout.activity_player_bg_select

    override fun onSetStatusBar() {
        StatusBarUtils.setTransparencyStatusBar(this)
    }

    override fun initData(
        savedInstanceState: Bundle?,
        binding: ActivityPlayerBgSelectBinding
    ) {
        binding.statusbarPlayerBgSelect.layoutParams =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                StatusBarUtils.getStatusBarHeight()
            )
        ThemeSelector.applyViewTheme(binding.statusbarPlayerBgSelect)
        ThemeSelector.applyViewTheme(binding.titlebarPlayerBgSelect)
        binding.titlebarPlayerBgSelect
            .addMenuButton(
                R.drawable.ic_clear_bg,
                resources.getDimension(R.dimen.title_bar_icon_size).toInt()
            )
            .setOnIconClickListener(object : DoraTitleBar.OnIconClickListener {

                override fun onIconBackClick(icon: AppCompatImageView) {
                    finish()
                }

                override fun onIconMenuClick(
                    position: Int,
                    icon: AppCompatImageView
                ) {
                    resetBackground()
                }
            })
        binding.tabBar?.setTabTextSelectedColor(ThemeSelector.getThemeColor(this))
        binding.tabBar?.setIndicatorColor(ThemeSelector.getThemeColor(this))
        selectedBackground = PlayerBgManager.getSelectedBgId(this)
        val galleryBgList = PlayerBgFactory.createGalleryBackgrounds(this)
        val mallBgList = PlayerBgFactory.createMallBackgrounds(this)
        val systemBgList = PlayerBgFactory.createSystemBackgrounds(this)
        galleryBgFragment = BackgroundListFragment(galleryBgList.toMutableList()) {
            selectBackground(it)
        }
        mallBgFragment = BackgroundListFragment(mallBgList.toMutableList()) {
            selectBackground(it)
        }
        systemBgFragment = BackgroundListFragment(systemBgList.toMutableList()) {
            selectBackground(it)
        }
        val fragmentList = listOf(galleryBgFragment, mallBgFragment, systemBgFragment)
        val tabTitles = arrayListOf(
            getString(R.string.tab_title_bg_gallery),
            getString(R.string.tab_title_bg_mall),
            getString(R.string.tab_title_bg_system)
        )
        binding.tabBar?.setTextTabs(tabTitles)
        // 根据开关选择策略，不再写大段if-else内部逻辑
        tabStrategy = if (useViewPager2) {
            ViewPager2TabStrategy(this)
        } else {
            FragmentTabStrategy(this)
        }
        tabStrategy.initTab(binding, tabTitles, fragmentList)
    }

    /**
     * 屏幕旋转，动态更新列数。交给策略处理。
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        tabStrategy.onConfigurationChanged()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::tabStrategy.isInitialized) {
            tabStrategy.release()
        }
    }

    /**
     * 选择背景。
     */
    private fun selectBackground(background: PlayerBackground) {
        val isUnlocked = isBackgroundUnlocked(background)
        // 商城背景、未解锁：走购买兑换流程
        if (background.bgType == PlayerBackground.BACKGROUND_TYPE_MALL && !isUnlocked) {
            val costPoints = background.price
            val totalPoints = PointsManager.getTotalPoints()
            if (totalPoints < costPoints) {
                showShortToast(getString(R.string.bg_mall_not_enough_note))
                return
            }
            val skinThemeColor = ThemeSelector.getThemeColor(this)
            DoraDoubleButtonDialog(this, object : DoraDoubleButtonDialog.DialogListener {
                override fun onConfirm(eventType: String) {
                    PointsManager.addPoints(PointsSource.EXCHANGE.desc, -costPoints, background.name)
                    // 标记商城背景解锁
                    SPUtils.writeBoolean(this@PlayerBgSelectActivity, background.id, true)
                    // 购买成功，选中背景
                    selectBackground(background)
                    showShortToast(getString(R.string.bg_mall_buy_success, background.name))
                }

                override fun onCancel(eventType: String) {
                }
            }).show(BUY_MALL_BG,
                getString(R.string.bg_mall_buy_confirm, background.name)
            ) {
                themeColor(skinThemeColor)
            }
            return
        }
        if (!isUnlocked) {
            if (background.bgType == PlayerBackground.BACKGROUND_TYPE_SYSTEM) {
                TrackAnalysis.report(lifecycleScope, EventType.EVENT_TYPE_CHANGE_BG,
                    mapOf("id" to background.id)
                )
                val totalMin = SPUtils.readInteger(this, MusicTimer.KEY_TOTAL_PLAY_MINUTES, 0)
                val needMin = getSystemBgRequireMinutes(background.id)
                val remain = (needMin - totalMin).coerceAtLeast(1)
                showShortToast(getString(R.string.bg_system_need_minutes, remain))
            } else {
                showShortToast(getString(R.string.background_not_unlocked))
            }
            return
        }
        selectedBackground = background.id
        PlayerBgManager.selectBg(this, background.id)
        galleryBgFragment.updateSelectId(selectedBackground)
        mallBgFragment.updateSelectId(selectedBackground)
        systemBgFragment.updateSelectId(selectedBackground)
        showShortToast(getString(R.string.background_selected, background.name))
    }

    /**
     * 清空当前背景，恢复默认背景。
     */
    private fun resetBackground() {
        selectedBackground = PlayerBgManager.DEFAULT_BACKGROUND
        PlayerBgManager.selectBg(this, PlayerBgManager.DEFAULT_BACKGROUND)
        galleryBgFragment.updateSelectId(PlayerBgManager.DEFAULT_BACKGROUND)
        mallBgFragment.updateSelectId(PlayerBgManager.DEFAULT_BACKGROUND)
        systemBgFragment.updateSelectId(PlayerBgManager.DEFAULT_BACKGROUND)
        showShortToast(getString(R.string.background_reset))
    }

    private fun isBackgroundUnlocked(background: PlayerBackground): Boolean {
        return when (background.bgType) {
            PlayerBackground.BACKGROUND_TYPE_SYSTEM -> {
                val totalMinutes = SPUtils.readInteger(this, MusicTimer.KEY_TOTAL_PLAY_MINUTES, 0)
                val needMinutes = getSystemBgRequireMinutes(background.id)
                totalMinutes >= needMinutes
            }
            PlayerBackground.BACKGROUND_TYPE_GALLERY,
            PlayerBackground.BACKGROUND_TYPE_MALL -> {
                SPUtils.readBoolean(this, background.id, false)
            }
            else -> false
        }
    }

    /**
     * 获取系统背景需要的听歌分钟。
     */
    private fun getSystemBgRequireMinutes(bgId: String): Int {
        return when (bgId) {
            PlayerBgFactory.PlayerBgId.SYSTEM_BACKGROUND_1 -> 1
            PlayerBgFactory.PlayerBgId.SYSTEM_BACKGROUND_2 -> 10
            PlayerBgFactory.PlayerBgId.SYSTEM_BACKGROUND_3 -> 60
            PlayerBgFactory.PlayerBgId.SYSTEM_BACKGROUND_4 -> 120
            else -> Int.MAX_VALUE
        }
    }

    companion object {

        // 购买弹窗标记
        const val BUY_MALL_BG = "buy_mall_bg"
    }
}
