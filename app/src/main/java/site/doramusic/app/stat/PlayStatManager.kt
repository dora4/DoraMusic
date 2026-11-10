package site.doramusic.app.stat

import dora.db.dao.DaoFactory
import dora.db.dao.OrmDao
import java.util.Calendar

object PlayStatManager {

    private val recordDao: OrmDao<PlayRecord> = DaoFactory.getDao(PlayRecord::class.java)

    private object Sql {

        // 总播放时长：全部播放记录，取actual_duration
        val QUERY_TOTAL_DURATION = """
            SELECT SUM(actual_duration) AS total_duration
            FROM play_record
            WHERE play_time >= ? AND play_time <= ?
        """.trimIndent()

        // 完整听完的歌曲数量，is_finished=1
        val QUERY_FINISHED_COUNT = """
            SELECT COUNT(*) AS total_count
            FROM play_record
            WHERE play_time >= ? AND play_time <= ? AND is_finished = 1
        """.trimIndent()
    }

    /**
     * 插入【完整听完】记录（onCompletion回调）。
     * @param songId 歌曲id
     * @param songName 歌曲名
     * @param songFullDuration 歌曲原始全长ms
     * @param actualPlayMs 本次实际播放时长（听完一般等于songFullDuration）
     */
    @JvmOverloads
    fun insertPlayRecord(songId: Int, songName: String, songFullDuration: Long, actualPlayMs: Long = songFullDuration) {
        if (actualPlayMs <= 0) return
        val record = PlayRecord().apply {
            this.songId = songId
            this.songName = songName
            this.playTime = System.currentTimeMillis()
            this.duration = songFullDuration
            this.actualDuration = actualPlayMs
            this.isFinished = true
        }
        recordDao.insert(record)
    }

    /**
     * 插入【中途切歌/暂停退出】记录（commitCurrentPlayDuration调用）。
     * @param songId 歌曲id
     * @param songName 歌曲名
     * @param songFullDuration 歌曲原始全长ms
     * @param actualPlayMs 这次实际听了多久ms
     */
    @JvmOverloads
    fun addPartialPlayRecord(songId: Int, songName: String, songFullDuration: Long, actualPlayMs: Long) {
        if (actualPlayMs <= 0) return
        val record = PlayRecord().apply {
            this.songId = songId
            this.songName = songName
            this.playTime = System.currentTimeMillis()
            this.duration = songFullDuration
            this.actualDuration = actualPlayMs
            this.isFinished = false
        }
        recordDao.insert(record)
    }

    fun getDailyStat(): PlayStat {
        val start = TimeUtils.getDailyStartMs()
        val end = TimeUtils.getDailyEndMs()
        return queryStatByTimeRange(start, end)
    }

    fun getMonthlyStat(): PlayStat {
        val start = TimeUtils.getMonthlyStartMs()
        val end = TimeUtils.getMonthlyEndMs()
        return queryStatByTimeRange(start, end)
    }

    fun getAnnualStat(): PlayStat {
        val start = TimeUtils.getAnnualStartMs()
        val end = TimeUtils.getAnnualEndMs()
        return queryStatByTimeRange(start, end)
    }

    private fun queryStatByTimeRange(startMs: Long, endMs: Long): PlayStat {
        var totalDur = 0L
        recordDao.getDB().rawQuery(Sql.QUERY_TOTAL_DURATION, arrayOf(startMs.toString(), endMs.toString())).use { cursor ->
            if (cursor.moveToFirst()) {
                totalDur = cursor.getLong(cursor.getColumnIndexOrThrow("total_duration"))
            }
        }
        var totalCnt = 0
        recordDao.getDB().rawQuery(Sql.QUERY_FINISHED_COUNT, arrayOf(startMs.toString(), endMs.toString())).use { cursor ->
            if (cursor.moveToFirst()) {
                totalCnt = cursor.getInt(cursor.getColumnIndexOrThrow("total_count"))
            }
        }
        return PlayStat(totalDur, totalCnt)
    }

    private object TimeUtils {

        fun getDailyStartMs(): Long {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            return calendar.timeInMillis
        }

        fun getDailyEndMs(): Long {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            calendar.set(Calendar.MILLISECOND, 999)
            return calendar.timeInMillis
        }

        fun getMonthlyStartMs(): Long {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            return calendar.timeInMillis
        }

        fun getMonthlyEndMs(): Long {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            calendar.set(Calendar.MILLISECOND, 999)
            return calendar.timeInMillis
        }

        fun getAnnualStartMs(): Long {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DAY_OF_YEAR, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            return calendar.timeInMillis
        }

        fun getAnnualEndMs(): Long {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR))
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            calendar.set(Calendar.MILLISECOND, 999)
            return calendar.timeInMillis
        }
    }
}
