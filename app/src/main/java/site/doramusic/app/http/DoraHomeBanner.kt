package site.doramusic.app.http

import dora.db.constraint.Id
import dora.db.migration.OrmMigration
import dora.db.table.OrmTable
import dora.db.table.PrimaryKeyEntry
import dora.db.table.PrimaryKeyId

class DoraHomeBanner : OrmTable {
    val imgUrl: String? = null
    val detailUrl: String? = null

    @Id
    private val id: Long = 0
    override val primaryKey: PrimaryKeyEntry
        get() = PrimaryKeyId(id)
    override val isUpgradeRecreated: Boolean
        get() = false
    override val migrations: Array<OrmMigration>
        get() = arrayOf()
}