package site.doramusic.app.stat

import dora.db.constraint.Id
import dora.db.migration.OrmMigration
import dora.db.table.OrmTable
import dora.db.table.Column
import dora.db.table.Table

/**
 * 播放记录明细，一次播放行为一条（听完/中途切歌都生成）。
 */
@Table("play_record")
data class PlayRecord(

    @Id
    val id: Long = OrmTable.ID_UNASSIGNED,

    /**
     * 歌曲id。
     */
    @Column("song_id")
    var songId: Int = -1,

    /**
     * 歌曲名称。
     */
    @Column("song_name")
    var songName: String = "",

    /**
     * 本次播放结束时间戳 ms（听完/切歌那一刻）。
     */
    @Column("play_time")
    var playTime: Long = 0L,

    /**
     * 歌曲原始总时长 ms（固定歌曲全长，兼容旧逻辑）。
     */
    @Column("duration")
    var duration: Long = 0L,

    /**
     * 本次实际播放时长 ms（统计求和用这个）。
     */
    @Column("actual_duration")
    var actualDuration: Long = 0L,

    /**
     * 播放完成标记：true=正常听完，false=中途切歌/暂停停止。
     */
    @Column("is_finished")
    var isFinished: Boolean = true,

    override val isUpgradeRecreated: Boolean = false,
    override val migrations: Array<OrmMigration>? = arrayOf()

) : OrmTable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PlayRecord
        if (id != other.id) return false
        if (playTime != other.playTime) return false
        if (duration != other.duration) return false
        if (actualDuration != other.actualDuration) return false
        if (isFinished != other.isFinished) return false
        if (isUpgradeRecreated != other.isUpgradeRecreated) return false
        if (songId != other.songId) return false
        if (songName != other.songName) return false
        if (!migrations.contentEquals(other.migrations)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + playTime.hashCode()
        result = 31 * result + duration.hashCode()
        result = 31 * result + actualDuration.hashCode()
        result = 31 * result + isFinished.hashCode()
        result = 31 * result + isUpgradeRecreated.hashCode()
        result = 31 * result + songId.hashCode()
        result = 31 * result + songName.hashCode()
        result = 31 * result + (migrations?.contentHashCode() ?: 0)
        return result
    }
}
