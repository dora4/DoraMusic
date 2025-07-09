package site.doramusic.app.model

import dora.db.constraint.Id
import dora.db.constraint.Unique
import dora.db.migration.OrmMigration
import dora.db.table.Column
import dora.db.table.OrmTable
import dora.db.table.Since

@Since(version = 2)
data class Donation(
    @Id
    val id: Long = 0,
    @Unique
    @Column("order_id")
    val orderId: String,
    @Column("pending")
    var pending: Boolean = false,
    @Column("token_amount")
    val tokenAmount: Double,
    @Column("token_symbol")
    val tokenSymbol: String,
    @Column("timestamp")
    val timestamp: Long,
    @Column("transaction_hash")
    var transactionHash: String = "",
    override val isUpgradeRecreated: Boolean = false,
    override val migrations: Array<OrmMigration>? = arrayOf()
) : OrmTable