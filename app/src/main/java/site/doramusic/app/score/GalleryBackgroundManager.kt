package site.doramusic.app.score

import android.content.Context
import androidx.annotation.DrawableRes
import site.doramusic.app.R
import site.doramusic.app.conf.AppConfig
import dora.util.SPUtils

/**
 * 抽卡画廊背景管理器。
 */
object GalleryBackgroundManager {

    private const val KEY_SELECTED_BACKGROUND = "selected_gallery_background"

    data class Background(
        val galleryId: String?,
        val nameRes: Int,
        @DrawableRes val backgroundRes: Int
    )

    val backgrounds = listOf(
//        Background(
//            galleryId = null,
//            nameRes = R.string.background_default,
//            backgroundRes = R.drawable.bg_default
//        ),
        Background(
            galleryId = AppConfig.GALLERY_RAIN_FOREST,
            nameRes = R.string.rain_forest,
            backgroundRes = R.drawable.bg_rain_forest
        ),
//        Background(
//            galleryId = AppConfig.GALLERY_DESERT,
//            nameRes = R.string.desert,
//            backgroundRes = R.drawable.bg_desert
//        ),
//        Background(
//            galleryId = AppConfig.GALLERY_CITY,
//            nameRes = R.string.city,
//            backgroundRes = R.drawable.bg_city
//        ),
//        Background(
//            galleryId = AppConfig.GALLERY_COUNTRYSIDE,
//            nameRes = R.string.countryside,
//            backgroundRes = R.drawable.bg_countryside
//        ),
//        Background(
//            galleryId = AppConfig.GALLERY_PLATEAU,
//            nameRes = R.string.plateau,
//            backgroundRes = R.drawable.bg_plateau
//        ),
//        Background(
//            galleryId = AppConfig.GALLERY_BEACH,
//            nameRes = R.string.beach,
//            backgroundRes = R.drawable.bg_beach
//        ),
//        Background(
//            galleryId = AppConfig.GALLERY_GLACIER,
//            nameRes = R.string.glacier,
//            backgroundRes = R.drawable.bg_glacier
//        ),
//        Background(
//            galleryId = AppConfig.GALLERY_MOUNTAIN,
//            nameRes = R.string.mountain,
//            backgroundRes = R.drawable.bg_mountain
//        ),
//        Background(
//            galleryId = AppConfig.GALLERY_UNDERSEA,
//            nameRes = R.string.undersea,
//            backgroundRes = R.drawable.bg_undersea
//        ),
//        Background(
//            galleryId = AppConfig.GALLERY_HIGHWAY,
//            nameRes = R.string.highway,
//            backgroundRes = R.drawable.bg_highway
//        )
    )

    fun isUnlocked(
        context: Context,
        background: Background
    ): Boolean {
        val galleryId = background.galleryId ?: return true
        return SPUtils.readBoolean(context, galleryId)
    }

    fun getSelectedBackground(context: Context): Background? {
        val galleryId = SPUtils.readString(
            context,
            KEY_SELECTED_BACKGROUND,
            ""
        )
        return backgrounds.firstOrNull {
            it.galleryId == galleryId
        }
    }

    fun selectBackground(
        context: Context,
        background: Background
    ) {
        if (!isUnlocked(context, background)) {
            return
        }
        SPUtils.writeString(
            context,
            KEY_SELECTED_BACKGROUND,
            background.galleryId ?: ""
        )
    }
}