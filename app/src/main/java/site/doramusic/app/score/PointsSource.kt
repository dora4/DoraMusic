package site.doramusic.app.score

import dora.util.GlobalContext
import site.doramusic.app.R

enum class PointsSource(@JvmField val desc: String) {

    /** 听歌时间（根据计时） */
    LISTEN_MUSIC(GlobalContext.get().getString(R.string.listening_reward)),

    /** 每日登录奖励 */
    DAILY_LOGIN(GlobalContext.get().getString(R.string.daily_login_reward)),

    /** 完成任务 */
    TASK(GlobalContext.get().getString(R.string.task_reward)),

    /** 活动/运营事件 */
    EVENT(GlobalContext.get().getString(R.string.event_reward)),

    /** 抽奖（一般是负分） */
    GACHA(GlobalContext.get().getString(R.string.gacha_cost)),

    /** 兑换道具（一般是负分） */
    EXCHANGE(GlobalContext.get().getString(R.string.exchange_cost));
}
