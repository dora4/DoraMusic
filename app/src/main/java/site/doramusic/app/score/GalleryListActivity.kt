package site.doramusic.app.score

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route

import dora.BaseActivity

import dora.db.builder.WhereBuilder
import dora.db.dao.DaoFactory
import dora.util.StatusBarUtils
import site.doramusic.app.R
import site.doramusic.app.conf.ARoutePath
import site.doramusic.app.conf.AppConfig
import site.doramusic.app.databinding.ActivityGalleryListBinding
import site.doramusic.app.util.ThemeSelector

/**
 * 图鉴列表，每个大版本更新。
 */
@Route(path = ARoutePath.ACTIVITY_GALLERY_LIST)
class GalleryListActivity : BaseActivity<ActivityGalleryListBinding>() {

    private lateinit var adapter: CardPackAdapter

    override fun getLayoutId(): Int = R.layout.activity_gallery_list

    override fun onSetStatusBar() {
        StatusBarUtils.setTransparencyStatusBar(this)
    }

    override fun initData(savedInstanceState: Bundle?, binding: ActivityGalleryListBinding) {
        binding.statusbarGalleryList.layoutParams = LinearLayout
            .LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, StatusBarUtils.getStatusBarHeight())
        ThemeSelector.applyViewTheme(binding.statusbarGalleryList)
        ThemeSelector.applyViewTheme(binding.titlebarGalleryList)
        binding.recyclerView.layoutManager = GridLayoutManager(this, 2)
        adapter = CardPackAdapter(getCardPacks()) { pack ->
            val intent = Intent(this, DrawCardActivity::class.java)
            intent.putExtra(DrawCardActivity.GALLERY_ID, pack.id)
            intent.putExtra(DrawCardActivity.GALLERY_NAME, pack.name)
            startActivityForResult(intent, AppConfig.REQUEST_CODE_DRAW_CARD)
        }
        binding.recyclerView.adapter = adapter
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AppConfig.REQUEST_CODE_DRAW_CARD) {
            // 抽卡返回后刷新每个卡牌包的已抽数量
            adapter.setList(getCardPacks())
        }
    }

    /** 获取卡包列表及已抽数量 */
    private fun getCardPacks(): List<CardPack> {
        // 新加的卡包排在最前面
        val galleryIds = listOf(
            AppConfig.GALLERY_RAIN_FOREST to getString(R.string.rain_forest),
//            AppConfig.GALLERY_DESERT to "沙漠",
//            AppConfig.GALLERY_CITY to "都市",
//            AppConfig.GALLERY_COUNTRYSIDE to "田园",
//            AppConfig.GALLERY_PLATEAU to "高原",
//            AppConfig.GALLERY_BEACH to "海滩",
//            AppConfig.GALLERY_GLACIER to "冰川",
//            AppConfig.GALLERY_MOUNTAIN to "山地",
//            AppConfig.GALLERY_UNDERSEA to "海底",
//            AppConfig.GALLERY_HIGHWAY to "公路"
        ).reversed()

        return galleryIds.map { (id, name) ->
            CardPack(
                id = id,
                name = name,
                drawableRes = when (id) {
                    AppConfig.GALLERY_RAIN_FOREST -> R.drawable.bg_rain_forest
//                    AppConfig.GALLERY_DESERT -> R.drawable.bg_desert
//                    AppConfig.GALLERY_CITY -> R.drawable.bg_city
//                    AppConfig.GALLERY_COUNTRYSIDE -> R.drawable.bg_countryside
//                    AppConfig.GALLERY_PLATEAU -> R.drawable.bg_plateau
//                    AppConfig.GALLERY_BEACH -> R.drawable.bg_beach
//                    AppConfig.GALLERY_GLACIER -> R.drawable.bg_glacier
//                    AppConfig.GALLERY_MOUNTAIN -> R.drawable.bg_mountain
//                    AppConfig.GALLERY_UNDERSEA -> R.drawable.bg_undersea
//                    AppConfig.GALLERY_HIGHWAY -> R.drawable.bg_highway
                    else -> R.drawable.bg_rain_forest
                },
                ownNum = DaoFactory.getDao(GalleryCard::class.java).count(
                    WhereBuilder.create().addWhereEqualTo("gallery_id", id)
                        .andWhereEqualTo("is_drawn", true)
                ).toInt()
            )
        }
    }
}
