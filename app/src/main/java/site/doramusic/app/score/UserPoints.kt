package site.doramusic.app.score

import dora.db.constraint.Id
import dora.db.migration.OrmMigration
import dora.db.table.Column
import dora.db.table.OrmTable
import dora.db.table.Table

@Table("user_points")
data class UserPoints(
    @Id
    val id: Long = OrmTable.ID_UNASSIGNED,
    @Column("total_points")
    val totalPoints: Int = 0,
    override val isUpgradeRecreated: Boolean = false,
    override val migrations: Array<OrmMigration>? = arrayOf()
) : OrmTable

@Table("points_record")
data class PointsRecord(
    @Id
    val id: Long = OrmTable.ID_UNASSIGNED,
    @Column("type")
    val type: String,
    @Column("points")
    val points: Int,
    @Column("timestamp")
    val timestamp: Long = System.currentTimeMillis(),
    @Column("extra")
    val extra: String? = null,
    override val isUpgradeRecreated: Boolean = false,
    override val migrations: Array<OrmMigration>? = arrayOf()
) : OrmTable