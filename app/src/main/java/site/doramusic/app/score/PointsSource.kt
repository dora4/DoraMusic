package site.doramusic.app.score

enum class PointsSource(@JvmField val desc: String) {

    /** 听歌时间（根据计时） */
    LISTEN_MUSIC("听歌时长积分"),

    /** 每日登录奖励 */
    DAILY_LOGIN("每日登录奖励"),

    /** 完成任务 */
    TASK("任务奖励"),

    /** 活动/运营事件 */
    EVENT("活动奖励"),

    /** 抽奖（一般是负分） */
    GACHA("抽奖消耗"),

    /** 兑换道具（一般是负分） */
    EXCHANGE("兑换消耗");
}
