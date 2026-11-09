package site.doramusic.app.score

import android.content.res.Configuration
import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import com.alibaba.android.arouter.facade.annotation.Route
import dora.BaseActivity
import dora.util.RxBus
import dora.util.SPUtils
import dora.util.StatusBarUtils
import dora.widget.DoraTitleBar
import site.doramusic.app.R
import site.doramusic.app.conf.ARoutePath
import site.doramusic.app.conf.AppConfig
import site.doramusic.app.databinding.ActivityPlayerBgSelectBinding
import site.doramusic.app.event.PlayerBackgroundChangedEvent
import site.doramusic.app.util.ThemeSelector

@Route(path = ARoutePath.ACTIVITY_PLAYER_BG_SELECT)
class PlayerBgSelectActivity :
    BaseActivity<ActivityPlayerBgSelectBinding>(), AppConfig {

    // 切换开关
    private val useViewPager2 = false
//     private val useViewPager2 = true

    private lateinit var galleryBgFragment: BackgroundListFragment
    private lateinit var mallBgFragment: BackgroundListFragment
    private lateinit var systemBgFragment: BackgroundListFragment
    private lateinit var tabStrategy: TabRenderStrategy
    private var selectedBackground: String = DEFAULT_BACKGROUND

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
            .addMenuButton(R.drawable.ic_clear_bg,
                resources.getDimension(R.dimen.title_bar_icon_size).toInt())
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
        selectedBackground = SPUtils.readString(
            this,
            KEY_SELECTED_BACKGROUND,
            DEFAULT_BACKGROUND
        ) ?: DEFAULT_BACKGROUND
        val galleryBgList = createGalleryBackgrounds()
        val mallBgList = createMallBackgrounds()
        val systemBgList = createSystemBackgrounds()
        galleryBgFragment = BackgroundListFragment(galleryBgList.toMutableList(), selectedBackground) {
            selectBackground(it)
        }
        mallBgFragment = BackgroundListFragment(mallBgList.toMutableList(), selectedBackground) {
            selectBackground(it)
        }
        systemBgFragment = BackgroundListFragment(systemBgList.toMutableList(), selectedBackground) {
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
        if(::tabStrategy.isInitialized){
            tabStrategy.release()
        }
    }

    /**
     * 选择背景。
     */
    private fun selectBackground(background: PlayerBackground) {
        if (!isBackgroundUnlocked(background)) {
            showShortToast(getString(R.string.background_not_unlocked))
            return
        }
        selectedBackground = background.id
        SPUtils.writeString(this, KEY_SELECTED_BACKGROUND, selectedBackground)
        // fragment同步选中状态
        galleryBgFragment.updateSelectId(selectedBackground)
        mallBgFragment.updateSelectId(selectedBackground)
        systemBgFragment.updateSelectId(selectedBackground)
        RxBus.getInstance().post(
            PlayerBackgroundChangedEvent(backgroundRes = background.imageRes.takeIf { it != 0 })
        )
        showShortToast(getString(R.string.background_selected, background.name))
    }

    /**
     * 清空当前背景，恢复默认背景。
     */
    private fun resetBackground() {
        selectedBackground = DEFAULT_BACKGROUND
        SPUtils.remove(this, KEY_SELECTED_BACKGROUND)
        galleryBgFragment.updateSelectId(DEFAULT_BACKGROUND)
        mallBgFragment.updateSelectId(DEFAULT_BACKGROUND)
        systemBgFragment.updateSelectId(DEFAULT_BACKGROUND)
        RxBus.getInstance().post(PlayerBackgroundChangedEvent(backgroundRes = null))
        showShortToast(getString(R.string.background_reset))
    }

    private fun isBackgroundUnlocked(background: PlayerBackground): Boolean {
        if (background.id == DEFAULT_BACKGROUND) return true
        if (background.bgType == PlayerBackground.BACKGROUND_TYPE_SYSTEM) return true
        return when (background.bgType) {
            PlayerBackground.BACKGROUND_TYPE_GALLERY -> {
                SPUtils.readBoolean(this, background.galleryId, false)
            }
            else -> { // PlayerBackground.BACKGROUND_TYPE_MALL
                SPUtils.readBoolean(this, "$PREFIX_MALL_BACKGROUND${background.id}", false)
            }
        }
    }

    private fun createGalleryBackgrounds(): List<PlayerBackground> {
        return listOf(
            PlayerBackground(
                id = AppConfig.GALLERY_RAIN_FOREST,
                galleryId = AppConfig.GALLERY_RAIN_FOREST,
                name = getString(R.string.rain_forest),
                imageRes = R.drawable.bg_rain_forest,
                bgType = PlayerBackground.BACKGROUND_TYPE_GALLERY
            ),
            PlayerBackground(
                id = AppConfig.GALLERY_GLACIER,
                galleryId = AppConfig.GALLERY_GLACIER,
                name = getString(R.string.glacier),
                imageRes = R.drawable.bg_glacier,
                bgType = PlayerBackground.BACKGROUND_TYPE_GALLERY
            )
        )
    }

    private fun createSystemBackgrounds(): List<PlayerBackground> {
        return listOf(
            PlayerBackground(
                id = SYSTEM_BACKGROUND_1,
                galleryId = "",
                name = getString(R.string.bg_name_system_1),
                imageRes = R.drawable.bg_system_1,
                bgType = PlayerBackground.BACKGROUND_TYPE_SYSTEM
            ),
            PlayerBackground(
                id = SYSTEM_BACKGROUND_2,
                galleryId = "",
                name = getString(R.string.bg_name_system_2),
                imageRes = R.drawable.bg_system_2,
                bgType = PlayerBackground.BACKGROUND_TYPE_SYSTEM
            ),
            PlayerBackground(
                id = SYSTEM_BACKGROUND_3,
                galleryId = "",
                name = getString(R.string.bg_name_system_3),
                imageRes = R.drawable.bg_system_3,
                bgType = PlayerBackground.BACKGROUND_TYPE_SYSTEM
            ),
            PlayerBackground(
                id = SYSTEM_BACKGROUND_4,
                galleryId = "",
                name = getString(R.string.bg_name_system_4),
                imageRes = R.drawable.bg_system_4,
                bgType = PlayerBackground.BACKGROUND_TYPE_SYSTEM
            )
        )
    }

    private fun createMallBackgrounds(): List<PlayerBackground> {
        return listOf(
            PlayerBackground(
                id = MALL_BACKGROUND_1,
                galleryId = "",
                name = getString(R.string.bg_name_mall_1),
                imageRes = R.drawable.bg_mall_1,
                bgType = PlayerBackground.BACKGROUND_TYPE_MALL
            ),
            PlayerBackground(
                id = MALL_BACKGROUND_2,
                galleryId = "",
                name = getString(R.string.bg_name_mall_2),
                imageRes = R.drawable.bg_mall_2,
                bgType = PlayerBackground.BACKGROUND_TYPE_MALL
            ),
            PlayerBackground(
                id = MALL_BACKGROUND_3,
                galleryId = "",
                name = getString(R.string.bg_name_mall_3),
                imageRes = R.drawable.bg_mall_3,
                bgType = PlayerBackground.BACKGROUND_TYPE_MALL
            ),
            PlayerBackground(
                id = MALL_BACKGROUND_4,
                galleryId = "",
                name = getString(R.string.bg_name_mall_4),
                imageRes = R.drawable.bg_mall_4,
                bgType = PlayerBackground.BACKGROUND_TYPE_MALL
            )
        )
    }

    companion object {
        private const val KEY_SELECTED_BACKGROUND = "selected_gallery_background"
        private const val DEFAULT_BACKGROUND = "default"
        private const val PREFIX_MALL_BACKGROUND = "mall_bg_"
        private const val MALL_BACKGROUND_1 = "mall_bg_1"
        private const val MALL_BACKGROUND_2 = "mall_bg_2"
        private const val MALL_BACKGROUND_3 = "mall_bg_3"
        private const val MALL_BACKGROUND_4 = "mall_bg_4"
        private const val SYSTEM_BACKGROUND_1 = "system_bg_1"
        private const val SYSTEM_BACKGROUND_2 = "system_bg_2"
        private const val SYSTEM_BACKGROUND_3 = "system_bg_3"
        private const val SYSTEM_BACKGROUND_4 = "system_bg_4"
    }
}
