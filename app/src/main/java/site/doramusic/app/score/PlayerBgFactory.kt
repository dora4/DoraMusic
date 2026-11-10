package site.doramusic.app.score

import android.content.Context
import site.doramusic.app.R
import site.doramusic.app.conf.AppConfig

object PlayerBgFactory {

    // 全部背景汇总
    fun getAllBackgroundList(context: Context): MutableList<PlayerBackground> {
        return mutableListOf<PlayerBackground>().apply {
            addAll(createGalleryBackgrounds(context))
            addAll(createSystemBackgrounds(context))
            addAll(createMallBackgrounds(context))
        }
    }

    // 图鉴背景
    fun createGalleryBackgrounds(context: Context): List<PlayerBackground> {
        return listOf(
            PlayerBackground(
                id = AppConfig.GALLERY_RAIN_FOREST,
                name = context.getString(R.string.rain_forest),
                imageRes = R.drawable.bg_rain_forest,
                bgType = PlayerBackground.BACKGROUND_TYPE_GALLERY
            ),
            PlayerBackground(
                id = AppConfig.GALLERY_GLACIER,
                name = context.getString(R.string.glacier),
                imageRes = R.drawable.bg_glacier,
                bgType = PlayerBackground.BACKGROUND_TYPE_GALLERY
            )
        )
    }

    // 系统解锁背景
    fun createSystemBackgrounds(context: Context): List<PlayerBackground> {
        return listOf(
            PlayerBackground(
                id = PlayerBgId.SYSTEM_BACKGROUND_1,
                name = context.getString(R.string.bg_name_system_1),
                imageRes = R.drawable.bg_system_1,
                bgType = PlayerBackground.BACKGROUND_TYPE_SYSTEM
            ),
            PlayerBackground(
                id = PlayerBgId.SYSTEM_BACKGROUND_2,
                name = context.getString(R.string.bg_name_system_2),
                imageRes = R.drawable.bg_system_2,
                bgType = PlayerBackground.BACKGROUND_TYPE_SYSTEM
            ),
            PlayerBackground(
                id = PlayerBgId.SYSTEM_BACKGROUND_3,
                name = context.getString(R.string.bg_name_system_3),
                imageRes = R.drawable.bg_system_3,
                bgType = PlayerBackground.BACKGROUND_TYPE_SYSTEM
            ),
            PlayerBackground(
                id = PlayerBgId.SYSTEM_BACKGROUND_4,
                name = context.getString(R.string.bg_name_system_4),
                imageRes = R.drawable.bg_system_4,
                bgType = PlayerBackground.BACKGROUND_TYPE_SYSTEM
            )
        )
    }

    // 商城付费背景
    fun createMallBackgrounds(context: Context): List<PlayerBackground> {
        return listOf(
            PlayerBackground(
                id = PlayerBgId.MALL_BACKGROUND_1,
                name = context.getString(R.string.bg_name_mall_1),
                price = 10000,
                imageRes = R.drawable.bg_mall_1,
                bgType = PlayerBackground.BACKGROUND_TYPE_MALL
            ),
            PlayerBackground(
                id = PlayerBgId.MALL_BACKGROUND_2,
                name = context.getString(R.string.bg_name_mall_2),
                price = 20000,
                imageRes = R.drawable.bg_mall_2,
                bgType = PlayerBackground.BACKGROUND_TYPE_MALL
            ),
            PlayerBackground(
                id = PlayerBgId.MALL_BACKGROUND_3,
                name = context.getString(R.string.bg_name_mall_3),
                price = 30000,
                imageRes = R.drawable.bg_mall_3,
                bgType = PlayerBackground.BACKGROUND_TYPE_MALL
            ),
            PlayerBackground(
                id = PlayerBgId.MALL_BACKGROUND_4,
                name = context.getString(R.string.bg_name_mall_4),
                price = 40000,
                imageRes = R.drawable.bg_mall_4,
                bgType = PlayerBackground.BACKGROUND_TYPE_MALL
            )
        )
    }

    object PlayerBgId {
        const val MALL_BACKGROUND_1 = "mall_bg_1"
        const val MALL_BACKGROUND_2 = "mall_bg_2"
        const val MALL_BACKGROUND_3 = "mall_bg_3"
        const val MALL_BACKGROUND_4 = "mall_bg_4"

        const val SYSTEM_BACKGROUND_1 = "system_bg_1"
        const val SYSTEM_BACKGROUND_2 = "system_bg_2"
        const val SYSTEM_BACKGROUND_3 = "system_bg_3"
        const val SYSTEM_BACKGROUND_4 = "system_bg_4"
    }
}
