package site.doramusic.app.model

import dora.db.constraint.AssignType
import dora.db.constraint.PrimaryKey
import dora.db.migration.OrmMigration
import dora.db.table.Column
import dora.db.table.Ignore
import dora.db.table.OrmTable
import dora.db.table.Table
import kotlin.concurrent.Volatile

@Table("download_task")
class DownloadTask(
    @Ignore
    override val isUpgradeRecreated: Boolean = false,
    @Ignore
    override val migrations: Array<OrmMigration>? = arrayOf()
) : OrmTable {

    //任务名称 -> 名称唯一不重复
    @Column("task_name")
    @PrimaryKey(AssignType.BY_MYSELF)
    private var taskName: String? = null

    // 所属的musicId（外健）
    @Column("music_id")
    var musicId: String? = null

    //多线程访问的问题，所以需要同步机制
    //状态:正在下载、下载完成、暂停、等待、下载错误。
    @Volatile
    @Column("status")
    var status: Int = STATUS_WAIT

    // 总大小 -> (完成之后才会赋值)
    @Column("size")
    var size: Long = 0

    fun getTaskName(): String? {
        return taskName
    }

    fun setTaskName(taskName: String) {
        this.taskName = taskName
    }

    companion object {
        const val STATUS_LOADING: Int = 1
        const val STATUS_WAIT: Int = 2
        const val STATUS_PAUSE: Int = 3
        const val STATUS_ERROR: Int = 4
        const val STATUS_FINISH: Int = 5
    }
}
