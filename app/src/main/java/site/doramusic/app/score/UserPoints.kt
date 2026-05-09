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
) : OrmTable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserPoints

        if (id != other.id) return false
        if (totalPoints != other.totalPoints) return false
        if (isUpgradeRecreated != other.isUpgradeRecreated) return false
        if (!migrations.contentEquals(other.migrations)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + totalPoints
        result = 31 * result + isUpgradeRecreated.hashCode()
        result = 31 * result + (migrations?.contentHashCode() ?: 0)
        return result
    }
}

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
) : OrmTable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PointsRecord

        if (id != other.id) return false
        if (points != other.points) return false
        if (timestamp != other.timestamp) return false
        if (isUpgradeRecreated != other.isUpgradeRecreated) return false
        if (type != other.type) return false
        if (extra != other.extra) return false
        if (!migrations.contentEquals(other.migrations)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + points
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + isUpgradeRecreated.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + (extra?.hashCode() ?: 0)
        result = 31 * result + (migrations?.contentHashCode() ?: 0)
        return result
    }
}