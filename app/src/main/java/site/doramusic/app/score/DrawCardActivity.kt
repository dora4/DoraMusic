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
import dora.util.IntentUtils
import dora.util.SPUtils
import dora.util.StatusBarUtils
import site.doramusic.app.R
import site.doramusic.app.conf.ARoutePath
import site.doramusic.app.conf.AppConfig
import site.doramusic.app.databinding.ActivityDrawCardBinding
import site.doramusic.app.util.ThemeSelector

/**
 * 抽卡界面。
 */
@Route(path = ARoutePath.ACTIVITY_DRAW_CARD)
class DrawCardActivity : BaseActivity<ActivityDrawCardBinding>() {

    private lateinit var galleryId: String
    private lateinit var galleryName: String

    override fun getLayoutId(): Int = R.layout.activity_draw_card

    override fun onGetExtras(action: String?, bundle: Bundle?, intent: Intent) {
        super.onGetExtras(action, bundle, intent)
        galleryId = IntentUtils.getStringExtra(intent, GALLERY_ID)
        galleryName = IntentUtils.getStringExtra(intent, GALLERY_NAME)
    }

    override fun onSetStatusBar() {
        StatusBarUtils.setTransparencyStatusBar(this)
    }

    override fun initData(savedInstanceState: Bundle?, binding: ActivityDrawCardBinding) {
        binding.statusbarDrawCard.layoutParams = LinearLayout
            .LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, StatusBarUtils.getStatusBarHeight())
        ThemeSelector.applyViewTheme(binding.statusbarDrawCard)
        ThemeSelector.applyViewTheme(binding.titlebarDrawCard)
        binding.tvGalleryName.text = getString(R.string.gallery_name_format, galleryName)
        binding.tvMyPoints.text =
            getString(R.string.my_points_format, PointsManager.getTotalPoints())
        ThemeSelector.applyViewTheme(binding.btnDrawCard)
        val gallery = Gallery(
            galleryId,
            listOf(
                // L（Legendary）传说
                GalleryCard(number = 0, galleryId = galleryId, probability = CardRarity.L.probability),
                // U（Ultimate）极品
                GalleryCard(number = 1, galleryId = galleryId, probability = CardRarity.U.probability),
                // SSR（Super Super Rare）超超稀有
                GalleryCard(number = 2, galleryId = galleryId, probability = CardRarity.SSR.probability),
                GalleryCard(number = 3, galleryId = galleryId, probability = CardRarity.SSR.probability),
                GalleryCard(number = 4, galleryId = galleryId, probability = CardRarity.SSR.probability),
                GalleryCard(number = 5, galleryId = galleryId, probability = CardRarity.SSR.probability),
                // SR（Super Rare）超级稀有
                GalleryCard(number = 6, galleryId = galleryId, probability = CardRarity.SR.probability),
                GalleryCard(number = 7, galleryId = galleryId, probability = CardRarity.SR.probability),
                GalleryCard(number = 8, galleryId = galleryId, probability = CardRarity.SR.probability),
                GalleryCard(number = 9, galleryId = galleryId, probability = CardRarity.SR.probability),
                // R（Rare） 稀有
                GalleryCard(number = 10, galleryId = galleryId, probability = CardRarity.R.probability),
                GalleryCard(number = 11, galleryId = galleryId, probability = CardRarity.R.probability),
                GalleryCard(number = 12, galleryId = galleryId, probability = CardRarity.R.probability),
                GalleryCard(number = 13, galleryId = galleryId, probability = CardRarity.R.probability),
                // P（Perfect）完美
                GalleryCard(number = 14, galleryId = galleryId, probability = CardRarity.P.probability),
                GalleryCard(number = 15, galleryId = galleryId, probability = CardRarity.P.probability),
                GalleryCard(number = 16, galleryId = galleryId, probability = CardRarity.P.probability),
                GalleryCard(number = 17, galleryId = galleryId, probability = CardRarity.P.probability),
                // B（Boutique）精品
                GalleryCard(number = 18, galleryId = galleryId, probability = CardRarity.B.probability),
                GalleryCard(number = 19, galleryId = galleryId, probability = CardRarity.B.probability),
                GalleryCard(number = 20, galleryId = galleryId, probability = CardRarity.B.probability),
                GalleryCard(number = 21, galleryId = galleryId, probability = CardRarity.B.probability),
                // E（Exceptional）卓越
                GalleryCard(number = 22, galleryId = galleryId, probability = CardRarity.E.probability),
                GalleryCard(number = 23, galleryId = galleryId, probability = CardRarity.E.probability),
                GalleryCard(number = 24, galleryId = galleryId, probability = CardRarity.E.probability),
                GalleryCard(number = 25, galleryId = galleryId, probability = CardRarity.E.probability),
                // F（Fine）精美
                GalleryCard(number = 26, galleryId = galleryId, probability = CardRarity.F.probability),
                GalleryCard(number = 27, galleryId = galleryId, probability = CardRarity.F.probability),
                GalleryCard(number = 28, galleryId = galleryId, probability = CardRarity.F.probability),
                GalleryCard(number = 29, galleryId = galleryId, probability = CardRarity.F.probability),
                // N1（Normal） 一级普通
                GalleryCard(number = 30, galleryId = galleryId, probability = CardRarity.N1.probability),
                GalleryCard(number = 31, galleryId = galleryId, probability = CardRarity.N1.probability),
                GalleryCard(number = 32, galleryId = galleryId, probability = CardRarity.N1.probability),
                GalleryCard(number = 33, galleryId = galleryId, probability = CardRarity.N1.probability),
                // N2（Normal） 二级普通
                GalleryCard(number = 34, galleryId = galleryId, probability = CardRarity.N2.probability),
                GalleryCard(number = 35, galleryId = galleryId, probability = CardRarity.N2.probability),
                GalleryCard(number = 36, galleryId = galleryId, probability = CardRarity.N2.probability),
                GalleryCard(number = 37, galleryId = galleryId, probability = CardRarity.N2.probability),
                // N3（Normal） 三级普通
                GalleryCard(number = 38, galleryId = galleryId, probability = CardRarity.N3.probability),
                GalleryCard(number = 39, galleryId = galleryId, probability = CardRarity.N3.probability),
                GalleryCard(number = 40, galleryId = galleryId, probability = CardRarity.N3.probability),
                GalleryCard(number = 41, galleryId = galleryId, probability = CardRarity.N3.probability),
                // N4（Normal） 四级普通
                GalleryCard(number = 42, galleryId = galleryId, probability = CardRarity.N4.probability),
                GalleryCard(number = 43, galleryId = galleryId, probability = CardRarity.N4.probability),
                GalleryCard(number = 44, galleryId = galleryId, probability = CardRarity.N4.probability),
                GalleryCard(number = 45, galleryId = galleryId, probability = CardRarity.N4.probability),
                // N5（Normal） 五级普通
                GalleryCard(number = 46, galleryId = galleryId, probability = CardRarity.N5.probability),
                GalleryCard(number = 47, galleryId = galleryId, probability = CardRarity.N5.probability),
                GalleryCard(number = 48, galleryId = galleryId, probability = CardRarity.N5.probability),
                GalleryCard(number = 49, galleryId = galleryId, probability = CardRarity.N5.probability),
                // N6（Normal） 六级普通
                GalleryCard(number = 50, galleryId = galleryId, probability = CardRarity.N6.probability),
                GalleryCard(number = 51, galleryId = galleryId, probability = CardRarity.N6.probability),
                GalleryCard(number = 52, galleryId = galleryId, probability = CardRarity.N6.probability),
                GalleryCard(number = 53, galleryId = galleryId, probability = CardRarity.N6.probability)
            )
        )

        val galleryCards = (53 downTo 0).map { number ->
            // 查询数据库，是否已经拥有
            val ownCard = DaoFactory.getDao(GalleryCard::class.java)
                .selectOne(
                    WhereBuilder.create()
                        .addWhereEqualTo("number", number)
                        .andWhereEqualTo("gallery_id", galleryId)
                )

            GalleryCard(
                number = number,
                galleryId = galleryId,
                isDrawn = ownCard != null,  // 如果数据库有，则标记已抽
                probability = 0.0
            )
        }.toMutableList()

        val adapter = GalleryCardAdapter(
            cards = galleryCards,
            getCardImage = { number -> getBackImage(galleryId, number) },  // 这里 getBackImage 返回正面资源
            onCardDraw = { card, pokerView ->
            }
        )

        binding.recyclerView.layoutManager = GridLayoutManager(this, 6)
        binding.recyclerView.adapter = adapter
        binding.btnDrawCard.setOnClickListener {
            if (SPUtils.readBoolean(this@DrawCardActivity, galleryId)) {
                showShortToast(getString(R.string.collection_complete))
                return@setOnClickListener
            }
            val totalPoints = PointsManager.getTotalPoints()
            // 先扣除积分
            if (totalPoints >= 100) {
                PointsManager.addPoints(PointsSource.GACHA.desc, -100)
                binding.tvMyPoints.text = getString(R.string.my_points_format, PointsManager.getTotalPoints())

            } else {
                showShortToast(getString(R.string.insufficient_points))
                return@setOnClickListener
            }
            val card = gallery.drawCardIgnoreDrawn() // 随机抽一张
            if (card != null) {
                card.isDrawn = true
                val index = galleryCards.indexOfFirst { it.number == card.number }
                if (index != -1) {
                    val holder = binding.recyclerView.findViewHolderForAdapterPosition(index)
                    if (holder is GalleryCardAdapter.CardViewHolder) {
                        val pokerView = holder.pokerView
                        val isDrawn = galleryCards[index].isDrawn

                        if (isDrawn) {
                            // 如果已经翻出过，先翻回去再翻出
                            pokerView.reset()                 // 重置到背面
                            pokerView.setBackImage(getBackImage(galleryId, card.number)) // 设置正面图片
                            pokerView.flipCard()              // 翻出正面
                        } else {
                            // 直接翻出
                            pokerView.setBackImage(getBackImage(galleryId, card.number))
                            pokerView.reset()
                            pokerView.flipCard()
                        }
                    }

                    // 标记已抽
                    galleryCards[index].isDrawn = true

                    // 保存到数据库
                    val ownCard = DaoFactory.getDao(GalleryCard::class.java)
                        .selectOne(
                            WhereBuilder.create()
                                .addWhereEqualTo("number", card.number)
                                .andWhereEqualTo("gallery_id", galleryId)
                        )
                    if (ownCard == null) {
                        DaoFactory.getDao(GalleryCard::class.java).insert(card)
                        val num = DaoFactory.getDao(GalleryCard::class.java).count(
                            WhereBuilder.create()
                                .addWhereEqualTo("gallery_id", galleryId)
                                .andWhereEqualTo("is_drawn", true)
                        )
                        if (num >= 54 && !SPUtils.readBoolean(this@DrawCardActivity, galleryId)) {
                            SPUtils.writeBoolean(this@DrawCardActivity, galleryId, true)
                            showLongToast(getString(R.string.collection_complete))
                        }
                    }
                    adapter.notifyItemChanged(index)
                }
            } else {
                showShortToast(getString(R.string.you_already_own_this_card))
            }
        }
    }

    private fun getBackImage(galleryId: String, number: Int): Int {
        fun prefixFor(galleryId: String): String = when (galleryId) {
            AppConfig.GALLERY_RAIN_FOREST -> "rain_forest"
            AppConfig.GALLERY_DESERT -> "desert"
            AppConfig.GALLERY_CITY -> "city"
            AppConfig.GALLERY_COUNTRYSIDE -> "countryside"
            AppConfig.GALLERY_PLATEAU -> "plateau"
            AppConfig.GALLERY_BEACH -> "beach"
            AppConfig.GALLERY_GLACIER -> "glacier"
            AppConfig.GALLERY_MOUNTAIN -> "mountain"
            AppConfig.GALLERY_UNDERSEA -> "undersea"
            AppConfig.GALLERY_HIGHWAY -> "highway"
            else -> "unnamed"
        }
        val prefix = prefixFor(galleryId)
        return R.drawable::class.java.getField("${prefix}_${54 - number}").getInt(null)
    }

    companion object {
        const val GALLERY_ID = "gallery_id"
        const val GALLERY_NAME = "gallery_name"
    }
}
