package site.doramusic.app.http

import dora.db.constraint.Id
import dora.db.migration.OrmMigration
import dora.db.table.Column
import dora.db.table.OrmTable
import dora.db.table.Table

@Table("dora_banner_ad")
class DoraBannerAd : OrmTable {

    @Column("img_url")
    val imgUrl: String? = null
    @Column("detail_url")
    val detailUrl: String? = null

    @Id
    private val id: Long = 0
    override val isUpgradeRecreated: Boolean
        get() = false
    override val migrations: Array<OrmMigration>
        get() = arrayOf()
}