package site.doramusic.app.score

import dora.db.dao.DaoFactory
import dora.db.dao.OrmDao

object PointsManager {

    private var pointDao: OrmDao<UserPoints> = DaoFactory.getDao(UserPoints::class.java)
    private var recordDao: OrmDao<PointsRecord> = DaoFactory.getDao(PointsRecord::class.java)

    @JvmOverloads
    fun addPoints(type: String, points: Int, extra: String? = null) {
        // 插入记录表
        val record = PointsRecord(
            type = type,
            points = points,
            extra = extra
        )
        recordDao.insert(record)

        // 获取总积分记录，如果没有则插入一条初始值0
        var userPoints = pointDao.selectOne()
        if (userPoints == null) {
            val initialPoints = UserPoints(totalPoints = 0)
            pointDao.insert(initialPoints)
            userPoints = initialPoints
        }
        // 更新总积分
        val newPoints = userPoints.totalPoints + points
        pointDao.update(userPoints.copy(totalPoints = newPoints))
    }

    fun getTotalPoints(): Int {
        return pointDao.selectOne()?.totalPoints ?: 0
    }

    fun getRecords(): List<PointsRecord> {
        return recordDao.selectAll()
    }
}
