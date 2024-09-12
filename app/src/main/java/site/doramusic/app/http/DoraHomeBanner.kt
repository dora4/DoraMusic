package site.doramusic.app.http

import dora.db.constraint.Id
import dora.db.migration.OrmMigration
import dora.db.table.OrmTable

class DoraHomeBanner : OrmTable {
    val imgUrl: String? = null
    val detailUrl: String? = null

    @Id
    private val id: Long = 0
    override val isUpgradeRecreated: Boolean
        get() = false
    override val migrations: Array<OrmMigration>
        get() = arrayOf()
}