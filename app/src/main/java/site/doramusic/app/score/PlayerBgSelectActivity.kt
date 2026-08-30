package site.doramusic.app.score

import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.GridLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import dora.util.RxBus
import dora.util.SPUtils
import dora.util.StatusBarUtils
import dora.widget.DoraTitleBar
import site.doramusic.app.R
import site.doramusic.app.conf.ARoutePath
import site.doramusic.app.conf.AppConfig
import site.doramusic.app.databinding.ActivityPlayerBgSelectBinding
import site.doramusic.app.event.PlayerBackgroundChangedEvent
import site.doramusic.app.ui.activity.BaseSkinActivity
import site.doramusic.app.util.ThemeSelector

/**
 * 播放器背景设置。
 *
 * 已集齐对应画廊后，可以使用该画廊的播放器背景。
 */
@Route(path = ARoutePath.ACTIVITY_PLAYER_BG_SELECT)
class PlayerBgSelectActivity :
    BaseSkinActivity<ActivityPlayerBgSelectBinding>(), AppConfig {

    private lateinit var adapter: GalleryBackgroundAdapter

    /**
     * 当前选中的背景。
     */
    private var selectedBackground: String = DEFAULT_BACKGROUND

    override fun getLayoutId(): Int {
        return R.layout.activity_player_bg_select
    }

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
            .addMenuButton(R.drawable.ic_clear_bg)
            .setOnIconClickListener(object : DoraTitleBar.OnIconClickListener {

                override fun onIconBackClick(icon: AppCompatImageView) {
                }

                override fun onIconMenuClick(
                    position: Int,
                    icon: AppCompatImageView
                ) {
                    resetBackground()
                }
            })
        // 当前选择的背景。
        selectedBackground = SPUtils.readString(
            this@PlayerBgSelectActivity,
            KEY_SELECTED_BACKGROUND,
            DEFAULT_BACKGROUND
        ) ?: DEFAULT_BACKGROUND
        val backgrounds = createBackgrounds().toMutableList()
        binding.recyclerView.layoutManager =
            GridLayoutManager(
                this@PlayerBgSelectActivity,
                2
            )
        // 将当前已保存的背景传给 Adapter，
        // 页面打开时立即显示正确的选中状态。
        adapter = GalleryBackgroundAdapter(
            backgrounds = backgrounds,
            selectedBackground = selectedBackground
        )
        adapter.setOnItemClickListener { adapter, _, position ->
            selectBackground(adapter.getItem(position) as GalleryBackground)
        }
        binding.recyclerView.adapter = adapter
    }

    /**
     * 选择背景。
     */
    private fun selectBackground(background: GalleryBackground) {
        if (!isBackgroundUnlocked(background)) {
            showShortToast(
                getString(R.string.background_not_unlocked)
            )
            return
        }
        selectedBackground = background.id
        SPUtils.writeString(
            this,
            KEY_SELECTED_BACKGROUND,
            selectedBackground
        )
        adapter.setSelectedBackground(selectedBackground)
        // 通知播放器立即刷新背景。
        RxBus.getInstance().post(
            PlayerBackgroundChangedEvent(
                backgroundRes = background.imageRes.takeIf { it != 0 }
            )
        )
        showShortToast(
            getString(
                R.string.background_selected,
                background.name
            )
        )
    }

    /**
     * 清空当前背景，恢复默认背景。
     */
    private fun resetBackground() {
        selectedBackground = DEFAULT_BACKGROUND
        SPUtils.remove(
            this,
            KEY_SELECTED_BACKGROUND
        )
        // 刷新选择页面。
        adapter.setSelectedBackground(DEFAULT_BACKGROUND)
        // 通知播放器恢复默认背景。
        RxBus.getInstance().post(
            PlayerBackgroundChangedEvent(
                backgroundRes = null
            )
        )
        showShortToast(getString(R.string.background_reset))
    }

    /**
     * 判断背景是否已经解锁。
     *
     * 默认背景始终解锁。
     * 其他背景要求对应画廊已经集齐。
     */
    private fun isBackgroundUnlocked(
        background: GalleryBackground
    ): Boolean {
        if (background.id == DEFAULT_BACKGROUND) {
            return true
        }

        return SPUtils.readBoolean(
            this,
            background.galleryId
        )
    }

    /**
     * 创建所有播放器背景。
     */
    private fun createBackgrounds(): List<GalleryBackground> {
        return listOf(
//            GalleryBackground(
//                id = DEFAULT_BACKGROUND,
//                galleryId = "",
//                name = getString(R.string.background_default),
//                imageRes = R.drawable.bg_default
//            ),
            GalleryBackground(
                id = AppConfig.GALLERY_RAIN_FOREST,
                galleryId = AppConfig.GALLERY_RAIN_FOREST,
                name = getString(R.string.rain_forest),
                imageRes = R.drawable.bg_rain_forest
            ),
//            GalleryBackground(
//                id = AppConfig.GALLERY_DESERT,
//                galleryId = AppConfig.GALLERY_DESERT,
//                name = getString(R.string.desert),
//                imageRes = R.drawable.bg_desert
//            ),
//            GalleryBackground(
//                id = AppConfig.GALLERY_CITY,
//                galleryId = AppConfig.GALLERY_CITY,
//                name = getString(R.string.city),
//                imageRes = R.drawable.bg_city
//            ),
//            GalleryBackground(
//                id = AppConfig.GALLERY_COUNTRYSIDE,
//                galleryId = AppConfig.GALLERY_COUNTRYSIDE,
//                name = getString(R.string.countryside),
//                imageRes = R.drawable.bg_countryside
//            ),
//            GalleryBackground(
//                id = AppConfig.GALLERY_PLATEAU,
//                galleryId = AppConfig.GALLERY_PLATEAU,
//                name = getString(R.string.plateau),
//                imageRes = R.drawable.bg_plateau
//            ),
//            GalleryBackground(
//                id = AppConfig.GALLERY_BEACH,
//                galleryId = AppConfig.GALLERY_BEACH,
//                name = getString(R.string.beach),
//                imageRes = R.drawable.bg_beach
//            ),
//            GalleryBackground(
//                id = AppConfig.GALLERY_GLACIER,
//                galleryId = AppConfig.GALLERY_GLACIER,
//                name = getString(R.string.glacier),
//                imageRes = R.drawable.bg_glacier
//            ),
//            GalleryBackground(
//                id = AppConfig.GALLERY_MOUNTAIN,
//                galleryId = AppConfig.GALLERY_MOUNTAIN,
//                name = getString(R.string.mountain),
//                imageRes = R.drawable.bg_mountain
//            ),
//            GalleryBackground(
//                id = AppConfig.GALLERY_UNDERSEA,
//                galleryId = AppConfig.GALLERY_UNDERSEA,
//                name = getString(R.string.undersea),
//                imageRes = R.drawable.bg_undersea
//            ),
//            GalleryBackground(
//                id = AppConfig.GALLERY_HIGHWAY,
//                galleryId = AppConfig.GALLERY_HIGHWAY,
//                name = getString(R.string.highway),
//                imageRes = R.drawable.bg_highway
//            )
        )
    }

    data class GalleryBackground(
        val id: String,
        val galleryId: String,
        val name: String,
        val imageRes: Int
    )

    companion object {
        private const val KEY_SELECTED_BACKGROUND =
            "selected_gallery_background"

        private const val DEFAULT_BACKGROUND =
            "default"
    }
}