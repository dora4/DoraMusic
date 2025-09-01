package site.doramusic.app.model

import dora.db.constraint.Id
import dora.db.constraint.Unique
import dora.db.migration.OrmMigration
import dora.db.table.Column
import dora.db.table.OrmTable
import dora.db.table.Since
import site.doramusic.app.base.conf.AppConfig.Companion.COLUMN_ORDER_ID
import site.doramusic.app.base.conf.AppConfig.Companion.COLUMN_PENDING
import site.doramusic.app.base.conf.AppConfig.Companion.COLUMN_TIMESTAMP
import site.doramusic.app.base.conf.AppConfig.Companion.COLUMN_TOKEN_AMOUNT
import site.doramusic.app.base.conf.AppConfig.Companion.COLUMN_TOKEN_SYMBOL
import site.doramusic.app.base.conf.AppConfig.Companion.COLUMN_TRANSACTION_HASH

@Since(version = 3)
data class Donation(
    @Id
    val id: Long = 0,
    @Unique
    @Column(COLUMN_ORDER_ID)
    val orderId: String = "",
    @Column(COLUMN_TOKEN_AMOUNT)
    val tokenAmount: Double = 0.0,
    @Column(COLUMN_TOKEN_SYMBOL)
    val tokenSymbol: String = "POL",
    @Column(COLUMN_TIMESTAMP)
    val timestamp: Long = 0,
    @Column(COLUMN_PENDING)
    var pending: Boolean = false,
    @Column(COLUMN_TRANSACTION_HASH)
    var transactionHash: String = "",
    override val isUpgradeRecreated: Boolean = false,
    override val migrations: Array<OrmMigration>? = arrayOf()
) : OrmTable