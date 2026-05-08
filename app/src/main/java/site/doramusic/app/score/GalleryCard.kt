package site.doramusic.app.score

import dora.db.constraint.Id
import dora.db.migration.OrmMigration
import dora.db.table.Column
import dora.db.table.OrmTable

data class GalleryCard(
    @Id
    val id: Long = OrmTable.ID_UNASSIGNED,
    @Column("number")
    val number: Int = 0,
    @Column("gallery_id")
    val galleryId: String = "",
    @Column("is_drawn")
    var isDrawn: Boolean = false,
    @Column("probability")
    val probability: Double = 0.0,
    override val isUpgradeRecreated: Boolean = false,
    override val migrations: Array<OrmMigration>? = arrayOf()
) : OrmTable

class Gallery(val id: String, private val cards: List<GalleryCard>) {

    /**
     * 正常抽卡：抽出的卡标记为已抽出。
     */
    fun drawCard(): GalleryCard? {
        val availableCards = cards.filter { !it.isDrawn }
        if (availableCards.isEmpty()) return null
        val totalProb = availableCards.sumOf { it.probability }
        val rand = Math.random() * totalProb
        var cumulative = 0.0
        for (card in availableCards) {
            cumulative += card.probability
            if (rand <= cumulative) {
                card.isDrawn = true
                return card
            }
        }
        return null
    }

    /**
     * 独立概率抽卡：抽出来的卡不改变原卡的概率和状态。
     */
    fun drawCardIgnoreDrawn(): GalleryCard? {
        if (cards.isEmpty()) return null
        val totalProb = cards.sumOf { it.probability }
        val rand = Math.random() * totalProb
        var cumulative = 0.0
        for (card in cards) {
            cumulative += card.probability
            if (rand <= cumulative) {
                // 这里不修改 card.isDrawn
                return card.copy() // 返回一个副本，不影响原始状态
            }
        }
        return null
    }
}

enum class CardRarity(
    val probability: Double,
    val displayName: String
) {

    L(0.001, "传说"),
    U(0.003, "极品"),
    SSR(0.004, "超超稀有"),
    SR(0.006, "超级稀有"),
    R(0.008, "稀有"),
    P(0.012, "完美"),
    B(0.015, "精品"),
    E(0.018, "卓越"),
    F(0.020, "精美"),
    N1(0.022, "一级普通"),
    N2(0.024, "二级普通"),
    N3(0.025, "三级普通"),
    N4(0.028, "四级普通"),
    N5(0.032, "五级普通"),
    N6(0.035, "六级普通")
}
