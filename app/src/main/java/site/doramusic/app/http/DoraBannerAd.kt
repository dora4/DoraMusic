package site.doramusic.app.http

import dora.db.constraint.Id
import dora.db.migration.OrmMigration
import dora.db.table.Column
import dora.db.table.OrmTable
import dora.db.table.Table

@Table("dora_banner_ad")
class DoraBannerAd : OrmTable {

    @Id
    private val id: Long = OrmTable.ID_UNASSIGNED
    @Column("img_url")
    val imgUrl: String? = null
    @Column("detail_url")
    val detailUrl: String? = null

    override val isUpgradeRecreated: Boolean
        get() = false
    override val migrations: Array<OrmMigration>
        get() = arrayOf()
}